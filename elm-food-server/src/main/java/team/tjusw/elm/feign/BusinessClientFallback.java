package team.tjusw.elm.feign;

import org.springframework.stereotype.Component;
import team.tjusw.elm.po.CommonResult;

@Component
public class BusinessClientFallback implements BusinessClient {

    @Override
    public String getBusinessById(Integer businessId) {
        // Fallback or Downgrade logic when the business server is unavailable
        // We simulate a successful call so we don't block the food service logic
        return "{\"code\":200, \"message\":\"Fallback: Business service unavailable, assuming business exists\"}";
    }
}
