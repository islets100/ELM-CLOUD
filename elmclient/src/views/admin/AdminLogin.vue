<template>
  <div class="admin-login">
    <div class="login-box">
      <h2>管理员登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-item">
          <label>用户名</label>
          <input 
            type="text" 
            v-model="loginForm.username" 
            placeholder="输入管理员账号"
            required
          >
        </div>
        <div class="form-item">
          <label>密码</label>
          <input 
            type="password" 
            v-model="loginForm.password" 
            placeholder="输入密码"
            required
          >
        </div>
        <div class="form-item remember">
          <input type="checkbox" v-model="loginForm.rememberMe" id="remember">
          <label for="remember">记住登录</label>
        </div>
        <button type="submit" class="login-btn">登录</button>
        <p class="error-tip" v-if="errorMsg">{{ errorMsg }}</p>
      </form>
    </div>
  </div>
</template>

<script>
import adminApi from '../../api/admin'
import auth from '../../utils/auth'

export default {
  name: 'AdminLogin',
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
        rememberMe: false
      },
      errorMsg: ''
    }
  },
  created() {
    // 已登录管理员直接跳管理页
    if (auth.isLoggedIn() && auth.isAdmin()) {
      this.$router.push('/admin/users')
    }
  },
  methods: {
async handleLogin() {
  try {
    const response = await adminApi.login(this.loginForm);
    const { id_token } = response.data;
    // 存入本地（对应 auth.js 中的 setToken 逻辑）
    auth.setToken(id_token);
    // 验证用户并跳转
    const userRes = await adminApi.getCurrentUser();
    auth.setUserInfo(userRes.data);
    if (auth.isAdmin()) {
      this.$router.push('/admin/users');
    } else {
      this.errorMsg = '无管理员权限';
      auth.removeToken();
    }
  } catch (error) {
    this.errorMsg = '用户名或密码错误';
  }
}
  }
}
</script>

<style scoped>
.admin-login {
  width: 100vw;
  height: 100vh;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
}
.login-box {
  width: 90%;
  max-width: 400px;
  background: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
}
h2 {
  text-align: center;
  color: #333;
  margin-bottom: 1.5rem;
}
.form-item {
  margin-bottom: 1rem;
}
.form-item label {
  display: block;
  color: #666;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}
.form-item input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 0.9rem;
}
.remember {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 1rem 0;
}
.login-btn {
  width: 100%;
  padding: 0.8rem;
  background: #0097ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
}
.login-btn:hover {
  background: #0086e5;
}
.error-tip {
  color: #ff4d4f;
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
}
</style>