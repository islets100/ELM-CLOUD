package team.tjusw.elm.feign;

import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public team.tjusw.elm.po.CommonResult<Integer> validateUser(String userId) {
        return new team.tjusw.elm.po.CommonResult<>(500, "user fail", null);
    }
}