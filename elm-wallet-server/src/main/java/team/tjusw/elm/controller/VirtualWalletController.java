package team.tjusw.elm.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import team.tjusw.elm.service.VirtualWalletService;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.VirtualWalletPO;

@RestController
@RequestMapping("/wallet")
public class VirtualWalletController {

    @Autowired
    private VirtualWalletService walletService;

    @GetMapping("/getBalance")
    public CommonResult<BigDecimal> getBalance(@RequestParam("userId") String userId) {
        return new CommonResult<>(200, "success", walletService.getBalance(userId));
    }

    @PostMapping("/recharge")
    public CommonResult<Boolean> recharge(@RequestParam("userId") String userId, @RequestParam("amount") BigDecimal amount) {
        return new CommonResult<>(200, "success", walletService.recharge(userId, amount));
    }

    @PostMapping("/pay")
    public CommonResult<Boolean> pay(@RequestParam("userId") String userId, @RequestParam("amount") BigDecimal amount) {
        try {
            return new CommonResult<>(200, "success", walletService.pay(userId, amount));
        } catch(Exception e) {
            return new CommonResult<>(500, e.getMessage(), false);
        }
    }

    @PostMapping("/repayOverdraft")
    public CommonResult<Boolean> repayOverdraft(@RequestParam("userId") String userId, @RequestParam("amount") BigDecimal amount) {
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