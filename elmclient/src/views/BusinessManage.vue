<template>
  <div class="business-manage">
    <!-- 头部：保持一致的导航栏 -->
    <header>
      <p>店铺信息管理</p>
    </header>

    <!-- 多店铺切换：若当前商家名下有多家店铺，提供切换入口 -->
    <div v-if="businesses && businesses.length > 1" class="business-switcher">
      <span class="label">当前店铺：</span>
      <button
        v-for="biz in businesses"
        :key="biz.id"
        class="biz-tag"
        :class="{ active: biz.id === businessId }"
        @click="switchBusiness(biz)"
      >
        {{ biz.businessName }}
      </button>
    </div>
    
    <!-- 表单区域：与登录/注册页表单风格统一 -->
    <div class="form-container">
      <div class="avatar-upload">
        <img :src="businessLogo" alt="店铺logo" class="logo-img">
        <button class="upload-btn" @click="handleLogoUpload">更换头像</button>
        <!-- 隐藏的文件上传输入框 -->
        <input type="file" ref="logoFile" accept="image/*" @change="handleFileChange" class="hidden-file">
      </div>
      
      <ul class="form-list">
        <li>
          <div class="form-label">店铺名称</div>
          <div class="form-content">
            <input type="text" v-model="businessInfo.name" placeholder="请输入店铺名称">
          </div>
        </li>
        <li>
          <div class="form-label">联系电话</div>
          <div class="form-content">
            <input type="tel" v-model="businessInfo.phone" placeholder="请输入联系电话">
          </div>
        </li>
        <li>
          <div class="form-label">店铺地址</div>
          <div class="form-content">
            <input type="text" v-model="businessInfo.address" placeholder="请输入店铺地址">
          </div>
        </li>
        <li>
          <div class="form-label">起送价</div>
          <div class="form-content">
            <input type="number" v-model="businessInfo.minPrice" placeholder="请输入起送价" min="0">
          </div>
        </li>
        <li>
          <div class="form-label">配送费</div>
          <div class="form-content">
            <input type="number" v-model="businessInfo.deliveryFee" placeholder="请输入配送费" min="0">
          </div>
        </li>
        <li>
          <div class="form-label">店铺简介</div>
          <div class="form-content">
            <textarea v-model="businessInfo.desc" placeholder="请输入店铺简介" rows="3"></textarea>
          </div>
        </li>
      </ul>
      
      <button class="save-btn" @click="saveInfo">保存修改</button>
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>
    
    <!-- 底部导航：与其他页面统一 -->
    <div class="merchant-footer">
      <div class="footer-item" @click="$router.push('/business')">
        <i class="fa fa-home"></i>
        <span>首页</span>
      </div>
      <div class="footer-item" @click="$router.push('/businessOrders')">
        <i class="fa fa-list-alt"></i>
        <span>订单</span>
      </div>
      <div class="footer-item" @click="$router.push('/wallet')">
        <i class="fa fa-credit-card"></i>
        <span>钱包</span>
      </div>
      <div class="footer-item active" @click="$router.push('/businessManage')">
        <i class="fa fa-user"></i>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script>
import defaultLogo from '../assets/sj01.png'

