---
name: UI 设计专家
description: 现代化 UI/UX 设计，创建美观、易用、响应式的用户界面
---

# UI 设计专家技能

## 设计原则

### 1. 视觉层次
- **大小对比**：重要元素更大
- **颜色对比**：主要操作使用强调色
- **间距**：相关元素靠近，不相关元素分离

### 2. 一致性
- **颜色系统**：统一的配色方案
- **字体系统**：2-3 种字体，明确层级
- **间距系统**：使用 4px/8px 基准网格

### 3. 响应式设计
- **移动优先**：从小屏幕开始设计
- **断点**：576px, 768px, 992px, 1200px
- **弹性布局**：使用 Flexbox/Grid

## 配色方案

```css
/* 主色调 */
--primary: #3b82f6;         /* 蓝色 */
--primary-hover: #2563eb;
--primary-light: #dbeafe;

/* 功能色 */
--success: #10b981;         /* 绿色 */
--warning: #f59e0b;         /* 橙色 */
--danger: #ef4444;          /* 红色 */

/* 中性色 */
--text-primary: #1f2937;
--text-secondary: #6b7280;
--bg-primary: #ffffff;
--bg-secondary: #f9fafb;
--border: #e5e7eb;

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  --bg-primary: #1f2937;
  --text-primary: #f9fafb;
  --border: #374151;
}
```

## 字体系统

```css
/* 字体族 */
--font-sans: -apple-system, BlinkMacSystemFont, "Segoe UI", 
             "PingFang SC", "Microsoft YaHei", sans-serif;

/* 字体大小 */
--text-xs: 0.75rem;    /* 12px */
--text-sm: 0.875rem;   /* 14px */
--text-base: 1rem;     /* 16px */
--text-lg: 1.125rem;   /* 18px */
--text-xl: 1.25rem;    /* 20px */
--text-2xl: 1.5rem;    /* 24px */

/* 字重 */
--font-normal: 400;
--font-medium: 500;
--font-semibold: 600;
--font-bold: 700;
```

## 间距系统

```css
--space-1: 0.25rem;   /* 4px */
--space-2: 0.5rem;    /* 8px */
--space-3: 0.75rem;   /* 12px */
--space-4: 1rem;      /* 16px */
--space-6: 1.5rem;    /* 24px */
--space-8: 2rem;      /* 32px */
--space-12: 3rem;     /* 48px */
```

## 组件设计

### 按钮
```css
.btn {
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary {
  background: var(--primary);
  color: white;
}

.btn-primary:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
```

### 输入框
```css
.input {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--border);
  border-radius: 0.375rem;
  transition: all 0.2s;
}

.input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}
```

### 卡片
```css
.card {
  background: var(--bg-primary);
  border-radius: 0.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  transition: all 0.3s;
}

.card:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
```

## 布局模式

### 响应式网格
```css
.grid {
  display: grid;
  gap: 1.5rem;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}
```

### Flexbox 布局
```css
.flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
```

### 容器
```css
.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}
```

## 动画效果

### 过渡
```css
.transition {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
```

### 加载动画
```css
@keyframes spin {
  to { transform: rotate(360deg); }
}

.spinner {
  border: 3px solid var(--bg-secondary);
  border-top-color: var(--primary);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 0.8s linear infinite;
}
```

### 微交互
```css
.btn:active {
  transform: scale(0.98);
}

.card:hover {
  transform: translateY(-4px);
}
```

## 设计检查清单
- [ ] 使用统一配色方案
- [ ] 字体大小层级清晰
- [ ] 间距使用 4px/8px 基准
- [ ] 按钮有视觉反馈
- [ ] 响应式适配多端
- [ ] 对比度符合标准（≥ 4.5:1）
- [ ] 交互元素有悬停状态

## 输出格式

```markdown
## UI 设计方案

### 设计目标
[要达到的目标]

### 配色方案
- 主色：#3b82f6
- 辅助色：[颜色]

### 组件设计
[关键组件说明]

### 完整代码
[HTML + CSS 代码]
```

## 设计资源
- **配色**：Coolors, Adobe Color
- **图标**：Heroicons, Feather Icons
- **字体**：Google Fonts
- **参考**：Tailwind CSS, Material Design
