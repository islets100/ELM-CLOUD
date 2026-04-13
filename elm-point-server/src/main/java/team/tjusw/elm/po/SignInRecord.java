package team.tjusw.elm.po;

import java.time.LocalDate;

/**
 * 签到记录实体类
 */
public class SignInRecord {
    private Integer id;
    private Long userId;
    private LocalDate signDate;
    private Integer consecutiveDays;
    private Integer pointsAwarded;

    public SignInRecord() {
    }

    public SignInRecord(Long userId, LocalDate signDate, Integer consecutiveDays, Integer pointsAwarded) {
        this.userId = userId;
        this.signDate = signDate;
        this.consecutiveDays = consecutiveDays;
        this.pointsAwarded = pointsAwarded;
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

    public LocalDate getSignDate() {
        return signDate;
    }

    public void setSignDate(LocalDate signDate) {
        this.signDate = signDate;
    }

    public Integer getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(Integer consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }

    public Integer getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(Integer pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }
}
