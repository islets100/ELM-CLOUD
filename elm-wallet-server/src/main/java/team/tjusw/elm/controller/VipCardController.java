package team.tjusw.elm.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.tjusw.elm.service.VipCardService;
import team.tjusw.elm.po.CommonResult;

@RestController
@RequestMapping("/vip")
public class VipCardController {

    @Autowired
    private VipCardService vipCardService;

    @PostMapping("/purchase")
    public CommonResult<Boolean> purchase(@RequestParam("userId") String userId, @RequestParam("cardType") String cardType) {
        try {
            return new CommonResult<>(200, "success", vipCardService.purchaseVipCard(userId, cardType));
        } catch(Exception e) {
            return new CommonResult<>(500, e.getMessage(), false);
        }
    }
}