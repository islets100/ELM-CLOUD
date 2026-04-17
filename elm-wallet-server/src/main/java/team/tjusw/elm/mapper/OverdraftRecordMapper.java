package team.tjusw.elm.mapper;
import org.apache.ibatis.annotations.*;
import team.tjusw.elm.po.OverdraftRecordPO;
import java.util.List;

@Mapper
public interface OverdraftRecordMapper {
    @Insert("INSERT INTO overdraft_record (wallet_id, amount, repaid_amount, interest, create_time, status) VALUES (#{walletId}, #{amount}, #{repaidAmount}, #{interest}, now(), #{status})")
    int insert(OverdraftRecordPO record);

    @Select("SELECT * FROM overdraft_record WHERE wallet_id = #{walletId} AND status = 0")
    List<OverdraftRecordPO> selectUnpaidByWalletId(@Param("walletId") Integer walletId);

    @Update("UPDATE overdraft_record SET repaid_amount = #{repaidAmount}, interest = #{interest}, status = #{status} WHERE id = #{id}")
    int update(OverdraftRecordPO record);
}