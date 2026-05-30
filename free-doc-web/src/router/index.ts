import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/s/:shareCode',
      name: 'ShareEntry',
      component: () => import('@/views/share/ShareEntryView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      name: 'Workspace',
      component: () => import('@/views/workspace/Workspace.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/settings',
      name: 'SystemSettings',
      component: () => import('@/views/settings/SystemSettings.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/test/font',
      name: 'FontTest',
      component: () => import('@/views/test/FontTest.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('@/views/layout/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'doc/:docId',
          name: 'DocEdit',
          component: () => import('@/views/doc/DocEdit.vue')
        },
        {
          path: 'doc/:docId/view',
          name: 'DocView',
          component: () => import('@/views/doc/DocView.vue')
        },
        {
          path: 'search',
          name: 'Search',
          component: () => import('@/views/doc/DocSearch.vue')
        },
        {
          path: 'workspace/:projectId',
          name: 'ProjectWorkspace',
          component: () => import('@/views/project/ProjectWorkspace.vue')
        },
        {
          path: 'project/:projectId',
          name: 'ProjectSetting',
          component: () => import('@/views/project/ProjectSetting.vue')
        },
        {
          path: 'team/:teamId',
          name: 'TeamSetting',
          component: () => import('@/views/team/TeamSetting.vue')
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/settings/Profile.vue')
        }
      ]
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth !== false && !userStore.isLoggedIn) {
    next({
      path: '/login',
      query: to.fullPath === '/login' ? undefined : { redirect: to.fullPath }
    })
    return
  }

  if (to.path === '/login' && userStore.isLoggedIn) {
    next(typeof to.query.redirect === 'string' ? to.query.redirect : '/')
    return
  }

  next()
})

export default router
