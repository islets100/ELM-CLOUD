package team.tjusw.elm.mapper;
import org.apache.ibatis.annotations.*;
import team.tjusw.elm.po.VirtualWalletPO;

@Mapper
public interface VirtualWalletMapper {
    @Select("SELECT wallet_id as walletId, user_id as userId, balance, credit_limit as creditLimit, used_credit_limit as usedCreditLimit, last_interest_time as lastInterestTime, create_time as createTime FROM virtual_wallet WHERE user_id = #{userId}")
    VirtualWalletPO selectByUserId(@Param("userId") String userId);

    @Select("SELECT wallet_id as walletId, user_id as userId, balance, credit_limit as creditLimit, used_credit_limit as usedCreditLimit, last_interest_time as lastInterestTime, create_time as createTime FROM virtual_wallet WHERE wallet_id = #{walletId}")
    VirtualWalletPO selectById(@Param("walletId") Integer walletId);

    @Insert("INSERT INTO virtual_wallet (user_id, balance, credit_limit, used_credit_limit) VALUES (#{userId}, #{balance}, #{creditLimit}, #{usedCreditLimit})")
    int insert(VirtualWalletPO wallet);

    @Update("UPDATE virtual_wallet SET balance = #{balance}, credit_limit = #{creditLimit}, used_credit_limit = #{usedCreditLimit}, last_interest_time = #{lastInterestTime} WHERE wallet_id = #{walletId}")
    int updateWallet(VirtualWalletPO wallet);
}