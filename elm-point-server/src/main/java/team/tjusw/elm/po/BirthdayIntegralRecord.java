package team.tjusw.elm.po;

import java.time.LocalDate;

/**
 * 生日积分记录实体类，用于记录用户每天获得的生日积分，防止重复获取
 */
public class BirthdayIntegralRecord {
    private Integer id;
    private Long userId;
    private LocalDate recordDate;
    private Boolean monthlyEarned;
    private Boolean birthdayEarned;
    private Integer monthlyPoints;
    private Integer birthdayPoints;

    public BirthdayIntegralRecord() {
        this.monthlyEarned = false;
        this.birthdayEarned = false;
        this.monthlyPoints = 0;
        this.birthdayPoints = 0;
    }

    public BirthdayIntegralRecord(Long userId, LocalDate recordDate) {
        this.userId = userId;
        this.recordDate = recordDate;
        this.monthlyEarned = false;
        this.birthdayEarned = false;
        this.monthlyPoints = 0;
        this.birthdayPoints = 0;
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

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public Boolean getMonthlyEarned() {
        return monthlyEarned;
    }

    public void setMonthlyEarned(Boolean monthlyEarned) {
        this.monthlyEarned = monthlyEarned;
    }

    public Boolean getBirthdayEarned() {
        return birthdayEarned;
    }

    public void setBirthdayEarned(Boolean birthdayEarned) {
        this.birthdayEarned = birthdayEarned;
    }

    public Integer getMonthlyPoints() {
        return monthlyPoints;
    }

    public void setMonthlyPoints(Integer monthlyPoints) {
        this.monthlyPoints = monthlyPoints;
    }

    public Integer getBirthdayPoints() {
        return birthdayPoints;
    }

    public void setBirthdayPoints(Integer birthdayPoints) {
        this.birthdayPoints = birthdayPoints;
    }
}
