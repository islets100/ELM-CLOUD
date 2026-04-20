# ELM-CLOUD 启动指南
## 1. 启动 Eureka 服务
```bash
cd elm-eureka-server
mvn spring-boot:run
```

---

## 2. 依次启动八项服务
1. 进入对应服务文件夹
2. 执行启动命令
```bash
mvn spring-boot:run
```
3. 打开 Eureka 界面并刷新
4. 查看 **instance currently registered with eureka** 一栏，存在对应服务即启动成功

---

## 3. 启动网关服务
1. 进入网关目录
2. 执行启动命令
```bash
mvn spring-boot:run
```
3. 刷新 Eureka 查看是否注册成功

---

## 4. 启动前端应用
1. 进入前端目录
```bash
cd elm-front/clmclient
```
2. 首次运行安装依赖
```bash
npm install
```
3. 启动前端应用
```bash
npm run serve
```
4. 验证：访问 `http://localhost:8081`，看到前端页面即成功

---

## 5. 验证网关
通过网关访问后端服务示例：
```
http://localhost:14000/business/...
```