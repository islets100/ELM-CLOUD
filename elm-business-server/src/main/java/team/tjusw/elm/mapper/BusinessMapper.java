package team.tjusw.elm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import team.tjusw.elm.po.Business;

@Mapper
public interface BusinessMapper {
	@Select("select * from business where orderTypeId=#{orderTypeId} order by businessId")
	public List<Business> listBusinessByOrderTypeId(Integer orderTypeId);

	@Select("select * from business where businessId=#{businessId}")
	public Business getBusinessById(Integer businessId);

	@Select("select * from business where userId=#{userId} order by businessId")
	public List<Business> listBusinessByUserId(String userId);

	@Insert("insert into business (businessName, businessAddress, businessExplain, businessImg, orderTypeId, starPrice, deliveryPrice, remarks, userId) "
			+ "values (#{businessName}, #{businessAddress}, #{businessExplain}, #{businessImg}, #{orderTypeId}, #{starPrice}, #{deliveryPrice}, #{remarks}, #{userId})")
	@Options(useGeneratedKeys = true, keyProperty = "businessId", keyColumn = "businessId")
	public int insertBusiness(Business business);

	@Update("update business set businessName=#{businessName}, businessAddress=#{businessAddress}, "
			+ "businessExplain=#{businessExplain}, businessImg=#{businessImg}, orderTypeId=#{orderTypeId}, "
			+ "starPrice=#{starPrice}, deliveryPrice=#{deliveryPrice}, remarks=#{remarks}, userId=#{userId} "
			+ "where businessId=#{businessId}")
	public int updateBusiness(Business business);
}
