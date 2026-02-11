# 规则目录 — 代码质量

## 条件类名使用工具函数

紧急程度：高
分类：代码质量

### 描述

确保条件 CSS 通过共享的 `classNames` 工具函数处理，而非自定义三元表达式、字符串拼接或模板字符串。集中化的类名逻辑使组件保持一致且更易维护。

### 建议修复

```ts
import { cn } from '@/utils/classnames'
const classNames = cn(isActive ? 'text-primary-600' : 'text-gray-500')
```

## Tailwind 优先的样式策略

紧急程度：高
分类：代码质量

### 描述

优先使用 Tailwind CSS 工具类，除非 Tailwind 组合无法实现所需样式，否则不添加新的 `.module.css` 文件。保持使用 Tailwind 可以提高一致性并减少维护开销。

当添加、编辑或删除代码质量规则时，请更新此文件以保持目录准确。

## 类名排序以便于覆盖

### 描述

编写组件时，始终将传入的 `className` 属性放在组件自身类值之后，以便下游使用者可以覆盖或扩展样式。这样既保留了组件的默认值，又允许外部调用者更改或移除特定样式。

示例：

```tsx
import { cn } from '@/utils/classnames'

const Button = ({ className }) => {
  return <div className={cn('bg-primary-600', className)}></div>
}
```
