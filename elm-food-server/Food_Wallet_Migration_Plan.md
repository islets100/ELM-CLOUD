# Food 与 Wallet 模块微服务迁移计划及上下文记录

## 1. 当前上下文与现状分析 🔍

我们在将 `elm-pls` (原仿“饿了吗”单体项目) 的 **Food（食品）** 和 **Wallet（钱包）** 模块重构并迁移至 `ELM` (Spring Cloud 微服务架构) 的过程中，通过检索现有的数据库脚本与同事（Cowter_il 负责 User，Wei Yufei 负责 Cart+Business）提交的代码，发现新老架构存在以下**核心级技术栈与规范差异**：

### 1.1 技术栈更迭
* **持久层框架变更**：原项目使用 `Spring Data JPA`（通过 `@Entity` 自动建表和关联管理）；新架构已全面改为 `MyBatis`，移除了 JPA 依赖，需手动编写 Mapper 接口与 SQL。
* **统一返回结构变更**：原项目的 API 统一返回封装类为 `HttpResult`；新架构中统一变更为自定义的 `CommonResult<T>`。

### 1.2 数据库与实体层改造
根据 `ELM/docker/mysql/init/01-create-tables-if-not-exists.sql` 初始脚本，表结构已发生颠覆性变化：
* **主键与类型变化**：表主键从 `Long` 普遍变更为 `INT` (如 `foodId`, `businessId`)，特别注意用户的唯一标识 `userId` 变为了 `VARCHAR(50)` 字符串。
* **外键约束移除**：数据库层面去除了外键（FOREIGN KEY），仅保留索引（KEY），这为微服务拆分和独立部署提供了基础。
* **字段规范变化**：原 `BaseEntity` 形态被废弃。例如原表示逻辑删除的字段从 `Boolean deleted` 变为了 `TINYINT valid` (默认值为 1)。原实体间强耦合（如 `@ManyToOne Business business`）需要降级为普通 ID 字段存储（如 `Integer businessId`）。

---

## 2. 核心迁移与解耦策略 💡

考虑到同事负责的 User 和 Business 模块正在开发中，可能存在接口缺失或不稳定的情况，为保证 Food 和 Wallet 的迁移测试不受阻碍，采用以下解耦方案：

### 防御性 OpenFeign 调用（带 Fallback 降级机制）
* **微服务内部通信**：跨域验证（如 Food 新增时验证 Business 存在，Wallet 支付时验证 User）不再查库，而是通过 OpenFeign 发起内部 HTTP 调用。
* **容错机制**：为所有的 Feign 客户端配置 Fallback 类。若同事的服务挂掉或未实现，触发降级逻辑，返回预设的 Mock 数据（例如“虚拟成功验证”），从而确保当前模块主流程畅通。

---

## 3. 详细执行计划 📝

决定**先从 Food 模块入手**，积累经验后再迁移业务更复杂的 Wallet 模块。

### 阶段一：Food 模块迁移 (elm-food-server)

1. **实体类（POJO）重构**
   * 在 `team.tjusw.elm.po` 包下新建 `Food` 类。
   * 移除所有 JPA 注解（`@Entity`, `@Column`, `@ManyToOne` 等）。
   * 字段类型向新 SQL 对齐：`Integer foodId`, `Integer businessId`, `Integer valid` 等。

2. **持久层（Mapper）重构**
   * 创建 `FoodMapper` 接口，替代原有的 `FoodRepository`。
   * 使用 MyBatis 注解（如 `@Select`, `@Insert`）或 XML 映射文件，实现简单的单表增删改查。
   * 特别实现逻辑删除的更新：`UPDATE food SET valid = 0 WHERE foodId = #{foodId}`。

3. **RPC 防腐层（Feign Client）搭建**
   * 引入 OpenFeign 依赖及配置。
   * 创建 `BusinessClient` 接口，定义调用 `elm-business-server` 获取商家信息的 API。
   * 创建并注册对应的 `BusinessClientFallback`，处理调用失败的降级逻辑。

4. **Service 与 Controller 适配改造**
   * 在 `elm-food-server` 编写符合新规范的 `FoodController` 和 `FoodService`。
   * 替换原 `HttpResult` 为 `CommonResult`。
   * 将参数校验、跨域查询等逻辑切换为调用 Feign Client。

**(✅ 已完成 2026-04-17)：**
* 完成了 `Food.java` POJO 剥离，将原来 JPA 的关联改为平级属性 `businessId` 等。
* 完成了 `FoodMapper.java`，使用了 Mybatis 注解 `@Select`, `@Insert`, `@Update` 进行增删改查（对删除做了逻辑删除 `valid=0` 处理），替换掉了 JPA 的 Repository。
* 编写了防腐层接口 `BusinessClient.java` 及熔断降级类 `BusinessClientFallback.java`。
* 改造完毕 `FoodService` 和 `FoodController`，统一返回 `CommonResult` 并利用 `BusinessClient` 引入了软验证。
* 启动类 `Application.java` 加入 `@EnableFeignClients` 并在项目中添加了 `spring-cloud-starter-openfeign`。
* 成功本地启动并在微服务环境（包含 Eureka、ConfigServer、Docker MySQL/Redis/RabbitMQ）中运行编译通过。

