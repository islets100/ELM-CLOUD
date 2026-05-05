package team.tjusw.elm.dto;

/**
 * 积分统计信息DTO
 */
public class IntegralStatisticsDTO {
    private Integer totalIntegral;
    private Integer availableIntegral;
    private Integer expiredIntegral;
    private Integer usedIntegral;

    // Getter and Setter methods
    public Integer getTotalIntegral() {
        return totalIntegral;
    }

    public void setTotalIntegral(Integer totalIntegral) {
        this.totalIntegral = totalIntegral;
    }

    public Integer getAvailableIntegral() {
        return availableIntegral;
    }

    public void setAvailableIntegral(Integer availableIntegral) {
        this.availableIntegral = availableIntegral;
    }

    public Integer getExpiredIntegral() {
        return expiredIntegral;
    }

    public void setExpiredIntegral(Integer expiredIntegral) {
        this.expiredIntegral = expiredIntegral;
    }

    public Integer getUsedIntegral() {
        return usedIntegral;
    }

    public void setUsedIntegral(Integer usedIntegral) {
        this.usedIntegral = usedIntegral;
    }
}
