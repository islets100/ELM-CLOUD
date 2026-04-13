package team.tjusw.elm.po;

import java.time.LocalDateTime;

/**
 * 积分实体类
 */
public class Integral {
    private Integer id;
    private Long userId;
    private Integer amount;
    private String type;  // INCREASE-增加, DECREASE-减少
    private String status;  // AVAILABLE-可用, USED-已使用, EXPIRED-已过期
    private String channel;  // 积分渠道（获得或消费）
    private LocalDateTime expireTime;
    private Long businessId;
    private String description;
    private Integer remainingAmount;
    private LocalDateTime createTime;

    // 积分类型常量
    public static final String TYPE_INCREASE = "INCREASE";
    public static final String TYPE_DECREASE = "DECREASE";

    // 积分状态常量
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_USED = "USED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    // 积分获得途径常量
    public static final String CHANNEL_CONSUMPTION = "CONSUMPTION";  // 消费获取
    public static final String CHANNEL_ACTIVITY = "ACTIVITY";  // 活动奖励
    public static final String CHANNEL_COMMENT = "COMMENT";  // 评论奖励
    public static final String CHANNEL_PICTURE = "PICTURE";  // 评论图片奖励
    public static final String CHANNEL_CHECK_IN = "CHECK_IN";  // 签到奖励
    public static final String CHANNEL_PACKET = "PACKET";  // 红包奖励
    public static final String CHANNEL_VIP_LEVEL = "VIP_LEVEL";  // 会员奖励
    public static final String CHANNEL_BIRTHDAY = "BIRTHDAY";  // 生日奖励
    public static final String CHANNEL_OTHER = "OTHER";  // 其他

    // 积分消费途径常量
    public static final String CHANNEL_EXCHANGE = "EXCHANGE";  // 积分兑换
    public static final String CHANNEL_ORDER_DEDUCTION = "ORDER_DEDUCTION";  // 订单抵扣

    public Integral() {
    }

    public Integral(Long userId, Integer amount, String type, String status,
                   String channel, LocalDateTime expireTime,
                   Long businessId, String description) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.channel = channel;
        this.expireTime = expireTime;
        this.businessId = businessId;
        this.description = description;
        this.createTime = LocalDateTime.now();
    }

    // Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(Integer remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
