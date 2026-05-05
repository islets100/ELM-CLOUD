<template>
  <div class="wrapper">
    <!-- 顶部导航：与登录页一致的样式和高度 -->
    <header>
      <p>用户注册</p>
    </header>

    <!-- 注册表单卡片：复用登录页卡片布局 -->
    <div class="register-card">
      <!-- 品牌标识：与登录页完全一致的图标和标题 -->
      <div class="brand-logo">
        <i class="fa fa-cutlery"></i>
        <h2>外卖平台</h2>
      </div>

      <!-- 表单区域：带图标输入组，保持登录页交互风格 -->
      <ul class="form-box">
        <li>
          <div class="input-group">
            <i class="fa fa-user"></i>
            <input
              type="text"
              v-model="user.username"
              placeholder="请输入用户名（登录用）"
              @input="clearError"
              @blur="validateUsernameWithHelper"
            >
          </div>
        </li>
      </ul>

      <!-- 错误提示：与登录页一致的样式和位置 -->
      <p class="error-message" v-if="errorMsg">{{ errorMsg }}</p>

      <!-- 操作按钮区：双按钮纵向布局，保持登录页交互逻辑 -->
      <div class="action-buttons">
        <button
          @click="handleRegister"
          :disabled="loading || !isUsernameValid"
          class="main-btn"
        >
          <span v-if="loading">
            <i class="fa fa-spinner fa-spin"></i> 注册中...
          </span>
          <span v-else>注册账号</span>
        </button>
        <button @click="goToLogin" class="secondary-btn">
          已有账号？立即登录
        </button>
      </div>

      <!-- 注册须知：补充用户引导，样式轻量化 -->
      <p class="register-tip">
        <i class="fa fa-info-circle"></i> 注册后初始密码为"password"，请登录后及时修改
      </p>
    </div>

    <!-- 底部组件：与登录页一致的固定定位 -->
    <Footer />
  </div>
</template>

<script>
import Footer from '../components/Footer.vue';
import axios from 'axios';
import { validateRegisterUsername } from '../utils/register';

export default {
  name: 'Register',
  components: { Footer },
  data() {
    return {
      // 表单数据：仅用户名（后端默认生成初始密码）
      user: {
        username: ''
      },
      loading: false, // 注册按钮加载状态
      errorMsg: '', // 错误提示信息
      isUsernameValid: false // 用户名合法性校验状态
    };
  },
  methods: {
    /** 清除错误提示：输入时自动清空错误 */
    clearError() {
      this.errorMsg = '';
    },

    /** 用户名合法性校验：仅保留“非空”校验（删除长度校验） */
    validateUsername() {
      const username = this.user.username.trim();
      if (!username) {
        this.errorMsg = '用户名不能为空';
        this.isUsernameValid = false;
      } 
      // （可选）若也不需要字符格式校验，可删除以下else if判断
      else if (!/^[a-zA-Z0-9_-]+$/.test(username)) {
        this.errorMsg = '用户名仅支持字母、数字、下划线和短横线';
        this.isUsernameValid = false;
      } 
      else {
        this.isUsernameValid = true;
      }
    },

    /** 核心注册逻辑：接口调用 + 状态处理 */
    async handleRegister() {
      // 二次校验：防止绕过blur事件提交
      this.validateUsernameWithHelper();
      if (!this.isUsernameValid) return;

      const username = this.user.username.trim();
      this.loading = true;

      try {
        // 调用后端注册接口（与登录页接口风格一致）
        const response = await axios.post('/api/register', {
          username: username
        });

        if (response.data?.success) {
          this.errorMsg = '';
          this.$message?.success('注册成功！初始密码为"password"') || alert('注册成功！初始密码为"password"');
          setTimeout(() => {
            this.$router.push('/login');
          }, 1500);
        } else {
          this.errorMsg = response.data?.message || '注册失败，请稍后重试';
        }
      } catch (error) {
        // 错误分类处理：与登录页错误提示逻辑统一
        console.error('注册失败:', error);
        if (error.response?.status === 409) {
          this.errorMsg = '用户名已被注册，请更换其他用户名';
        } else if (error.response?.status === 400) {
          this.errorMsg = '请求参数错误，请检查用户名格式';
        } else if (error.response?.status === 404) {
          this.errorMsg = '注册接口不可用，请稍后重试';
        } else if (error.response?.status === 500) {
          this.errorMsg = '服务器繁忙，请稍后重试';
        } else {
          this.errorMsg = '网络连接失败，请检查网络后重试';
        }
      } finally {
        this.loading = false;
      }
    },

    /** 跳转到登录页：支持从注册页快速切换 */
    validateUsernameWithHelper() {
      const result = validateRegisterUsername(this.user.username);
      this.errorMsg = result.valid ? '' : result.message;
      this.isUsernameValid = result.valid;
    },

    goToLogin() {
      this.$router.push('/login');
    }
  },
  // 监听用户名变化，实时更新校验状态（仅判断“非空”）
  watch: {
    'user.username'(val) {
      // 仅判断“非空”，删除长度相关判断
      this.isUsernameValid = validateRegisterUsername(val).valid;
    }
  }
};
</script>

