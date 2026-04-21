<template>
  <div class="business-register">
    <!-- 头部：保持统一导航栏样式 -->
    <header>
      <p>商家注册</p>
    </header>
    
    <!-- 表单区域：与登录/注册页风格一致 -->
    <div class="form-container">
      <!-- 表单标题与说明 -->
      <div class="form-header">
        <h3>完善店铺信息</h3>
        <p class="form-desc">请填写以下信息完成商家注册</p>
      </div>
      
      <!-- 表单列表：复用登录页表单布局 -->
      <ul class="form-list">
        <!-- 店铺信息组（商家特有） -->
        <li class="group-title">店铺基本信息</li>
        <li>
          <div class="form-label">店铺名称</div>
          <div class="form-content">
            <input type="text" v-model="businessInfo.name" placeholder="请输入店铺名称">
          </div>
        </li>
        <li>
          <div class="form-label">店铺地址</div>
          <div class="form-content">
            <input type="text" v-model="businessInfo.address" placeholder="请输入详细地址">
          </div>
        </li>
        <li>
          <div class="form-label">联系电话</div>
          <div class="form-content">
            <input type="tel" v-model="businessInfo.contactPhone" placeholder="请输入店铺联系电话">
          </div>
        </li>
        
        <li class="group-title">配送信息</li>
        <li>
          <div class="form-label">起送价</div>
          <div class="form-content">
            <input type="number" v-model="businessInfo.minPrice" placeholder="请输入起送价（元）">
          </div>
        </li>
        <li>
          <div class="form-label">配送费</div>
          <div class="form-content">
            <input type="number" v-model="businessInfo.deliveryFee" placeholder="请输入配送费（元）">
          </div>
        </li>
        
        <li class="group-title">店铺介绍</li>
        <li>
          <div class="form-label">店铺简介</div>
          <div class="form-content">
            <textarea v-model="businessInfo.desc" placeholder="请简要介绍店铺特色"></textarea>
          </div>
        </li>
        
        <!-- 营业执照上传 -->
        <li>
          <div class="form-label">营业执照</div>
          <div class="form-content">
            <div class="upload-area">
              <i class="fa fa-upload"></i>
              <p>点击上传营业执照照片</p>
            </div>
          </div>
        </li>
      </ul>
      
      <!-- 按钮区域：与其他页面按钮风格统一 -->
      <div class="btn-group">
        <button class="cancel-btn" @click="$router.go(-1)">取消</button>
        <button class="submit-btn" @click="handleSubmit">完成注册</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BusinessRegister',
  data() {
    return {
      // 从普通注册页传递的手机号（用于关联账号）
      phone: this.$route.query.phone || '',
      // 店铺信息（与BusinessManage.vue保持字段一致）
      businessInfo: {
        name: '',           // 店铺名称
        address: '',        // 店铺地址
        contactPhone: '',   // 联系电话
        minPrice: 0,        // 起送价
        deliveryFee: 0,     // 配送费
        desc: '',           // 店铺简介
        businessImg: ''     // 店铺图片（注册时可不上传）
      }
    }
  },
  methods: {
    handleSubmit() {
      // 表单验证
      if (!this.businessInfo.name) {
        alert('请输入店铺名称');
        return;
      }
      if (!this.businessInfo.address) {
        alert('请输入店铺地址');
        return;
      }
      if (!this.businessInfo.contactPhone) {
        alert('请输入联系电话');
        return;
      }
      if (this.businessInfo.minPrice < 0) {
        alert('起送价不能为负数');
        return;
      }
      if (this.businessInfo.deliveryFee < 0) {
        alert('配送费不能为负数');
        return;
      }
      
      // 准备提交给后端的数据（包含关联的手机号）
      const submitData = {
        phone: this.phone,
        ...this.businessInfo
      };
      
      // 后端接口调用（已注释，待开发）
      /*
      this.$axios.post('BusinessController/registerBusiness', this.$qs.stringify(submitData))
        .then(response => {
          if (response.data > 0) {
            alert('商家注册成功！即将跳转到登录页');
            this.$router.push('/login');
          } else {
            alert('注册失败，请重试');
          }
        })
        .catch(error => {
          console.error('注册失败', error);
          alert('网络错误，请稍后重试');
        });
      */
      
      // 模拟注册成功（开发阶段用）
      alert('商家注册成功！即将跳转到登录页');
      this.$router.push('/login');
    }
  }
}
</script>

<style scoped>
/* 保持原有样式不变，与其他页面风格统一 */
.business-register {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
  box-sizing: border-box;
  padding-bottom: 8vw;
}

header {
  width: 100%;
  height: 12vw;
  background-color: #0097FF;
  color: #fff;
  font-size: 4.8vw;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  display: flex;
  justify-content: center;
  align-items: center;
}

.form-container {
  padding-top: 15vw;
  padding-left: 3vw;
  padding-right: 3vw;
}

.form-header {
  text-align: center;
  padding: 5vw 0;
}

.form-header h3 {
  font-size: 4.5vw;
  color: #333;
  margin-bottom: 2vw;
}

.form-desc {
  font-size: 3vw;
  color: #666;
}

.form-list {
  background-color: white;
  border-radius: 3vw;
  overflow: hidden;
  margin-bottom: 5vw;
}

.group-title {
  background-color: #f9f9f9;
  color: #999;
  font-size: 3vw;
  padding: 3vw;
  border-bottom: 1px solid #eee;
}

.form-list li {
  display: flex;
  padding: 3vw;
  border-bottom: 1px solid #eee;
  align-items: flex-start;
}

.form-label {
  flex: 0 0 22vw;
  font-size: 3.5vw;
  color: #666;
  padding-top: 1.5vw;
}

.form-content {
  flex: 1;
}

.form-content input,
.form-content textarea {
  width: 100%;
  border: none;
  outline: none;
  font-size: 3.5vw;
  color: #333;
  padding: 1.5vw 0;
  background-color: transparent;
}

.form-content textarea {
  min-height: 15vw;
  resize: none;
  line-height: 1.5;
}

.upload-area {
  width: 100%;
  height: 25vw;
  border: 1px dashed #ccc;
  border-radius: 2vw;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #999;
  margin-top: 1vw;
}

.upload-area i {
  font-size: 8vw;
  margin-bottom: 2vw;
}

.upload-area p {
  font-size: 3vw;
}

.btn-group {
  display: flex;
  gap: 3vw;
}

.cancel-btn, .submit-btn {
  flex: 1;
  height: 12vw;
  border: none;
  border-radius: 4px;
  font-size: 3.8vw;
  font-weight: bold;
}

.cancel-btn {
  background-color: #eee;
  color: #666;
}

.submit-btn {
  background-color: #38CA73;
  color: white;
}
</style>
