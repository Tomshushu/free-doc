<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-blue-50 via-white to-indigo-50">
    <!-- 语言切换 -->
    <div class="fixed top-4 right-4 z-50">
      <LanguageToggle />
    </div>

    <div class="text-center mb-8">
      <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-600 shadow-lg mb-4">
        <i class="fa-solid fa-book text-white text-2xl"></i>
      </div>
      <h1 class="text-3xl font-bold text-gray-900">FreeDoc</h1>
      <p class="text-gray-500 mt-2">{{ $t('login.subtitle') }}</p>
    </div>

    <div class="w-full max-w-md px-6">
      <div class="bg-white/80 backdrop-blur-lg rounded-2xl shadow-xl p-8 border border-gray-100">
        <!-- 登录表单 -->
        <el-form v-if="isLogin" ref="loginFormRef" :model="loginForm" :rules="loginRules" @submit.prevent="handleLogin">
          <el-form-item prop="account">
            <el-input
              v-model="loginForm.account"
              :placeholder="$t('login.accountPlaceholder')"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              :placeholder="$t('login.passwordPlaceholder')"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="rememberMe">{{ $t('login.rememberMe') }}</el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="w-full"
              :loading="loading"
              @click="handleLogin"
            >
              {{ $t('login.login') }}
            </el-button>
          </el-form-item>

          <el-form-item class="text-center">
            <span class="text-gray-500">{{ $t('login.noAccount') }}</span>
            <el-button link type="primary" @click="switchMode">{{ $t('login.registerNow') }}</el-button>
          </el-form-item>
        </el-form>

        <!-- 注册表单 -->
        <el-form v-else ref="registerFormRef" :model="registerForm" :rules="registerRules" @submit.prevent="handleRegister">
          <el-form-item prop="userName">
            <el-input
              v-model="registerForm.userName"
              :placeholder="$t('login.usernamePlaceholder')"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="account">
            <el-input
              v-model="registerForm.account"
              :placeholder="$t('login.accountPlaceholder')"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              :placeholder="$t('login.passwordPlaceholder')"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              :placeholder="$t('login.confirmPasswordPlaceholder')"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleRegister"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="w-full"
              :loading="loading"
              @click="handleRegister"
            >
              {{ $t('login.register') }}
            </el-button>
          </el-form-item>

          <el-form-item class="text-center">
            <span class="text-gray-500">{{ $t('login.hasAccount') }}</span>
            <el-button link type="primary" @click="switchMode">{{ $t('login.loginNow') }}</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <p class="mt-8 text-sm text-gray-400">
      © 2026 FreeDoc. All rights reserved.
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { register } from '@/api/auth'
import LanguageToggle from '@/components/LanguageToggle.vue'
import type { FormInstance, FormRules } from 'element-plus'

const { t } = useI18n()

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isLogin = ref(true)
const loading = ref(false)
const rememberMe = ref(false)

// 登录表单
const loginFormRef = ref<FormInstance>()
const loginForm = reactive({
  account: '',
  password: ''
})

const loginRules = computed<FormRules>(() => ({
  account: [
    { required: true, message: t('login.accountPlaceholder'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('login.passwordPlaceholder'), trigger: 'blur' }
  ]
}))

// 注册表单
const registerFormRef = ref<FormInstance>()
const registerForm = reactive({
  userName: '',
  account: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error(t('login.passwordMismatch')))
  } else {
    callback()
  }
}

const registerRules = computed<FormRules>(() => ({
  userName: [
    { required: true, message: t('login.usernamePlaceholder'), trigger: 'blur' },
    { min: 2, max: 20, message: t('login.usernameLength'), trigger: 'blur' }
  ],
  account: [
    { required: true, message: t('login.accountPlaceholder'), trigger: 'blur' },
    { min: 3, max: 20, message: t('login.accountLength'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('login.passwordPlaceholder'), trigger: 'blur' },
    { min: 6, max: 20, message: t('login.passwordLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('login.confirmPasswordPlaceholder'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}))

function switchMode() {
  isLogin.value = !isLogin.value
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(loginForm.account, loginForm.password)
    ElMessage.success(t('login.loginSuccess'))
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(redirect)
  } catch (error: any) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register({
      userName: registerForm.userName,
      account: registerForm.account,
      password: registerForm.password
    })
    ElMessage.success(t('login.registerSuccess'))
    isLogin.value = true
    // 清空注册表单
    registerForm.userName = ''
    registerForm.account = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
  } catch (error: any) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
}
</style>