<style scoped>
/* 基础布局：与登录页完全一致的外层容器样式 */
.wrapper {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
  position: relative;
  padding-bottom: 14vw; /* 预留底部菜单高度，与登录页一致 */
  box-sizing: border-box;
}

/* 顶部导航：复用登录页导航样式 */
header {
  width: 100%;
  height: 12vw;
  background-color: #0097FF;
  color: #fff;
  font-size: 4.8vw;
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 注册卡片：与登录页卡片样式完全统一 */
.register-card {
  width: 90%;
  margin: 20vw auto 0; /* 与登录页一致的顶部间距 */
  background-color: #fff;
  border-radius: 10px;
  padding: 6vw 5vw;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  box-sizing: border-box;
}

/* 品牌标识：与登录页相同的图标大小和间距 */
.brand-logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 6vw;
}

.brand-logo i {
  font-size: 15vw;
  color: #0097FF; /* 主色，与登录页一致 */
  margin-bottom: 2vw;
}

.brand-logo h2 {
  font-size: 5vw;
  color: #333;
  margin: 0;
  font-weight: 600;
}

/* 表单容器：无列表样式，与登录页一致 */
.form-box {
  list-style: none;
  padding: 0;
  margin: 0 0 4vw 0;
}

.form-box li {
  margin-bottom: 4vw; /* 输入框间距，与登录页一致 */
}

/* 带图标输入组：复用登录页输入框样式 */
.input-group {
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 3vw;
  transition: border-color 0.2s ease;
}

.input-group:focus-within {
  border-color: #0097FF; /* 聚焦时主色边框，与登录页一致 */
}

.input-group i {
  font-size: 4vw;
  color: #999;
  margin-right: 3vw;
}

.input-group input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 3.5vw;
  color: #333;
  padding: 1vw 0;
  box-sizing: border-box;
}

.input-group input::placeholder {
  color: #ccc;
  font-size: 3.5vw;
}

/* 错误提示：与登录页一致的颜色和位置 */
.error-message {
  color: #ff4d4f; /* 错误色，与登录页统一 */
  font-size: 3vw;
  text-align: center;
  margin: 0 0 4vw 0;
  min-height: 4vw; /* 防止布局跳动 */
  line-height: 1.5;
}

/* 操作按钮区：纵向布局，与登录页按钮样式统一 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 3vw; /* 按钮间距，与登录页一致 */
  margin-bottom: 4vw;
}

.main-btn,
.secondary-btn {
  width: 100%;
  height: 12vw;
  border-radius: 6px;
  border: none;
  font-size: 4vw;
  font-weight: bold;
  outline: none;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 2vw; /* 图标与文字间距 */
  transition: background-color 0.2s ease;
}

/* 主按钮（注册）：主色，与登录页登录按钮一致 */
.main-btn {
  background-color: #0097FF;
  color: white;
}

.main-btn:disabled {
  background-color: #99ccff; /* 禁用色，与登录页一致 */
  cursor: not-allowed;
}

/* 次要按钮（去登录）：灰色背景，与登录页注册按钮一致 */
.secondary-btn {
  background-color: #f5f5f5;
  color: #666;
}

.secondary-btn:hover {
  background-color: #eee; /* hover反馈，与登录页一致 */
}

/* 注册须知：轻量化提示，补充用户引导 */
.register-tip {
  font-size: 2.8vw;
  color: #666;
  text-align: center;
  margin: 0;
  padding-top: 2vw;
  border-top: 1px solid #f5f5f5;
}

.register-tip i {
  color: #0097FF;
  margin-right: 1vw;
}

/* 底部组件：固定定位，与登录页样式统一 */
::v-deep .footer {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  z-index: 99;
}

/* 响应式适配：确保小屏显示正常 */
@media (min-width: 768px) {
  .wrapper {
    padding-bottom: 80px;
  }

  header {
    height: 60px;
    font-size: 24px;
  }

  .register-card {
    width: 500px;
    margin-top: 120px;
    padding: 30px 25px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  }

  .brand-logo {
    margin-bottom: 30px;
  }

  .brand-logo i {
    font-size: 70px;
    margin-bottom: 10px;
  }

  .brand-logo h2 {
    font-size: 24px;
  }

  .form-box {
    margin-bottom: 20px;
  }

  .form-box li {
    margin-bottom: 20px;
  }

  .input-group {
    padding: 15px;
  }

  .input-group i {
    font-size: 18px;
    margin-right: 15px;
  }

  .input-group input {
    font-size: 16px;
    padding: 5px 0;
  }

  .input-group input::placeholder {
    font-size: 16px;
  }

  .error-message {
    font-size: 14px;
    margin-bottom: 20px;
    min-height: 20px;
  }

  .action-buttons {
    gap: 15px;
    margin-bottom: 20px;
  }

  .main-btn,
  .secondary-btn {
    height: 50px;
    font-size: 16px;
    gap: 10px;
  }

  .register-tip {
    font-size: 12px;
    padding-top: 10px;
  }
}
</style>
