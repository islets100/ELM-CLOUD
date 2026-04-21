package team.tjusw.elm.feign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "elm-business-server", fallback = BusinessClientFallback.class)
public interface BusinessClient {
    @GetMapping("/businesses/{businessId}")
    String getBusinessById(@PathVariable("businessId") Integer businessId);
}
