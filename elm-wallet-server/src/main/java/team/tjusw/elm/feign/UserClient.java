package team.tjusw.elm.feign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Component;

@FeignClient(name = "elm-user-server", fallback = UserClientFallback.class)
public interface UserClient {
    @GetMapping("/user/validate")
    boolean validateUser(@RequestParam("userId") String userId);
}