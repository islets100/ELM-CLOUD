<template>
  <div class="add-user-page">
    <nav class="top-nav">
      <button @click="$router.go(-1)" class="back-btn">← 返回</button>
      <h1>新增用户</h1>
    </nav>

    <form class="add-user-form" @submit.prevent="submitUser">
      <div class="form-item">
        <label>用户名 <span class="required">*</span></label>
        <input 
          type="text" 
          v-model="userForm.username" 
          placeholder="输入登录用户名"
          required
          maxlength="50"
        >
        <span class="hint">1-50个字符</span>
      </div>
      <div class="form-item">
        <label>用户类型 <span class="required">*</span></label>
        <select v-model="userForm.role" required>
          <option value="USER">普通用户（顾客）</option>
          <option value="BUSINESS">店主</option>
        </select>
      </div>
      <div class="form-item">
        <label>姓氏 <span class="required">*</span></label>
        <input 
          type="text" 
          v-model="userForm.lastName" 
          placeholder="输入姓氏"
          required
        >
      </div>
      <div class="form-item">
        <label>名字 <span class="required">*</span></label>
        <input 
          type="text" 
          v-model="userForm.firstName" 
          placeholder="输入名字"
          required
        >
      </div>
      <div class="form-item">
        <label>联系电话 <span class="required">*</span></label>
        <input 
          type="tel" 
          v-model="userForm.phone" 
          placeholder="输入联系电话"
          required
        >
      </div>
      <div class="form-item">
        <label>地址</label>
        <input 
          type="text" 
          v-model="userForm.address" 
          placeholder="输入详细地址"
        >
      </div>
      <div class="form-item">
        <label>电子邮箱</label>
        <input 
          type="email" 
          v-model="userForm.email" 
          placeholder="输入电子邮箱"
        >
      </div>
	
      <div class="form-tip">
        注：用户初始密码为 <span class="pwd-tip">password</span>，建议首次登录后修改
      </div>
      <button type="submit" class="submit-btn" :disabled="loading">
        <span v-if="loading">创建中...</span>
        <span v-else>创建用户</span>
      </button>
    </form>
  </div>
</template>

<script>
import adminApi from '../../api/admin'
import auth from '../../utils/auth'
import axios from 'axios'

export default {
  name: 'AddUser',
  data() {
    return {
      loading: false,
      userForm: {
        username: '',
        role: 'USER', // 默认普通用户
        lastName: '',
        firstName: '',
        phone: '',
        email: '',
        address: ''
      }
    }
  },
  created() {
    // 权限校验
    if (!auth.isLoggedIn() || !auth.isAdmin()) {
      this.$router.push('/admin/login')
      return
    }

    const token = auth.getToken()
    if (!token) {
      alert('登录状态失效，请重新登录')
      this.$router.push('/admin/login')
    }
  },
  methods: {
    async submitUser() {
      // 前端基础校验
      const { username, lastName, firstName, phone, role } = this.userForm
      if (!username.trim()) {
        alert('请输入用户名');
        return;
      }
      if (username.length > 50) {
        alert('用户名不能超过50个字符');
        return;
      }
      if (!lastName.trim() || !firstName.trim()) {
        alert('姓氏和名字为必填项');
        return;
      }
      if (!phone.trim()) {
        alert('联系电话为必填项');
        return;
      }

      this.loading = true;
      try {
        const token = auth.getToken()
        // 权限强化：严格根据用户类型绑定权限，避免混淆
        const authority = role === 'BUSINESS' 
          ? 'BUSINESS'  // 店主强制绑定BUSINESS权限
          : 'USER';     // 普通用户绑定USER权限

        const personData = {
          username: username.trim(),
          activated: true,
          authorities: [{ name: authority }], // 权限与用户类型严格对应
          lastName: lastName.trim(),
          firstName: firstName.trim(),
          phone: phone.trim(),
          email: (this.userForm.email || '').trim(),
          address: (this.userForm.address || '').trim()
        };

        console.log('提交的用户数据（含权限）:', personData)

        const response = await axios.post('/api/persons', personData, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        if (response.status === 200) {
          alert('用户创建成功！');
          this.$router.push('/admin/users');
        } else {
          throw new Error(`创建失败，响应异常: ${JSON.stringify(response.data)}`)
        }
      } catch (err) {
        console.error('创建用户错误:', {
          status: err.response?.status,
          data: err.response?.data,
          message: err.message
        });

        let errorMsg = '创建用户失败'
        if (err.response?.data?.message) {
          errorMsg = err.response.data.message
        } else if (err.response?.status === 403) {
          errorMsg = '没有权限创建该类型用户'
        } else if (err.response?.status === 400) {
          errorMsg = '输入数据格式错误'
        }
        
        alert(errorMsg);
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
/* 样式保持不变 */
.hint {
  display: block;
  font-size: 0.8rem;
  color: #999;
  margin-top: 0.3rem;
}

.add-user-page {
  padding: 1.5rem;
  max-width: 600px;
  margin: 0 auto;
  background-color: #f9f9f9;
  min-height: 100vh;
}

.top-nav {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
  margin-bottom: 1.5rem;
}

.back-btn {
  background: transparent;
  border: none;
  color: #0097ff;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.5rem;
}

.top-nav h1 {
  color: #333;
  font-size: 1.2rem;
  flex: 1;
  text-align: center;
  margin: 0;
}

.add-user-form {
  background: #fff;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.form-item {
  margin-bottom: 1.2rem;
}

.form-item label {
  display: block;
  color: #666;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.required {
  color: #ff4d4f;
}

.form-item input, .form-item select {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  font-size: 0.9rem;
  box-sizing: border-box;
}

.form-tip {
  color: #999;
  font-size: 0.85rem;
  margin: 1rem 0;
  padding: 0.8rem;
  background: #f9f9f9;
  border-radius: 4px;
}

.pwd-tip {
  color: #ff4d4f;
  font-weight: bold;
}

.submit-btn {
  width: 100%;
  padding: 0.8rem;
  background: #38ca73;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
}

.submit-btn:disabled {
  background: #99d8b4;
  cursor: not-allowed;
}

.submit-btn:hover:not(:disabled) {
  background: #2db366;
}
</style>
