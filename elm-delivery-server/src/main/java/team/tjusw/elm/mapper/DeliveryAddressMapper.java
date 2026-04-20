package team.tjusw.elm.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import team.tjusw.elm.po.DeliveryAddress;

@Mapper
public interface DeliveryAddressMapper {
	@Select("select * from deliveryAddress where userId=#{userId} and valid = 1 order by daId")
	public List<DeliveryAddress> listDeliveryAddressByUserId(String userId);

	@Select("select * from deliveryAddress where daId=#{daId}")
	public DeliveryAddress getDeliveryAddressById(Integer daId);

	@Insert("insert into deliveryAddress values(null,#{contactName},#{contactSex},#{contactTel},#{address},#{userId},1)")
	@Options(useGeneratedKeys = true,keyProperty = "daId",keyColumn="daId")
	public int saveDeliveryAddress(DeliveryAddress deliveryAddress);

	public int updateDeliveryAddress(DeliveryAddress deliveryAddress);

	@org.apache.ibatis.annotations.Update("update deliveryAddress set valid = 0 where daId = #{daId}")
	public int removeDeliveryAddress(Integer daId);
}

