import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/admin/AdminLogin.vue')
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/AdminDashboard.vue')
      },
      {
        path: 'goods',
        name: 'AdminGoods',
        component: () => import('../views/admin/AdminGoods.vue')
      },
      {
        path: 'category',
        name: 'AdminCategory',
        component: () => import('../views/admin/AdminCategory.vue')
      },
      {
        path: 'news',
        name: 'AdminNews',
        component: () => import('../views/admin/AdminNews.vue')
      },
      {
        path: 'point-goods',
        name: 'AdminPointGoods',
        component: () => import('../views/admin/AdminPointGoods.vue')
      },
      {
        path: 'point-orders',
        name: 'AdminPointOrders',
        component: () => import('../views/admin/AdminPointOrders.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/AdminUsers.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/AdminOrders.vue')
      }
    ]
  },
  {
    path: '/market',
    name: 'Market',
    component: () => import('../views/Market.vue')
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: () => import('../views/UserCenter.vue')
  },
  {
    path: '/news',
    name: 'News',
    component: () => import('../views/News.vue')
  },
  {
    path: '/point',
    name: 'PointShop',
    component: () => import('../views/PointShop.vue')
  },
  {
    path: '/point/goods/:id',
    name: 'PointGoodsDetail',
    component: () => import('../views/PointGoodsDetail.vue')
  },
  {
    path: '/goods/:id',
    name: 'GoodsDetail',
    component: () => import('../views/GoodsDetail.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.path.startsWith('/admin')) {
    if (to.path === '/admin/login') {
      if (userStore.adminToken && userStore.adminInfo?.role === 1) {
        return next('/admin')
      }
      return next()
    }
    if (!userStore.adminToken) {
      return next('/admin/login')
    }
    if (userStore.adminInfo?.role !== 1) {
      return next('/')
    }
    return next()
  }

  const authRequiredPages = ['/user']
  const authRequired = authRequiredPages.some(path => to.path.startsWith(path))
  
  if (authRequired && !userStore.token) {
    userStore.showLoginDialog = true
    if (from.path !== to.path && from.matched.length > 0) {
      return next(false)
    }
    return next('/')
  }
  next()
})

export default router
