package team.tjusw.elm.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "elm-user-server", fallback = UserClientFallback.class)
public interface UserClient {
    @GetMapping("/users/exists/{userId}")
    team.tjusw.elm.po.CommonResult<Integer> validateUser(@PathVariable("userId") String userId);
}