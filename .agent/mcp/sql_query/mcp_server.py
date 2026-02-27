#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
MySQL 自然语言动态SQL查询 MCP Server
- 单工具架构: business_query（自然语言查询 + SQL执行）
- 三层索引缓存: L1目录级 + L2表级，10分钟过期
- 严格只读: 仅允许 SELECT 查询
- 分层检索: 先 L1 匹配相关表，再按需加载 L2 详情
"""

import sys
import json
import os
import re
import time
import argparse
from datetime import datetime, date
from typing import Any, Dict, List, Optional


# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  JSON 编码器
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

class DateTimeEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, (datetime, date)):
            return obj.isoformat()
        return super().default(obj)


# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  数据库连接管理
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

class DBConnection:
    """数据库连接管理器，复用单一连接"""

    def __init__(self, db_config):
        self.db_config = db_config
        self._conn = None

    def get(self):
        """获取连接，断线则重连"""
        if self._conn is None:
            self._connect()
        else:
            try:
                self._conn.ping(reconnect=True)
            except Exception:
                self._connect()
        return self._conn

    def _connect(self):
        import pymysql
        self._conn = pymysql.connect(
            host=self.db_config.get('host', 'localhost'),
            port=int(self.db_config.get('port', 3306)),
            user=self.db_config.get('user', 'root'),
            password=self.db_config.get('password', ''),
            database=self.db_config.get('database', ''),
            charset='utf8mb4',
            cursorclass=pymysql.cursors.DictCursor,
            autocommit=True,
            connect_timeout=5,
            read_timeout=30
        )

    def close(self):
        if self._conn:
            try:
                self._conn.close()
            except Exception:
                pass
            self._conn = None


# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  索引管理器
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CACHE_EXPIRE_SECONDS = 600  # 10分钟


class SchemaIndexManager:
    """
    三层索引管理器
    L1: 全库表目录（表名+注释，按前缀分域） -> db_cache/L1_catalog.json
    L2: 单表详情（字段+索引+主键+外键）     -> db_cache/L2_tables/<table>.json
    """

    def __init__(self, db_conn, db_config, cache_dir):
        self.db_conn = db_conn
        self.db_config = db_config
        self.cache_dir = cache_dir
        self.l2_dir = os.path.join(cache_dir, "L2_tables")
        self._l1_cache = None
        os.makedirs(self.l2_dir, exist_ok=True)

    # ──── 对外接口 ────

    def ensure_index(self):
        """确保 L1 索引可用且未过期，过期则重建"""
        l1 = self._load_l1()
        if l1 and not self._is_expired(l1.get("generated_at", 0)):
            self._l1_cache = l1
            return
        try:
            self._rebuild_all()
        except Exception as e:
            print(f"Rebuild index failed: {e}", file=sys.stderr)
            # 重建失败时尝试使用过期缓存兜底
            if l1:
                self._l1_cache = l1
            else:
                # 无缓存可用，构造空目录
                self._l1_cache = {"database": self.db_config.get('database', ''), "generated_at": 0, "table_count": 0, "domains": {}}

    def find_relevant_tables(self, query):
        """
        根据自然语言在 L1 中模糊匹配相关表名。
        返回匹配到的表名列表（最多20个）。
        """
        if not self._l1_cache:
            self.ensure_index()
        query_lower = query.lower()
        scored = []
        domains = self._l1_cache.get("domains", {})
        for domain, tables in domains.items():
            for t in tables:
                name = t["name"].lower()
                comment = (t.get("comment") or "").lower()
                score = 0
                # 完整表名匹配
                if name in query_lower:
                    score += 10
                # 域前缀匹配
                if domain in query_lower:
                    score += 3
                # 注释关键词匹配
                for word in self._tokenize(query_lower):
                    if word in name:
                        score += 5
                    if word in comment:
                        score += 4
                if score > 0:
                    scored.append((t["name"], score))
        scored.sort(key=lambda x: -x[1])
        if not scored:
            # 无匹配时返回全部表名（仅名称）让 LLM 自行判断，限制数量避免 L2 加载过慢
            all_tables = []
            for tables in domains.values():
                for t in tables:
                    all_tables.append(t["name"])
            return all_tables[:10]
        return [s[0] for s in scored[:20]]

    def get_table_details(self, table_names):
        """
        获取指定表的 L2 详情。按需从缓存或数据库加载。
        返回: {table_name: {columns, indexes, primary_key, comment}}
        """
        result = {}
        for name in table_names:
            detail = self._load_l2(name)
            if detail and not self._is_expired(detail.get("generated_at", 0)):
                result[name] = detail
            else:
                detail = self._build_l2_detail(name)
                if detail:
                    self._save_l2(name, detail)
                    result[name] = detail
        return result

    def get_l1_summary(self):
        """返回 L1 目录摘要文本"""
        if not self._l1_cache:
            self.ensure_index()
        domains = self._l1_cache.get("domains", {})
        lines = [f"数据库: {self._l1_cache.get('database', '?')}, 共 {self._l1_cache.get('table_count', '?')} 张表\n"]
        for domain, tables in domains.items():
            lines.append(f"【{domain}】({len(tables)} 张)")
            for t in tables:
                lines.append(f"  - {t['name']}: {t.get('comment') or '无注释'}")
        return "\n".join(lines)

    # ──── L1 构建 ────

    def _rebuild_all(self):
        """重建 L1 目录 + 清理过期 L2"""
        self._l1_cache = self._build_l1_catalog()
        self._save_l1(self._l1_cache)
        # 清理 L2 缓存：L1 重建后 L2 按需重新加载即可
        print("Schema index rebuilt", file=sys.stderr)

    def _build_l1_catalog(self):
        """从 INFORMATION_SCHEMA 构建 L1 目录"""
        conn = self.db_conn.get()
        database = self.db_config.get('database', '')
        with conn.cursor() as cursor:
            cursor.execute("SELECT TABLE_NAME, TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = %s ORDER BY TABLE_NAME", [database])
            rows = cursor.fetchall()

        domains = {}
        for row in rows:
            name = row['TABLE_NAME']
            comment = row.get('TABLE_COMMENT', '') or ''
            # 按下划线前缀分域，如 mst_carrier -> mst, ord_order -> ord
            parts = name.split('_', 1)
            domain = parts[0] if len(parts) > 1 else '_other'
            if domain not in domains:
                domains[domain] = []
            domains[domain].append({"name": name, "comment": comment})

        catalog = {
            "database": database,
            "generated_at": time.time(),
            "table_count": len(rows),
            "domains": domains
        }
        return catalog

    # ──── L2 构建 ────

    def _build_l2_detail(self, table_name):
        """构建单表 L2 详情"""
        conn = self.db_conn.get()
        database = self.db_config.get('database', '')
        try:
            with conn.cursor() as cursor:
                # 表注释
                cursor.execute("SELECT TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = %s AND TABLE_NAME = %s", [database, table_name])
                table_row = cursor.fetchone()
                if not table_row:
                    return None
                comment = table_row.get('TABLE_COMMENT', '') or ''

                # 字段信息
                cursor.execute("""
                    SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE, COLUMN_KEY, COLUMN_COMMENT
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE TABLE_SCHEMA = %s AND TABLE_NAME = %s
                    ORDER BY ORDINAL_POSITION
                """, [database, table_name])
                col_rows = cursor.fetchall()

                columns = []
                primary_key = []
                for c in col_rows:
                    col = {
                        "name": c['COLUMN_NAME'],
                        "type": c['DATA_TYPE'],
                        "nullable": c['IS_NULLABLE'] == 'YES',
                        "key": c['COLUMN_KEY'] or '',
                        "comment": c.get('COLUMN_COMMENT', '') or ''
                    }
                    if c.get('CHARACTER_MAXIMUM_LENGTH'):
                        col["length"] = c['CHARACTER_MAXIMUM_LENGTH']
                    columns.append(col)
                    if c['COLUMN_KEY'] == 'PRI':
                        primary_key.append(c['COLUMN_NAME'])

                # 索引信息
                cursor.execute("""
                    SELECT INDEX_NAME, NON_UNIQUE, COLUMN_NAME, SEQ_IN_INDEX
                    FROM INFORMATION_SCHEMA.STATISTICS
                    WHERE TABLE_SCHEMA = %s AND TABLE_NAME = %s
                    ORDER BY INDEX_NAME, SEQ_IN_INDEX
                """, [database, table_name])
                idx_rows = cursor.fetchall()

                index_map = {}
                for idx in idx_rows:
                    idx_name = idx['INDEX_NAME']
                    if idx_name not in index_map:
                        index_map[idx_name] = {"name": idx_name, "columns": [], "unique": idx['NON_UNIQUE'] == 0, "primary": idx_name == 'PRIMARY'}
                    index_map[idx_name]["columns"].append(idx['COLUMN_NAME'])
                indexes = list(index_map.values())

                # 外键信息
                cursor.execute("""
                    SELECT COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
                    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
                    WHERE TABLE_SCHEMA = %s AND TABLE_NAME = %s AND REFERENCED_TABLE_NAME IS NOT NULL
                """, [database, table_name])
                fk_rows = cursor.fetchall()
                foreign_keys = [{"column": fk['COLUMN_NAME'], "ref_table": fk['REFERENCED_TABLE_NAME'], "ref_column": fk['REFERENCED_COLUMN_NAME']} for fk in fk_rows]

            return {
                "table": table_name,
                "comment": comment,
                "generated_at": time.time(),
                "primary_key": primary_key,
                "columns": columns,
                "indexes": indexes,
                "foreign_keys": foreign_keys
            }
        except Exception as e:
            print(f"Build L2 detail failed for {table_name}: {e}", file=sys.stderr)
            return None

    # ──── 缓存读写 ────

    def _l1_path(self):
        return os.path.join(self.cache_dir, "L1_catalog.json")

    def _l2_path(self, table_name):
        return os.path.join(self.l2_dir, f"{table_name}.json")

    def _load_l1(self):
        path = self._l1_path()
        if os.path.exists(path):
            try:
                with open(path, 'r', encoding='utf-8') as f:
                    return json.load(f)
            except Exception:
                return None
        return None

    def _save_l1(self, data):
        with open(self._l1_path(), 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)

    def _load_l2(self, table_name):
        path = self._l2_path(table_name)
        if os.path.exists(path):
            try:
                with open(path, 'r', encoding='utf-8') as f:
                    return json.load(f)
            except Exception:
                return None
        return None

    def _save_l2(self, table_name, data):
        with open(self._l2_path(table_name), 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)

    def _is_expired(self, generated_at):
        return (time.time() - generated_at) > CACHE_EXPIRE_SECONDS

    def _tokenize(self, text):
        """简单分词：提取连续中文或英文字母数字下划线片段"""
        return [w for w in re.findall(r'[\u4e00-\u9fff]+|[a-z0-9_]+', text) if len(w) >= 2]


# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  只读查询执行器
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

# 禁止的 SQL 关键词（非 SELECT 类）
_FORBIDDEN_KEYWORDS = re.compile(
    r'^\s*(INSERT|UPDATE|DELETE|DROP|ALTER|CREATE|TRUNCATE|REPLACE|RENAME|GRANT|REVOKE|CALL|EXEC|EXECUTE|LOAD|MERGE|SET|LOCK|UNLOCK)\b',
    re.IGNORECASE | re.MULTILINE
)


class QueryExecutor:
    """只读查询执行器"""

    def __init__(self, db_conn):
        self.db_conn = db_conn

    def execute_readonly(self, sql, max_rows=500):
        """
        只读校验后执行 SQL。
        返回: (rows, error)
        """
        error = self._validate_readonly(sql)
        if error:
            return None, error
        try:
            conn = self.db_conn.get()
            with conn.cursor() as cursor:
                cursor.execute(sql)
                rows = cursor.fetchmany(max_rows)
                return rows, None
        except Exception as e:
            return None, f"SQL 执行失败: {e}"

    def _validate_readonly(self, sql):
        """严格校验只允许 SELECT"""
        stripped = sql.strip()
        if not stripped:
            return "SQL 语句不能为空"
        # 去除前导注释
        cleaned = re.sub(r'/\*.*?\*/', '', stripped, flags=re.DOTALL)
        cleaned = re.sub(r'--[^\n]*', '', cleaned)
        cleaned = cleaned.strip()
        if not cleaned.upper().startswith('SELECT'):
            return f"仅允许 SELECT 查询，拒绝执行: {stripped[:80]}"
        if _FORBIDDEN_KEYWORDS.search(cleaned):
            return f"检测到禁止的 SQL 操作，拒绝执行: {stripped[:80]}"
        # 检查子查询中是否藏了写操作（简单正则兜底）
        if re.search(r'\b(INSERT|UPDATE|DELETE|DROP|ALTER|CREATE|TRUNCATE)\b', cleaned, re.IGNORECASE):
            return f"SQL 中包含禁止的操作关键词，拒绝执行: {stripped[:80]}"
        return None


# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  上下文格式化
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

def build_schema_context(index_mgr, query):
    """
    根据自然语言构建表结构上下文 + 参考SQL，供 LLM 直接调用 sql 参数执行。
    """
    relevant_tables = index_mgr.find_relevant_tables(query)
    if not relevant_tables:
        return "未找到与查询相关的表。\n\n" + index_mgr.get_l1_summary()

    details = index_mgr.get_table_details(relevant_tables)

    lines = []
    lines.append(f"## 数据库表结构上下文\n")
    lines.append(f"以下是与查询 \"{query}\" 相关的 {len(details)} 张表的结构信息。\n")

    for table_name, detail in details.items():
        lines.append(f"### 表: {table_name}")
        lines.append(f"注释: {detail.get('comment', '无')}")
        lines.append(f"主键: {', '.join(detail.get('primary_key', []))}\n")

        # 字段
        lines.append("**字段:**")
        lines.append("| 列名 | 类型 | 可空 | 键 | 注释 |")
        lines.append("|------|------|------|-----|------|")
        for col in detail.get("columns", []):
            col_type = col["type"]
            if col.get("length"):
                col_type += f"({col['length']})"
            nullable = '是' if col.get('nullable') else '否'
            lines.append(f"| {col['name']} | {col_type} | {nullable} | {col.get('key', '')} | {col.get('comment', '')} |")

        # 索引
        idxs = detail.get("indexes", [])
        if idxs:
            lines.append("\n**索引:**")
            for idx in idxs:
                idx_type = "主键" if idx.get("primary") else ("唯一" if idx.get("unique") else "普通")
                lines.append(f"- {idx['name']} ({idx_type}): {', '.join(idx['columns'])}")

        # 外键
        fks = detail.get("foreign_keys", [])
        if fks:
            lines.append("\n**外键:**")
            for fk in fks:
                lines.append(f"- {fk['column']} → {fk['ref_table']}.{fk['ref_column']}")

        lines.append("")

    # 生成参考 SQL
    suggested_sql = _suggest_sql(query, details)
    lines.append("---")
    lines.append(f"**请使用 business_query 工具的 sql 参数执行以下 SQL（可根据需要调整）：**\n")
    lines.append(f"```sql\n{suggested_sql}\n```\n")

    return "\n".join(lines)


def _suggest_sql(query, details):
    """
    根据用户自然语言和匹配到的表结构，生成一条参考 SELECT SQL。
    简单规则匹配，覆盖常见查询模式。
    """
    if not details:
        return "SELECT 1"

    table_names = list(details.keys())
    primary_table = table_names[0]
    primary_detail = details[primary_table]
    columns = [c["name"] for c in primary_detail.get("columns", [])]

    query_lower = query.lower()

    # 提取可能的条件值（如 T001, T002 等编码值）
    where_clauses = []
    import re as _re
    # 匹配 T001/T002 风格的编码
    code_matches = _re.findall(r'\b([A-Z]\d{3,})\b', query)
    if code_matches:
        # 查找包含 code/tenant_code 等字段
        for code_val in code_matches:
            prefix = code_val[0].lower()
            # 猜测字段名：以 t 开头的编码通常对应 tenant_code
            candidate_cols = []
            for c in primary_detail.get("columns", []):
                col_name = c["name"].lower()
                col_comment = (c.get("comment") or "").lower()
                if "code" in col_name or "编码" in col_comment:
                    candidate_cols.append(c["name"])
            # 优先匹配带 tenant 的字段
            tenant_col = None
            for cc in candidate_cols:
                if "tenant" in cc.lower():
                    tenant_col = cc
                    break
            if not tenant_col and candidate_cols:
                tenant_col = candidate_cols[0]
            if tenant_col:
                where_clauses.append(f"{primary_table}.{tenant_col} = '{code_val}'")

    # 判断是否需要 JOIN（多表且有外键关系）
    join_clause = ""
    select_cols = f"{primary_table}.*"
    if len(table_names) > 1:
        secondary_table = table_names[1]
        secondary_detail = details[secondary_table]
        # 查找外键关联
        for fk in primary_detail.get("foreign_keys", []):
            if fk["ref_table"] == secondary_table:
                join_clause = f"\nLEFT JOIN {secondary_table} ON {primary_table}.{fk['column']} = {secondary_table}.{fk['ref_column']}"
                select_cols = f"{primary_table}.*, {secondary_table}.name AS {secondary_table}_name"
                break
        # 反向：secondary 的外键指向 primary
        if not join_clause:
            for fk in secondary_detail.get("foreign_keys", []):
                if fk["ref_table"] == primary_table:
                    join_clause = f"\nLEFT JOIN {secondary_table} ON {secondary_table}.{fk['column']} = {primary_table}.{fk['ref_column']}"
                    select_cols = f"{primary_table}.*, {secondary_table}.name AS {secondary_table}_name"
                    break
        # 尝试同名字段关联
        if not join_clause:
            primary_cols = {c["name"] for c in primary_detail.get("columns", [])}
            for c in secondary_detail.get("columns", []):
                if c["name"] in primary_cols and ("code" in c["name"].lower() or "id" in c["name"].lower()):
                    join_clause = f"\nLEFT JOIN {secondary_table} ON {primary_table}.{c['name']} = {secondary_table}.{c['name']}"
                    select_cols = f"{primary_table}.*, {secondary_table}.name AS {secondary_table}_name"
                    break

    # 判断聚合
    agg_keywords = {'数量': 'COUNT(*)', '多少': 'COUNT(*)', '统计': 'COUNT(*)', '总额': 'SUM', '总和': 'SUM', '平均': 'AVG'}
    is_agg = False
    for kw, func in agg_keywords.items():
        if kw in query_lower:
            is_agg = True
            if func == 'COUNT(*)':
                select_cols = "COUNT(*) AS total"
            break

    # 构建 SQL
    sql = f"SELECT {select_cols}\nFROM {primary_table}"
    if join_clause:
        sql += join_clause
    if where_clauses:
        sql += "\nWHERE " + " AND ".join(where_clauses)
    if not is_agg:
        sql += "\nLIMIT 100"
    return sql


def format_query_result(rows, sql):
    """将查询结果格式化为 Markdown 表格"""
    if not rows:
        return f"查询无结果。\n\n执行的SQL:\n```sql\n{sql}\n```"

    headers = list(rows[0].keys())
    lines = [f"**查询结果** (共 {len(rows)} 条)\n"]
    lines.append(f"```sql\n{sql}\n```\n")
    lines.append("| " + " | ".join(headers) + " |")
    lines.append("| " + " | ".join(["---"] * len(headers)) + " |")
    for row in rows:
        values = []
        for h in headers:
            v = row.get(h)
            if v is None:
                values.append("-")
            elif isinstance(v, (datetime, date)):
                values.append(str(v).replace('T', ' '))
            else:
                values.append(str(v))
        lines.append("| " + " | ".join(values) + " |")
    return "\n".join(lines)


# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  MCP Server
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

class MCPServer:
    """MCP Server: 单工具架构"""

    def __init__(self, db_config):
        self.db_config = db_config
        cache_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "db_cache")
        self.db_conn = DBConnection(db_config)
        self.index_mgr = SchemaIndexManager(self.db_conn, db_config, cache_dir)
        self.executor = QueryExecutor(self.db_conn)
        self._initialized = False

    def initialize(self):
        """初始化：建立连接 + 确保索引"""
        try:
            self.db_conn.get()  # 测试连接
            self.index_mgr.ensure_index()
            self._initialized = True
            print("MCP Server initialized successfully", file=sys.stderr)
        except Exception as e:
            print(f"MCP Server init failed: {e}", file=sys.stderr)
            self._initialized = False

    def close(self):
        self.db_conn.close()

    def list_tools(self):
        return [
            {
                "name": "business_query",
                "description": "基于业务语义的自然语言查询（需要业务语境识别能力）",
                "inputSchema": {
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "自然语言查询，例如：查询所有承运商"
                        },
                        "sql": {
                            "type": "string",
                            "description": "可选，直接执行的 SELECT SQL 语句。传入此参数时将跳过上下文生成，直接执行 SQL 并返回结果。"
                        }
                    },
                    "required": ["query"]
                }
            }
        ]

    def call_tool(self, tool_name, arguments):
        if tool_name != "business_query":
            return self._text_result(f"未知工具: {tool_name}")

        if not self._initialized:
            return self._text_result("MCP Server 未正确初始化，请检查数据库连接")

        try:
            # 确保索引未过期
            self.index_mgr.ensure_index()

            sql = (arguments.get("sql") or "").strip()
            query = (arguments.get("query") or "").strip()

            # 模式1: 直接执行 SQL
            if sql:
                rows, error = self.executor.execute_readonly(sql)
                if error:
                    return self._text_result(f"❌ {error}")
                return self._text_result(format_query_result(rows, sql))

            # 模式2: 自然语言 → 返回表结构上下文
            if query:
                context = build_schema_context(self.index_mgr, query)
                return self._text_result(context)

            return self._text_result("请提供 query（自然语言查询）或 sql（SQL语句）参数")

        except Exception as e:
            return self._text_result(f"执行出错: {e}")

    def _text_result(self, text):
        return {"content": [{"type": "text", "text": text}]}


# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  JSON-RPC stdio 通信
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

def read_json_message():
    try:
        line = sys.stdin.readline()
        if not line:
            return None
        return json.loads(line.strip())
    except Exception as e:
        print(f"Error reading message: {e}", file=sys.stderr)
        return None


def write_json_message(message):
    try:
        # 使用 ensure_ascii=True 保证不会因为无效 Unicode 字符导致 encode 失败
        data = json.dumps(message, ensure_ascii=True, cls=DateTimeEncoder)
        sys.stdout.buffer.write(data.encode('utf-8') + b"\n")
        sys.stdout.buffer.flush()
    except Exception as e:
        # 编码失败时发送错误响应，防止客户端无限等待
        print(f"Error writing message: {e}", file=sys.stderr)
        try:
            fallback = json.dumps({"jsonrpc": "2.0", "id": message.get("id"), "error": {"code": -32603, "message": f"Response encoding error: {e}"}})
            sys.stdout.buffer.write(fallback.encode('utf-8') + b"\n")
            sys.stdout.buffer.flush()
        except Exception:
            pass


def handle_request(server, request):
    method = request.get("method")
    request_id = request.get("id")

    if request_id is None:
        return

    if method == "ping":
        write_json_message({"jsonrpc": "2.0", "id": request_id, "result": {}})
    elif method == "initialize":
        write_json_message({
            "jsonrpc": "2.0",
            "id": request_id,
            "result": {
                "protocolVersion": "2024-11-05",
                "capabilities": {"tools": {}},
                "serverInfo": {"name": "sql-business-query", "version": "2.0.0"}
            }
        })
    elif method == "tools/list":
        write_json_message({"jsonrpc": "2.0", "id": request_id, "result": {"tools": server.list_tools()}})
    elif method == "tools/call":
        params = request.get("params", {})
        result = server.call_tool(params.get("name"), params.get("arguments", {}))
        write_json_message({"jsonrpc": "2.0", "id": request_id, "result": result})
    else:
        write_json_message({"jsonrpc": "2.0", "id": request_id, "error": {"code": -32601, "message": f"Method not found: {method}"}})


# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  主入口
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

def main():
    parser = argparse.ArgumentParser(description='MySQL Business Query MCP Server')
    parser.add_argument('--host', default=os.getenv('DB_HOST', '127.0.0.1'))
    parser.add_argument('--port', type=int, default=int(os.getenv('DB_PORT', '3306')))
    parser.add_argument('--user', default=os.getenv('DB_USER', 'root'))
    parser.add_argument('--password', default=os.getenv('DB_PASSWORD', '123456'))
    parser.add_argument('--database', default=os.getenv('DB_DATABASE', 'test001'))
    args = parser.parse_args()

    config = {"host": args.host, "port": args.port, "user": args.user, "password": args.password, "database": args.database}

    server = MCPServer(config)
    server.initialize()

    try:
        while True:
            request = read_json_message()
            if request is None:
                break
            handle_request(server, request)
    except KeyboardInterrupt:
        pass
    finally:
        server.close()


if __name__ == "__main__":
    main()
