package team.tjusw.elm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import team.tjusw.elm.service.VirtualWalletService;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.VirtualWalletPO;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/wallet")
public class VirtualWalletController {

    @Autowired
    private VirtualWalletService walletService;

    // 根据Token获取当前用户的钱包信息（适配前端）
    @GetMapping
    public CommonResult<VirtualWalletPO> getCurrentWallet(HttpServletRequest request) {
        try {
            // 从请求头获取token并解析userId
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return new CommonResult<>(401, "Missing or invalid token", null);
            }

            String token = authorization.substring(7);
            String userId = getUserIdFromToken(token);
            if (userId == null) {
                return new CommonResult<>(401, "Invalid token", null);
            }

            return new CommonResult<>(200, "success", walletService.getVirtualWallet(userId));
        } catch (Exception e) {
            return new CommonResult<>(500, e.getMessage(), null);
        }
    }

    private String getUserIdFromToken(String token) {
        try {
            // 简单的JWT解析，提取userId（subject）
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            // 简单解析，实际应使用JWT库
            if (payload.contains("\"sub\":\"")) {
                int start = payload.indexOf("\"sub\":\"") + 6;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return null;
    }

    @GetMapping("/getBalance")
    public CommonResult<BigDecimal> getBalance(@RequestParam("userId") String userId) {
        return new CommonResult<>(200, "success", walletService.getBalance(userId));
    }

    @PostMapping("/recharge")
    public CommonResult<Boolean> recharge(@RequestParam("userId") String userId,
            @RequestParam("amount") BigDecimal amount) {
        return new CommonResult<>(200, "success", walletService.recharge(userId, amount));
    }

    @PostMapping("/pay")
    public CommonResult<Boolean> pay(@RequestParam("userId") String userId, @RequestParam("amount") BigDecimal amount) {
        try {
            return new CommonResult<>(200, "success", walletService.pay(userId, amount));
        } catch (Exception e) {
            return new CommonResult<>(500, e.getMessage(), false);
        }
    }

    @PostMapping("/transfer")
    public CommonResult<Integer> transfer(@RequestParam("fromUserId") String fromUserId,
            @RequestParam("toUserId") String toUserId,
            @RequestParam("amount") BigDecimal amount) {
        try {
            return new CommonResult<>(200, "success", walletService.transfer(fromUserId, toUserId, amount));
        } catch (Exception e) {
            return new CommonResult<>(500, e.getMessage(), 0);
        }
    }

    @PostMapping("/repayOverdraft")
    public CommonResult<Boolean> repayOverdraft(@RequestParam("userId") String userId,
            @RequestParam("amount") BigDecimal amount) {
        return new CommonResult<>(200, "success", walletService.repayOverdraft(userId, amount));
    }

    @GetMapping("/getVirtualWallet")
    public CommonResult<VirtualWalletPO> getVirtualWallet(@RequestParam("userId") String userId) {
        return new CommonResult<>(200, "success", walletService.getVirtualWallet(userId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult<?> handleIllegalArgumentException(IllegalArgumentException e) {
        if ("The user does not exist".equals(e.getMessage())) {
            return new CommonResult<>(404, "The user does not exist", null);
        }
        return new CommonResult<>(500, e.getMessage(), null);
    }
}