export default {
  name: 'BusinessManage',
  data() {
    return {
      // 默认店铺 logo（打包为静态资源，避免 404）
      businessLogo: defaultLogo,
      logoFile: null, // 选中的logo文件
      // 当前商家名下的全部店铺
      businesses: [],
      businessInfo: {
        name: '我的测试店铺',
        phone: '13800138000',
        address: '测试市测试区测试路123号',
        minPrice: 20,
        deliveryFee: 5,
        desc: '这是一家测试用的店铺，提供各种美味食品'
      },
      businessId: '' // 商家ID（从后端商家接口获取）
    }
  },
  created() {
    // 页面加载时获取当前登录商家的全部店铺，并默认选中第一家
    this.initBusinesses();
  },
  methods: {
    // 初始化：获取当前商家的全部店铺列表，并默认选中第一家
    initBusinesses() {
      this.$axios.get('/api/businesses/my-businesses')
        .then(res => {
          if (res.data.success && Array.isArray(res.data.data) && res.data.data.length > 0) {
            this.businesses = res.data.data;
            // 默认选中第一家店铺
            this.setCurrentBusiness(this.businesses[0]);
          } else {
            alert('您还没有创建店铺，请先创建店铺');
          }
        })
        .catch(error => {
          console.error('获取店铺列表失败:', error);
          const errorMsg = error.response?.data?.message || error.message || '网络错误，无法加载店铺列表';
          alert('获取店铺列表失败：' + errorMsg);
        });
    },

    // 根据传入的店铺实体，填充当前编辑的店铺信息
    setCurrentBusiness(business) {
      if (!business || !business.id) {
        return;
      }
      this.businessId = business.id;
      this.businessInfo = {
        name: business.businessName || '',
        phone: business.contactPhone || '',
        address: business.businessAddress || '',
        minPrice: business.startPrice ? Number(business.startPrice) : 0,
        deliveryFee: business.deliveryPrice ? Number(business.deliveryPrice) : 0,
        desc: business.businessExplain || ''
      };
      // 如果后端已保存 logo（Base64 或 URL），优先使用；否则使用本地默认图
      this.businessLogo = business.businessImg ? business.businessImg : defaultLogo;
    },

    // 切换正在编辑的店铺
    switchBusiness(biz) {
      if (!biz || !biz.id || biz.id === this.businessId) {
        return;
      }
      this.setCurrentBusiness(biz);
    },
    // 触发logo文件选择
    handleLogoUpload() {
      this.$refs.logoFile.click();
    },

    // 处理logo文件选择（预览+存储文件）
    handleFileChange(e) {
      const file = e.target.files[0];
      if (file) {
        // 校验文件类型和大小（避免无效上传）
        const isImage = file.type.startsWith('image/');
        const isLt2M = file.size / 1024 / 1024 < 2;
        if (!isImage) {
          alert('请选择图片格式文件（JPG/PNG等）');
          return;
        }
        if (!isLt2M) {
          alert('图片大小不能超过2MB');
          return;
        }
        // 存储选中的文件
        this.logoFile = file;
        // 即时预览图片
        const reader = new FileReader();
        reader.onload = (event) => {
          this.businessLogo = event.target.result;
        };
        reader.readAsDataURL(file);
      }
    },

    // 保存店铺信息到后端（含logo上传）
    saveInfo() {
      // 1. 表单基础验证
      if (!this.businessInfo.name.trim()) {
        alert('请输入店铺名称');
        return;
      }
      if (!this.businessInfo.phone.trim() || !/^1[3-9]\d{9}$/.test(this.businessInfo.phone)) {
        alert('请输入正确的11位联系电话');
        return;
      }
      if (!this.businessInfo.address.trim()) {
        alert('请输入店铺地址');
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

      // 2. 构建 FormData（后端 @PutMapping 接收 MultipartFile）
      const formData = new FormData();
      formData.append('businessName', this.businessInfo.name);
      formData.append('businessAddress', this.businessInfo.address);
      formData.append('businessExplain', this.businessInfo.desc);
      formData.append('startPrice', this.businessInfo.minPrice);
      formData.append('deliveryPrice', this.businessInfo.deliveryFee);
      // 仅当选择了新 logo 时才上传文件，后端会把图片转成 Base64 存到 businessImg
      if (this.logoFile) {
        formData.append('businessImg', this.logoFile);
      }

      // 3. 调用后端更新接口（PUT + multipart/form-data）
      this.$axios.put(`/api/businesses/${this.businessId}`, formData)
        .then(response => {
          // 后端返回的是HttpResult对象，通过success字段判断是否成功
          if (response.data.success) {
            alert('店铺信息保存成功！');
            // 保存成功后刷新当前店铺信息（从最新数据重新填充表单）
            const updated = response.data.data || null;
            if (updated && updated.id === this.businessId) {
              this.setCurrentBusiness(updated);
            } else {
              // 如果返回的数据不完整，则重新拉取店铺列表
              this.initBusinesses();
            }
            this.logoFile = null; // 清空已上传的文件缓存
          } else {
            alert('保存失败：' + (response.data.message || '系统错误'));
          }
        })
        .catch(error => {
          console.error('保存店铺信息失败:', error);
          const errorMsg = error.response?.data?.message || error.message || '网络错误，请检查网络后重试';
          alert('保存失败：' + errorMsg);
        });
    },

    // 商家退出登录
    handleLogout() {
      if (confirm('确定要退出登录吗？')) {
        // 导入 auth 工具类
        const auth = require('../utils/auth').default;
        // 清除登录状态（Token 和用户信息）
        auth.logout();
        // 跳转到登录页
        this.$router.push('/login');
      }
    }
  }
}
</script>

<style scoped>
/* 页面整体容器：适配移动端，底部留足导航栏空间 */
.business-manage {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 15vw; /* 底部导航栏高度，避免内容被遮挡 */
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 头部导航：固定顶部，统一蓝色风格 */
header {
  width: 100%;
  height: 12vw; /* 移动端适配高度 */
  background-color: #0097FF;
  color: #ffffff;
  font-size: 4.8vw;
  font-weight: 500;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100; /* 确保在最上层，不被遮挡 */
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0;
  padding: 0;
}

/* 多店铺切换条 */
.business-switcher {
  position: fixed;
  top: 12vw; /* 紧贴头部导航下方 */
  left: 0;
  width: 100%;
  padding: 2.5vw 4vw;
  box-sizing: border-box;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 2vw;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
  z-index: 90;
}

.business-switcher .label {
  font-size: 3.2vw;
  color: #666666;
}

.biz-tag {
  border: 1px solid #0097FF;
  border-radius: 999px;
  padding: 1vw 3.2vw;
  background-color: #ffffff;
  color: #0097FF;
  font-size: 3.2vw;
  cursor: pointer;
}

.biz-tag.active {
  background-color: #0097FF;
  color: #ffffff;
}

/* 表单容器：与登录页间距统一，顶部避开头部导航和切换条 */
.form-container {
  width: 100%;
  padding: 22vw 0 5vw; /* 顶部：头部+切换条高度，避免被遮挡 */
  box-sizing: border-box;
}

/* 头像上传区域：白色背景，居中布局 */
.avatar-upload {
  width: 100%;
  background-color: #ffffff;
  padding: 5vw 0;
  margin-bottom: 3vw; /* 与下方表单间距 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05); /* 轻微阴影，提升层次感 */
}

/* 店铺logo图片：圆形裁剪，固定大小 */
.logo-img {
  width: 25vw;
  height: 25vw;
  border-radius: 50%; /* 圆形头像 */
  object-fit: cover; /* 图片填充方式，避免拉伸 */
  border: 2px solid #f0f0f0;
  margin-bottom: 3vw;
  background-color: #fafafa;
}

/* 更换头像按钮：统一蓝色，圆角风格 */
.upload-btn {
  width: 30vw;
  height: 8vw;
  background-color: #0097FF;
  color: #ffffff;
  border: none;
  border-radius: 4vw; /* 大圆角，更美观 */
  font-size: 3.2vw;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease; /* 点击过渡效果 */
}
.upload-btn:hover {
  background-color: #0086E5; /* hover时加深颜色 */
}

/* 隐藏的文件上传框：仅通过按钮触发 */
.hidden-file {
  display: none;
}

/* 表单列表：白色背景，统一间距 */
.form-list {
  width: 100%;
  background-color: #ffffff;
  margin: 0 0 3vw;
  padding: 0;
  list-style: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 表单列表项：横向布局，底部边框分隔 */
.form-list li {
  display: flex;
  align-items: center;
  padding: 3.5vw 3vw; /* 上下3.5vw，左右3vw间距 */
  border-bottom: 1px solid #f0f0f0; /* 灰色分隔线 */
  box-sizing: border-box;
}
/* 最后一项去掉底部边框 */
.form-list li:last-child {
  border-bottom: none;
}

/* 表单标签：固定宽度，灰色文字 */
.form-label {
  flex: 0 0 22vw; /* 固定宽度，不缩放 */
  font-size: 3.5vw;
  color: #666666;
  text-align: left;
}

/* 表单内容区：占满剩余宽度 */
.form-content {
  flex: 1;
  text-align: left;
}

/* 输入框样式：无边框，统一字体 */
.form-content input,
.form-content textarea {
  width: 100%;
  border: none;
  outline: none; /* 去掉聚焦时的默认边框 */
  font-size: 3.5vw;
  color: #333333;
  padding: 1vw 0;
  background-color: transparent; /* 透明背景，融入表单 */
}

/* 文本域样式：禁止拉伸，固定最小高度 */
.form-content textarea {
  min-height: 15vw;
  resize: none; /* 禁止用户拉伸 */
  line-height: 1.5; /* 行高优化，提升可读性 */
}

/* 输入框占位符样式：统一浅灰色 */
.form-content input::placeholder,
.form-content textarea::placeholder {
  color: #cccccc;
  font-weight: normal;
}

/* 保存按钮：绿色，宽屏设计 */
.save-btn {
  width: 94%; /* 左右各3%间距 */
  height: 12vw;
  background-color: #38CA73;
  color: #ffffff;
  border: none;
  border-radius: 6vw; /* 大圆角 */
  font-size: 3.8vw;
  font-weight: bold;
  margin: 0 3% 3vw;
  cursor: pointer;
  transition: background-color 0.2s ease;
}
.save-btn:hover {
  background-color: #2FB863; /* hover加深 */
}

/* 退出按钮：灰色，与保存按钮区分 */
.logout-btn {
  width: 94%;
  height: 12vw;
  background-color: #eeeeee;
  color: #666666;
  border: none;
  border-radius: 6vw;
  font-size: 3.8vw;
  font-weight: bold;
  margin: 0 3%;
  cursor: pointer;
  transition: background-color 0.2s ease;
}
.logout-btn:hover {
  background-color: #e0e0e0; /* hover加深 */
}

/* 底部导航栏：固定底部，白色背景 */
.merchant-footer {
  width: 100%;
  height: 15vw; /* 固定高度 */
  background-color: #ffffff;
  border-top: 1px solid #f0f0f0;
  position: fixed;
  bottom: 0;
  left: 0;
  z-index: 99;
  display: flex;
  justify-content: space-around; /* 均匀分布 */
  align-items: center;
  box-sizing: border-box;
}

/* 底部导航项：纵向布局，统一大小 */
.footer-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-size: 3vw;
  color: #666666;
  cursor: pointer;
  transition: color 0.2s ease;
}

/* 导航图标：比文字大，统一大小 */
.footer-item i {
  font-size: 5vw;
  margin-bottom: 1vw; /* 图标与文字间距 */
}

/* 活跃状态（当前页）：蓝色高亮 */
.footer-item.active {
  color: #0097FF;
}
</style>商家管理也改改，要确保数据的前后端传递