---
name: Vue 前端开发
description: Vue.js 前端开发，包含组件设计、状态管理和最佳实践
---

# Vue 前端开发技能

## 项目结构

```
src/
├── components/      # 公共组件
├── views/           # 页面组件
├── router/          # 路由配置
├── store/           # Pinia 状态管理
├── api/             # API 接口
├── utils/           # 工具函数
└── App.vue          # 根组件
```

## 组件开发（Vue 3 Composition API）

### 基础结构
```vue
<template>
  <div class="user-profile">
    <h2>{{ userName }}</h2>
    <button @click="handleUpdate">更新</button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// Props
const props = defineProps({
  userId: {
    type: Number,
    required: true
  }
})

// Emits
const emit = defineEmits(['update'])

// State
const userName = ref('')

// Computed
const displayName = computed(() => userName.value.toUpperCase())

// Methods
const handleUpdate = () => {
  emit('update', { name: userName.value })
}

// Lifecycle
onMounted(() => {
  fetchUserData()
})
</script>

<style scoped>
.user-profile {
  padding: 20px;
}
</style>
```

### 组件通信

**父传子（Props）**
```vue
<!-- 父组件 -->
<UserCard :user="currentUser" />

<!-- 子组件 -->
<script setup>
const props = defineProps({
  user: { type: Object, required: true }
})
</script>
```

**子传父（Emit）**
```vue
<!-- 子组件 -->
<script setup>
const emit = defineEmits(['update'])
emit('update', { id: 1, name: 'Alice' })
</script>

<!-- 父组件 -->
<UserCard @update="handleUserUpdate" />
```

## 状态管理（Pinia）

```javascript
// store/user.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // State
  const user = ref(null)
  const token = ref('')
  
  // Getters
  const isLoggedIn = computed(() => !!token.value)
  
  // Actions
  const login = async (credentials) => {
    const response = await loginAPI(credentials)
    user.value = response.user
    token.value = response.token
  }
  
  const logout = () => {
    user.value = null
    token.value = ''
  }
  
  return { user, token, isLoggedIn, login, logout }
})
```

## 路由配置

```javascript
// router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/user/:id',
    component: () => import('@/views/UserDetail.vue'),
    props: true
  },
  {
    path: '/admin',
    meta: { requiresAuth: true },
    component: () => import('@/views/Admin.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

## API 封装

```javascript
// api/request.js
import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default request
```

```javascript
// api/user.js
import request from './request'

export const getUserList = (params) => request.get('/users', { params })
export const getUserById = (id) => request.get(`/users/${id}`)
export const createUser = (data) => request.post('/users', data)
export const updateUser = (id, data) => request.put(`/users/${id}`, data)
```

## 最佳实践

### 1. Composables（复用逻辑）
```javascript
// composables/useUser.js
import { ref, onMounted } from 'vue'
import { getUserById } from '@/api/user'

export function useUser(userId) {
  const user = ref(null)
  const loading = ref(false)
  
  const fetchUser = async () => {
    loading.value = true
    try {
      user.value = await getUserById(userId)
    } finally {
      loading.value = false
    }
  }
  
  onMounted(() => fetchUser())
  
  return { user, loading, refetch: fetchUser }
}
```

### 2. 性能优化
```vue
<script setup>
import { computed } from 'vue'

// 使用 computed 缓存
const filteredList = computed(() => {
  return list.value.filter(item => item.active)
})
</script>

<template>
  <!-- 列表使用唯一 key -->
  <div v-for="item in list" :key="item.id">
    {{ item.name }}
  </div>
</template>
```

## 代码检查清单
- [ ] 组件名使用多单词
- [ ] Props 定义类型
- [ ] 使用 scoped 样式
- [ ] 列表渲染使用唯一 key
- [ ] API 调用有错误处理
- [ ] 路由懒加载
- [ ] 使用 Composables 复用逻辑

## 输出格式

```markdown
## Vue 组件实现报告

### 功能说明
[组件用途]

### 技术方案
- 组件结构：[说明]
- 状态管理：[是否使用 Pinia]

### 核心代码
[完整的 Vue 组件代码]

### API 接口
[相关 API 定义]
```

## 推荐工具
- **构建**：Vite
- **状态管理**：Pinia
- **UI 库**：Element Plus, Ant Design Vue
- **检查**：ESLint, Prettier
