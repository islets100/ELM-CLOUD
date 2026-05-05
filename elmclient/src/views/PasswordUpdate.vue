<template>
  <div class="password-update-page">
    <nav class="top-nav">
      <button @click="$router.go(-1)" class="back-btn">← 返回</button>
      <h1>修改密码</h1>
    </nav>

    <form class="password-form" @submit.prevent="submitPasswordChange">
      <div class="form-item">
        <label>当前密码 <span class="required">*</span></label>
        <input 
          type="password" 
          v-model="passwordForm.oldPassword" 
          placeholder="请输入当前密码" 
          required
        >
      </div>

      <div class="form-item">
        <label>新密码 <span class="required">*</span></label>
        <input 
          type="password" 
          v-model="passwordForm.newPassword" 
          placeholder="请输入新密码" 
          required
          @input="validatePassword"
        >
        <span class="hint" :class="{ valid: passwordValid, invalid: !passwordValid && passwordForm.newPassword }">
          {{ passwordHint }}
        </span>
      </div>

      <div class="form-item">
        <label>确认新密码 <span class="required">*</span></label>
        <input 
          type="password" 
          v-model="passwordForm.confirmPassword" 
          placeholder="请再次输入新密码" 
          required
          @input="checkPasswordMatch"
        >
        <span class="hint" :class="{ invalid: !passwordMatch && passwordForm.confirmPassword }">
          {{ passwordForm.confirmPassword && !passwordMatch ? '两次输入的密码不一致' : '请确认新密码' }}
        </span>
      </div>

      <button type="submit" class="submit-btn" :disabled="loading || !canSubmit">
        <span v-if="loading">修改中...</span>
        <span v-else>确认修改</span>
      </button>
    </form>
  </div>
</template>

<script>
import auth from '@/utils/auth.js'
import userApi from '../api/user.js'

export default {
  name: 'PasswordUpdate',
  data() {
    return {
      loading: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordValid: false,
      passwordHint: '密码长度至少6位',
      passwordMatch: false
    }
  },
  computed: {
    // 只有密码有效且两次输入一致才能提交
    canSubmit() {
      return this.passwordValid && this.passwordMatch && 
             this.passwordForm.oldPassword && this.passwordForm.newPassword
    }
  },
  created() {
    // 验证登录状态
    if (!auth.isLoggedIn()) {
      this.$router.push('/login')
      return
    }
  },
  methods: {
    // 验证密码强度
    validatePassword() {
      const password = this.passwordForm.newPassword
      if (!password) {
        this.passwordValid = false
        this.passwordHint = '密码长度至少6位'
        return
      }
      
      // 简单密码验证，可根据需求调整
      if (password.length < 6) {
        this.passwordValid = false
        this.passwordHint = '密码长度至少6位'
      } else {
        this.passwordValid = true
        this.passwordHint = '密码格式有效'
      }
      
      // 检查密码是否匹配
      this.checkPasswordMatch()
    },
    
    // 检查两次输入的密码是否一致
    checkPasswordMatch() {
      this.passwordMatch = this.passwordForm.newPassword === this.passwordForm.confirmPassword
    },
    
    async submitPasswordChange() {
      this.loading = true
      try {
        // 获取当前用户信息
        const user = auth.getUserInfo()
        if (!user || !user.username) {
          alert('用户信息获取失败，请重新登录')
          this.$router.push('/login')
          return
        }
        
        // 调用修改密码接口
        // 注意：这里需要先验证旧密码是否正确
        // 实际应用中应该有单独的验证旧密码接口
        const response = await userApi.updatePassword({
          username: user.username,
          password: this.passwordForm.newPassword
        })
        
        if (response.data === 'Update the password successfully.') {
          alert('密码修改成功，请重新登录')
          // 密码修改成功后退出登录，让用户重新登录
          auth.logout()
          this.$router.push('/login')
        } else {
          alert('修改失败：' + (response.data || '未知错误'))
        }
      } catch (err) {
        console.error('修改密码失败', err)
        const errorMsg = err.response?.data || '网络错误，请重试'
        alert(`修改失败：${errorMsg}`)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.top-nav {
  display: flex;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #eee;
}

.back-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  margin-right: 1rem;
}

h1 {
  font-size: 1.5rem;
  color: #333;
  margin: 0;
  flex: 1;
  text-align: center;
}

.password-form {
  padding: 1.5rem;
}

.form-item {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
  font-weight: 500;
}

.required {
  color: #ff4d4f;
}

input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

input:focus {
  outline: none;
  border-color: #0097FF;
}

.hint {
  display: block;
  margin-top: 0.3rem;
  font-size: 0.85rem;
}

.hint.valid {
  color: #52c41a;
}

.hint.invalid {
  color: #ff4d4f;
}

.submit-btn {
  width: 100%;
  padding: 0.8rem;
  background-color: #0097FF;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.submit-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.submit-btn:not(:disabled):hover {
  background-color: #0078cc;
}
</style>