### 阶段二：Wallet 模块迁移 (elm-wallet-server)
*(待 Food 模块迁移验证跑通后启动)*

**当前代码与数据库差异分析（Wallet/VIP/Overdraft模块）：**
1. **表结构巨变与功能缺失（重要!）**
   * 原单体包含完善的 `wallets`、`wallet_transactions`、`vip_cards`、`overdraft_records` 四张核心表，包含了复杂的计息（`accumulated_interest`）和透支等逻辑。
   * **然而同事改造后的微服务 `01-create-tables-if-not-exists.sql` 脚本中**，只提供了极简的 `VirtualWallet`（字段只有 walletId, userId, balance）和 `VirtualWalletDetails`，**完全没有建 `vip_cards` 和 `overdraft_records` 表**！甚至钱包表连原本用于存放信用额度的 `credit_limit` 和 `used_credit_limit` 字段都丢了。
2. **字段类型不一致问题**
   * `VirtualWallet` 中的主键是 `INT walletId`。
   * 关联的 `userId` 从单体的 `Long` 变成了 `VARCHAR(50)` 字符串。

**Wallet 模块迁移实施计划与难度评估：**

* **难点与重点**：VIP和透支还款（`repayOverdraft`）是高度耦合的，如果同事的 SQL 不补充完整，这部分相关的所有代码会直接报错。由于这块属于你认领的范畴，我们需要主动去弥补同事在建表上的“遗漏”。
* **执行步骤**：
  1. **数据库脚本补全**：在 `docker/mysql/init/` 中的脚本里，补上缺失的 `VirtualWallet` 信用额度字段，并将老系统的 `vip_cards` 和 `overdraft_records` 表同步加入微服务。
  2. **实体类重构（向新标准看齐）**：
     * 因为老单体 Wallet 逻辑庞大，我们可以建立 `Wallet`, `WalletTransaction`, `VipCard`, `OverdraftRecord` 这四大基础 POJO，依然采用简单的 `Integer`/`String` 关联，而不是 JPA 外键关联。
**(✅ 已完成 2026-04-17)：**
* 完成了针对缺失的信用额度与 VIP 相关字段对数据库的 SQL 补丁编写 (`02-add-wallet-vip-tables.sql`) 并在 Docker MySQL 中成功执行。
* 创建了符合微服务规范的 POJO：`VipCardPO.java`、`OverdraftRecordPO.java`，并重构了原有的 `VirtualWalletPO.java` 以包含新字段。
* 编写了对应的 MyBatis 接口：`VipCardMapper.java`、`OverdraftRecordMapper.java`，并更新了 `VirtualWalletMapper.java` 确保字段持久化。

  3. **服务防腐层建立**：
     * 编写了 `UserClient` 和 `UserClientFallback`，用于 VIP 购买和 Wallet 创建时对 User 进行验证跨域调用。
  4. **全套 MyBatis Mapper 开发**：
     * 补全并更新了 `VirtualWalletMapper`、`VipCardMapper`、`OverdraftRecordMapper` 的所有增删改查逻辑。
  5. **Wallet Controller 与 Service 落位**：
     * 完成了 `VirtualWalletServiceImpl`：实现了获取或创建钱包、充值、支付（支持透支机制、通过额度判断是否创建透支记录）、还清透支等功能。
     * 完成了 `VipCardServiceImpl`：实现了购买VIP卡（自动扣款/透支、自动提升钱包透支额度）、获取有效VIP信息等。
     * 更新了 `VirtualWalletController` 与 `VipCardController` 从而对外暴露出完整的 RESTful 微服务接口。

**(✅ 已完成 2026-04-17)：**
* 解决了 powershell 下 `Out-File` 导致 UTF-8 BOM 的编译错误，清理了项目。
* 完成微服务化后的透支、充值、支付与 VIP 开通等原先位于单体的核心业务逻辑代码搭建。
* 成功执行 `mvn clean compile` 语法与依赖编译验证通过！

---

## 4. 关键注意事项 ⚠️

1. **类型陷阱**：在搬运老代码时，务必全局搜索和调整类型。原来的 `Long id` 一定要适配为 `Integer`；原来的 `Long userId` 必须改为 `String userId`，否则会导致 MyBatis 类型映射报错或数据溢出。
2. **依赖检查**：确保 `elm-food-server` 的 `pom.xml` 中剔除所有 `spring-data-jpa` 和 `hibernate` 关联的库，避免 Spring Boot 自动装配 DataSource 产生冲突。
3. **接口路径前缀**：与同事保持一致，当前对外暴露的 API 通常在 Controller 加 `@RequestMapping("/api/foods")` 或者不加统一前缀而在 Gateway 统一处理，开发时需确认约定。
4. **日志与配置**：因为用到了 Feign 和降级，需在 `application.yml` / `bootstrap.yml` 中开启 Feign 的 Hystrix/Resilience4j 支持：`feign.circuitbreaker.enabled=true` 或类似配置。

---

*文档生成日期：2026-04-17*