# 动画库参考

## Framer Motion

最流行的 React 动画库，提供声明式 API。

### 基础动画

```tsx
import { motion, AnimatePresence } from "framer-motion";

// 简单动画
function FadeIn({ children }) {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.3 }}
    >
      {children}
    </motion.div>
  );
}

// 手势动画
function InteractiveCard() {
  return (
    <motion.div
      whileHover={{ scale: 1.02, y: -4 }}
      whileTap={{ scale: 0.98 }}
      transition={{ type: "spring", stiffness: 400, damping: 17 }}
      className="p-6 bg-white rounded-lg shadow"
    >
      悬停或点击我
    </motion.div>
  );
}

// 关键帧动画
function PulseButton() {
  return (
    <motion.button
      animate={{
        scale: [1, 1.05, 1],
        boxShadow: [
          "0 0 0 0 rgba(59, 130, 246, 0.5)",
          "0 0 0 10px rgba(59, 130, 246, 0)",
          "0 0 0 0 rgba(59, 130, 246, 0)",
        ],
      }}
      transition={{ duration: 2, repeat: Infinity }}
      className="px-4 py-2 bg-blue-600 text-white rounded"
    >
      点击我
    </motion.button>
  );
}
```

### 布局动画

```tsx
import { motion, LayoutGroup } from "framer-motion";

// 共享布局动画
function TabIndicator({ activeTab, tabs }) {
  return (
    <div className="flex border-b">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          onClick={() => setActiveTab(tab.id)}
          className="relative px-4 py-2"
        >
          {tab.label}
          {activeTab === tab.id && (
            <motion.div
              layoutId="activeTab"
              className="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-600"
              transition={{ type: "spring", stiffness: 500, damping: 30 }}
            />
          )}
        </button>
      ))}
    </div>
  );
}

// 自动布局重排序
function ReorderableList({ items, setItems }) {
  return (
    <Reorder.Group axis="y" values={items} onReorder={setItems}>
      {items.map((item) => (
        <Reorder.Item
          key={item.id}
          value={item}
          className="bg-white p-4 rounded-lg shadow mb-2 cursor-grab active:cursor-grabbing"
        >
          {item.title}
        </Reorder.Item>
      ))}
    </Reorder.Group>
  );
}
```

### 编排

```tsx
// 交错子元素
const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
      delayChildren: 0.2,
    },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.3 },
  },
};

function StaggeredList({ items }) {
  return (
    <motion.ul variants={containerVariants} initial="hidden" animate="visible">
      {items.map((item) => (
        <motion.li key={item.id} variants={itemVariants}>
          {item.content}
        </motion.li>
      ))}
    </motion.ul>
  );
}
```

### 页面过渡

```tsx
import { AnimatePresence, motion } from "framer-motion";
import { useRouter } from "next/router";

const pageVariants = {
  initial: { opacity: 0, x: -20 },
  enter: { opacity: 1, x: 0 },
  exit: { opacity: 0, x: 20 },
};

function PageTransition({ children }) {
  const router = useRouter();

  return (
    <AnimatePresence mode="wait" initial={false}>
      <motion.div
        key={router.pathname}
        variants={pageVariants}
        initial="initial"
        animate="enter"
        exit="exit"
        transition={{ duration: 0.3 }}
      >
        {children}
      </motion.div>
    </AnimatePresence>
  );
}
```

## GSAP (GreenSock)

业界标准的动画库，适用于复杂的高性能动画。

### 基础时间线

```tsx
import { useRef, useLayoutEffect } from "react";
import gsap from "gsap";

function AnimatedHero() {
  const containerRef = useRef<HTMLDivElement>(null);
  const titleRef = useRef<HTMLHeadingElement>(null);
  const subtitleRef = useRef<HTMLParagraphElement>(null);

  useLayoutEffect(() => {
    const ctx = gsap.context(() => {
      const tl = gsap.timeline({ defaults: { ease: "power3.out" } });

      tl.from(titleRef.current, {
        y: 50,
        opacity: 0,
        duration: 0.8,
      })
        .from(
          subtitleRef.current,
          {
            y: 30,
            opacity: 0,
            duration: 0.6,
          },
          "-=0.4", // 在上一个结束前 0.4s 开始
        )
        .from(".cta-button", {
          scale: 0.8,
          opacity: 0,
          duration: 0.4,
        });
    }, containerRef);

    return () => ctx.revert(); // 清理
  }, []);

  return (
    <div ref={containerRef}>
      <h1 ref={titleRef}>欢迎</h1>
      <p ref={subtitleRef}>发现精彩事物</p>
      <button className="cta-button">开始使用</button>
    </div>
  );
}
```

### ScrollTrigger

```tsx
import { useLayoutEffect, useRef } from "react";
import gsap from "gsap";
import { ScrollTrigger } from "gsap/ScrollTrigger";

gsap.registerPlugin(ScrollTrigger);

function ParallaxSection() {
  const sectionRef = useRef<HTMLDivElement>(null);
  const imageRef = useRef<HTMLImageElement>(null);

  useLayoutEffect(() => {
    const ctx = gsap.context(() => {
      // 视差图片
      gsap.to(imageRef.current, {
        yPercent: -20,
        ease: "none",
        scrollTrigger: {
          trigger: sectionRef.current,
          start: "top bottom",
          end: "bottom top",
          scrub: true,
        },
      });

      // 内容淡入
      gsap.from(".content-block", {
        opacity: 0,
        y: 50,
        stagger: 0.2,
        scrollTrigger: {
          trigger: sectionRef.current,
          start: "top 80%",
          end: "top 20%",
          scrub: 1,
        },
      });
    }, sectionRef);

    return () => ctx.revert();
  }, []);

  return (
    <section ref={sectionRef} className="relative overflow-hidden">
      <img ref={imageRef} src="/hero.jpg" alt="" className="absolute inset-0" />
      <div className="relative z-10">
        <div className="content-block">区块 1</div>
        <div className="content-block">区块 2</div>
      </div>
    </section>
  );
}
```

