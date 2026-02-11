# 规则目录 — 性能

## React Flow 数据使用

紧急程度：高
分类：性能

### 描述

渲染 React Flow 时，UI 消费优先使用 `useNodes`/`useEdges`，在需要修改或读取节点/边状态的回调中依赖 `useStoreApi`。避免在这些 Hooks 之外手动获取 Flow 数据。

## 复杂属性记忆化

紧急程度：高
分类：性能

### 描述

在传递给子组件之前，使用 `useMemo` 包装复杂的属性值（对象、数组、映射），以保证引用稳定并防止不必要的重新渲染。

当添加、编辑或删除性能规则时，请更新此文件以保持目录准确。

错误示例：

```tsx
<HeavyComp
    config={{
        provider: ...,
        detail: ...
    }}
/>
```

正确示例：

```tsx
const config = useMemo(() => ({
    provider: ...,
    detail: ...
}), [provider, detail]);

<HeavyComp
    config={config}
/>
```
