import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false, speed: 400 })

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/admin/AdminLogin.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { title: '管理后台' },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/AdminDashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'goods',
        name: 'AdminGoods',
        component: () => import('../views/admin/AdminGoods.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'category',
        name: 'AdminCategory',
        component: () => import('../views/admin/AdminCategory.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'news',
        name: 'AdminNews',
        component: () => import('../views/admin/AdminNews.vue'),
        meta: { title: '资讯管理' }
      },
      {
        path: 'point-goods',
        name: 'AdminPointGoods',
        component: () => import('../views/admin/AdminPointGoods.vue'),
        meta: { title: '积分商品管理' }
      },
      {
        path: 'point-orders',
        name: 'AdminPointOrders',
        component: () => import('../views/admin/AdminPointOrders.vue'),
        meta: { title: '积分订单管理' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/AdminUsers.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/AdminOrders.vue'),
        meta: { title: '订单管理' }
      }
    ]
  },
  {
    path: '/market',
    name: 'Market',
    component: () => import('../views/Market.vue'),
    meta: { title: '闲置市场' }
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: () => import('../views/UserCenter.vue'),
    meta: { title: '个人中心' }
  },
  {
    path: '/news',
    name: 'News',
    component: () => import('../views/News.vue'),
    meta: { title: '校园资讯' }
  },
  {
    path: '/point',
    name: 'PointShop',
    component: () => import('../views/PointShop.vue'),
    meta: { title: '积分商城' }
  },
  {
    path: '/point/goods/:id',
    name: 'PointGoodsDetail',
    component: () => import('../views/PointGoodsDetail.vue'),
    meta: { title: '积分商品详情' }
  },
  {
    path: '/goods/:id',
    name: 'GoodsDetail',
    component: () => import('../views/GoodsDetail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    if (to.name === 'GoodsDetail' && from.name === 'Market') {
      return false
    }
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  NProgress.start()
  document.title = to.meta.title
    ? `${to.meta.title} - 校园闲置物品交易系统`
    : '校园闲置物品交易系统'
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

router.afterEach(() => {
  NProgress.done()
})

export default router
