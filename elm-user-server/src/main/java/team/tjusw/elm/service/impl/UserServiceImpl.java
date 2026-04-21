package team.tjusw.elm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import team.tjusw.elm.mapper.UserMapper;
import team.tjusw.elm.po.Authority;
import team.tjusw.elm.po.User;
import team.tjusw.elm.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public User getUserByIdByPass(User user) {
		User stored = userMapper.getUserById(user.getUserId());
		if (stored == null) {
			return null;
		}
		String storedPassword = stored.getPassword();
		String rawPassword = user.getPassword();
		if (storedPassword == null || rawPassword == null) {
			return null;
		}

		// Backward compatibility: existing data may still be plaintext.
		boolean isBcrypt = storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")
				|| storedPassword.startsWith("$2y$");
		if (isBcrypt) {
			return passwordEncoder.matches(rawPassword, storedPassword) ? stored : null;
		}
		if (storedPassword.equals(rawPassword)) {
			userMapper.updatePassword(user.getUserId(), passwordEncoder.encode(rawPassword));
			stored.setPassword(null);
			return stored;
		}
		return null;
	}
	
	@Override
	public int getUserById(String userId) {
		return userMapper.countByUserId(userId);
	}
	
	@Override
	public int saveUser(User user) {
		if(this.getUserById(user.getUserId())==1)
			return 0;
		else {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			return userMapper.saveUser(user);
		}
	}

	@Override
	public User getUser(String userId) {
		User user = userMapper.getUserById(userId);
		if (user != null) {
			// Set authority based on userId
			Authority authority;
			if ("admin".equals(userId)) {
				authority = new Authority("ADMIN");
			} else {
				authority = new Authority("USER");
			}
			user.setAuthorities(Arrays.asList(authority));
		}
		return user;
	}

	@Override
	public List<User> listUsers() {
		return userMapper.listUsers();
	}

	@Override
	public User getUserByUserName(String userName) {
		return userMapper.getUserByUserName(userName);
	}

	@Override
	public boolean existsByUserName(String userName) {
		return userMapper.countByUserName(userName) > 0;
	}

	@Override
	public boolean updateUserName(String userId, String newUserName) {
		User existing = userMapper.getUserById(userId);
		if (existing == null) {
			return false;
		}
		return userMapper.updateUserName(userId, newUserName) > 0;
	}

	@Override
	public boolean changePassword(String userId, String currentPassword, String newPassword) {
		User authParam = new User();
		authParam.setUserId(userId);
		authParam.setPassword(currentPassword);
		User user = getUserByIdByPass(authParam);
		if (user == null) {
			return false;
		}
		return userMapper.updatePassword(userId, passwordEncoder.encode(newPassword)) > 0;
	}

	@Override
	public boolean updateUserProfile(String userId, String userName, Integer userSex, String userImg) {
		User existing = userMapper.getUserById(userId);
		if (existing == null) {
			return false;
		}
		if (userName != null) {
			existing.setUserName(userName);
		}
		if (userSex != null) {
			existing.setUserSex(userSex);
		}
		if (userImg != null) {
			existing.setUserImg(userImg);
		}
		return userMapper.updateUserProfile(existing) > 0;
	}
	
	

	
}
