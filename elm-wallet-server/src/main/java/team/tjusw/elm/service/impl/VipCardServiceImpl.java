package team.tjusw.elm.service.impl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import team.tjusw.elm.service.VipCardService;
import team.tjusw.elm.service.VirtualWalletService;
import team.tjusw.elm.po.VipCardPO;
import team.tjusw.elm.po.VirtualWalletPO;
import team.tjusw.elm.mapper.VipCardMapper;
import team.tjusw.elm.mapper.VirtualWalletMapper;

@Service
public class VipCardServiceImpl implements VipCardService {

    @Autowired
    private VipCardMapper vipCardMapper;
    @Autowired
    private VirtualWalletService virtualWalletService;
    @Autowired
    private VirtualWalletMapper virtualWalletMapper;

    @Override
    @Transactional
    public boolean purchaseVipCard(String userId, String cardType) {
        BigDecimal cost = "PREMIUM".equals(cardType) ? new BigDecimal("299.00") : new BigDecimal("99.00");
        virtualWalletService.pay(userId, cost);
        
        VipCardPO card = new VipCardPO();
        card.setUserId(userId);
        card.setCardType(cardType);
        card.setPurchaseDate(String.valueOf(System.currentTimeMillis()));
        card.setExpireDate(String.valueOf(System.currentTimeMillis() + 31536000000L)); // 1 year
        card.setStatus(1);
        vipCardMapper.insert(card);
        
        VirtualWalletPO wallet = virtualWalletMapper.selectByUserId(userId);
        wallet.setCreditLimit(new BigDecimal("2000.00"));
        virtualWalletMapper.updateWallet(wallet);
        
        return true;
    }
}