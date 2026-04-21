package team.tjusw.elm.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import team.tjusw.elm.service.VirtualWalletService;
import team.tjusw.elm.po.VirtualWalletPO;
import team.tjusw.elm.po.OverdraftRecordPO;
import team.tjusw.elm.mapper.VirtualWalletMapper;
import team.tjusw.elm.mapper.OverdraftRecordMapper;
import team.tjusw.elm.feign.UserClient;

@Service
public class VirtualWalletServiceImpl implements VirtualWalletService {

    @Autowired
    private VirtualWalletMapper virtualWalletMapper;
    @Autowired
    private OverdraftRecordMapper overdraftRecordMapper;
    @Autowired
    private UserClient userClient;

    private VirtualWalletPO getOrCreateWallet(String userId) {
        VirtualWalletPO wallet = virtualWalletMapper.selectByUserId(userId);
        if (wallet == null) {
            team.tjusw.elm.po.CommonResult<Integer> userRes = userClient.validateUser(userId);
            if (userRes.getCode() != 200) {
                throw new IllegalArgumentException("The user does not exist");
            }
            wallet = new VirtualWalletPO();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setCreditLimit(BigDecimal.ZERO);
            wallet.setUsedCreditLimit(BigDecimal.ZERO);
            virtualWalletMapper.insert(wallet);
            wallet = virtualWalletMapper.selectByUserId(userId);
        }
        return wallet;
    }

    @Override
    public VirtualWalletPO getVirtualWallet(String userId) {
        return getOrCreateWallet(userId);
    }

    @Override
    public BigDecimal getBalance(String userId) {
        return getOrCreateWallet(userId).getBalance();
    }

    @Override
    @Transactional
    public boolean recharge(String userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            return false;
        VirtualWalletPO wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        virtualWalletMapper.updateWallet(wallet);
        return true;
    }

    @Override
    @Transactional
    public boolean pay(String userId, BigDecimal amount) {
        VirtualWalletPO wallet = getOrCreateWallet(userId);
        BigDecimal currentBalance = wallet.getBalance();

        if (currentBalance.compareTo(amount) >= 0) {
            wallet.setBalance(currentBalance.subtract(amount));
            virtualWalletMapper.updateWallet(wallet);
            return true;
        } else {
            BigDecimal missing = amount.subtract(currentBalance);
            BigDecimal availableCredit = wallet.getCreditLimit().subtract(wallet.getUsedCreditLimit());
            if (availableCredit.compareTo(missing) >= 0) {
                wallet.setBalance(BigDecimal.ZERO);
                wallet.setUsedCreditLimit(wallet.getUsedCreditLimit().add(missing));
                wallet.setLastInterestTime(String.valueOf(System.currentTimeMillis()));

                OverdraftRecordPO record = new OverdraftRecordPO();
                record.setWalletId(wallet.getWalletId());
                record.setAmount(missing);
                record.setRepaidAmount(BigDecimal.ZERO);
                record.setInterest(BigDecimal.ZERO);
                record.setStatus(0);
                overdraftRecordMapper.insert(record);

                virtualWalletMapper.updateWallet(wallet);
                return true;
            }
        }
        throw new RuntimeException("Insufficient balance and credit");
    }

    @Override
    @Transactional
    public int transfer(String fromUserId, String toUserId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        if (!pay(fromUserId, amount)) {
            return 0;
        }
        if (!recharge(toUserId, amount)) {
            throw new RuntimeException("Transfer failed");
        }
        return 1;
    }

    @Override
    @Transactional
    public boolean repayOverdraft(String userId, BigDecimal amount) {
        VirtualWalletPO wallet = virtualWalletMapper.selectByUserId(userId);
        if (wallet == null)
            return false;

        List<OverdraftRecordPO> unpaid = overdraftRecordMapper.selectUnpaidByWalletId(wallet.getWalletId());
        BigDecimal remaining = amount;

        for (OverdraftRecordPO record : unpaid) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0)
                break;
            BigDecimal debt = record.getAmount().subtract(record.getRepaidAmount()).add(record.getInterest());
            if (remaining.compareTo(debt) >= 0) {
                remaining = remaining.subtract(debt);
                record.setRepaidAmount(record.getAmount());
                record.setStatus(1);
                wallet.setUsedCreditLimit(wallet.getUsedCreditLimit().subtract(record.getAmount()));
            } else {
                record.setRepaidAmount(record.getRepaidAmount().add(remaining));
                wallet.setUsedCreditLimit(wallet.getUsedCreditLimit().subtract(remaining));
                remaining = BigDecimal.ZERO;
            }
            overdraftRecordMapper.update(record);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            wallet.setBalance(wallet.getBalance().add(remaining));
        }
        virtualWalletMapper.updateWallet(wallet);
        return true;
    }
}
