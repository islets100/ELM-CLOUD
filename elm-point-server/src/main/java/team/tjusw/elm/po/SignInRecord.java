package team.tjusw.elm.po;

import java.time.LocalDate;

/**
 * 签到记录实体类
 */
public class SignInRecord {
    private Integer id;
    private String userId;
    private LocalDate signDate;
    private Integer consecutiveDays;
    private Integer pointsAwarded;

    public SignInRecord() {
    }

    public SignInRecord(String userId, LocalDate signDate, Integer consecutiveDays, Integer pointsAwarded) {
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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