### 文字动画

```tsx
import { useLayoutEffect, useRef } from "react";
import gsap from "gsap";
import { SplitText } from "gsap/SplitText";

gsap.registerPlugin(SplitText);

function AnimatedHeadline({ text }) {
  const textRef = useRef<HTMLHeadingElement>(null);

  useLayoutEffect(() => {
    const split = new SplitText(textRef.current, {
      type: "chars,words",
      charsClass: "char",
    });

    gsap.from(split.chars, {
      opacity: 0,
      y: 50,
      rotateX: -90,
      stagger: 0.02,
      duration: 0.8,
      ease: "back.out(1.7)",
    });

    return () => split.revert();
  }, [text]);

  return <h1 ref={textRef}>{text}</h1>;
}
```

## CSS 弹性物理

```tsx
// spring.ts - 自定义弹性物理
interface SpringConfig {
  stiffness: number; // 越高 = 越灵敏
  damping: number; // 越高 = 越少弹跳
  mass: number;
}

const presets: Record<string, SpringConfig> = {
  default: { stiffness: 170, damping: 26, mass: 1 },
  gentle: { stiffness: 120, damping: 14, mass: 1 },
  wobbly: { stiffness: 180, damping: 12, mass: 1 },
  stiff: { stiffness: 210, damping: 20, mass: 1 },
  slow: { stiffness: 280, damping: 60, mass: 1 },
  molasses: { stiffness: 280, damping: 120, mass: 1 },
};

function springToCss(config: SpringConfig): string {
  // 将弹性参数转换为 CSS 时间函数近似值
  const { stiffness, damping } = config;
  const duration = Math.sqrt(stiffness) / damping;
  const bounce = 1 - damping / (2 * Math.sqrt(stiffness));

  // 映射到 cubic-bezier（近似值）
  if (bounce <= 0) {
    return `cubic-bezier(0.25, 0.1, 0.25, 1)`;
  }
  return `cubic-bezier(0.34, 1.56, 0.64, 1)`;
}
```

## Web Animations API

原生浏览器动画 API，适用于简单动画。

```tsx
function useWebAnimation(
  ref: RefObject<HTMLElement>,
  keyframes: Keyframe[],
  options: KeyframeAnimationOptions,
) {
  useEffect(() => {
    if (!ref.current) return;

    const animation = ref.current.animate(keyframes, options);

    return () => animation.cancel();
  }, [ref, keyframes, options]);
}

// 使用示例
function SlideIn({ children }) {
  const elementRef = useRef<HTMLDivElement>(null);

  useWebAnimation(
    elementRef,
    [
      { transform: "translateX(-100%)", opacity: 0 },
      { transform: "translateX(0)", opacity: 1 },
    ],
    {
      duration: 300,
      easing: "cubic-bezier(0.16, 1, 0.3, 1)",
      fill: "forwards",
    },
  );

  return <div ref={elementRef}>{children}</div>;
}
```

## View Transitions API

原生浏览器页面过渡 API。

```tsx
// 检查支持
const supportsViewTransitions = "startViewTransition" in document;

// 简单页面过渡
async function navigateTo(url: string) {
  if (!document.startViewTransition) {
    window.location.href = url;
    return;
  }

  document.startViewTransition(async () => {
    await fetch(url);
    // 更新 DOM
  });
}

// 用于变形的命名元素
function ProductCard({ product }) {
  return (
    <Link href={`/product/${product.id}`}>
      <img
        src={product.image}
        style={{ viewTransitionName: `product-${product.id}` }}
      />
    </Link>
  );
}

// View Transitions 的 CSS
/*
::view-transition-old(root) {
  animation: fade-out 0.25s ease-out;
}

::view-transition-new(root) {
  animation: fade-in 0.25s ease-in;
}

::view-transition-group(product-*) {
  animation-duration: 0.3s;
}
*/
```

## 性能优化

### GPU 加速

```css
/* 触发 GPU 加速的属性 */
.animated-element {
  transform: translateZ(0); /* 强制 GPU 层 */
  will-change: transform, opacity; /* 提示浏览器 */
}

/* 仅动画 transform 和 opacity 以保持 60fps */
.smooth {
  transition:
    transform 0.3s ease,
    opacity 0.3s ease;
}

/* 避免动画这些属性（导致重排） */
.avoid {
  /* 不要动画：width、height、top、left、margin、padding */
}
```

### 减少动效

```tsx
function useReducedMotion() {
  const [prefersReduced, setPrefersReduced] = useState(false);

  useEffect(() => {
    const mq = window.matchMedia("(prefers-reduced-motion: reduce)");
    setPrefersReduced(mq.matches);

    const handler = (e: MediaQueryListEvent) => setPrefersReduced(e.matches);
    mq.addEventListener("change", handler);
    return () => mq.removeEventListener("change", handler);
  }, []);

  return prefersReduced;
}

// 使用示例
function AnimatedComponent() {
  const prefersReduced = useReducedMotion();

  return (
    <motion.div
      initial={{ opacity: 0, y: prefersReduced ? 0 : 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: prefersReduced ? 0 : 0.3 }}
    >
      内容
    </motion.div>
  );
}
```
