<template>
  <div class="redirect-page">
    <p>正在进入商家中心...</p>
  </div>
</template>

<script>
export default {
  name: 'BusinessRegister',
  created() {
    this.$router.replace('/business')
  }
}
</script>

<style scoped>
.redirect-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f5f7;
  color: #475569;
  font-size: 4vw;
}
</style>
