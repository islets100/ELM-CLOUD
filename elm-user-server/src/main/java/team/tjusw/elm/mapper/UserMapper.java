package team.tjusw.elm.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

import team.tjusw.elm.po.User;
@Mapper
public interface UserMapper {
 @Select("select * from user where userId=#{userId} and delTag = 1")
 public User getUserById(String userId);

 @Select("select * from user where userName=#{userName} and delTag = 1 limit 1")
 public User getUserByUserName(String userName);
 
 @Select("select count(*) from user where userId=#{userId}")
 public int countByUserId(String userId);

 @Select("select count(*) from user where userName=#{userName} and delTag = 1")
 public int countByUserName(String userName);

 @Select("select * from user where delTag = 1")
 public List<User> listUsers();
 
 @Insert("insert into user values(#{userId},#{password},#{userName},#{userSex},null,1,#{userType})")
 public int saveUser(User user);

 @Update("update user set password=#{password} where userId=#{userId}")
 public int updatePassword(@Param("userId") String userId, @Param("password") String password);

 @Update("update user set userName=#{userName}, userSex=#{userSex}, userImg=#{userImg} where userId=#{userId}")
 public int updateUserProfile(User user);

 @Update("update user set userName=#{userName} where userId=#{userId}")
 public int updateUserName(@Param("userId") String userId, @Param("userName") String userName);
}
