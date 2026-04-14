package team.tjusw.elm.service;

import team.tjusw.elm.po.User;

import java.util.List;

public interface UserService {
	public User getUserByIdByPass(User user);

	public int saveUser(User user);
	
	public int getUserById(String userId);

	public User getUser(String userId);

	public boolean changePassword(String userId, String currentPassword, String newPassword);

	public boolean updateUserProfile(String userId, String userName, Integer userSex, String userImg);

	public List<User> listUsers();

	public User getUserByUserName(String userName);

	public boolean existsByUserName(String userName);

	public boolean updateUserName(String userId, String newUserName);
}
