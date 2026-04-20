package team.tjusw.elm.mapper;

import org.apache.ibatis.annotations.*;
import team.tjusw.elm.po.VirtualWalletPO;

@Mapper
public interface VirtualWalletMapper {
    @Select("SELECT walletId, userId, balance, creditLimit, usedCreditLimit, lastInterestTime, createTime FROM VirtualWallet WHERE userId = #{userId}")
    VirtualWalletPO selectByUserId(@Param("userId") String userId);

    @Select("SELECT walletId, userId, balance, creditLimit, usedCreditLimit, lastInterestTime, createTime FROM VirtualWallet WHERE walletId = #{walletId}")
    VirtualWalletPO selectById(@Param("walletId") Integer walletId);

    @Insert("INSERT INTO VirtualWallet (userId, balance, creditLimit, usedCreditLimit) VALUES (#{userId}, #{balance}, #{creditLimit}, #{usedCreditLimit})")
    int insert(VirtualWalletPO wallet);

    @Update("UPDATE VirtualWallet SET balance = #{balance}, creditLimit = #{creditLimit}, usedCreditLimit = #{usedCreditLimit}, lastInterestTime = #{lastInterestTime} WHERE walletId = #{walletId}")
    int updateWallet(VirtualWalletPO wallet);
}