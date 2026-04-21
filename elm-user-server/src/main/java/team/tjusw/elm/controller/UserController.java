package team.tjusw.elm.controller;

import java.util.Map;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import team.tjusw.elm.dto.JwtTokenDto;
import team.tjusw.elm.dto.LoginDto;
import team.tjusw.elm.dto.PasswordChangeDto;
import team.tjusw.elm.dto.RegisterDto;
import team.tjusw.elm.dto.BirthdayModificationCheckDto;
import team.tjusw.elm.dto.UserNameUpdateDto;
import team.tjusw.elm.dto.UserUpdateDto;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.User;
import team.tjusw.elm.security.JwtTokenProvider;
import team.tjusw.elm.service.UserService;

@RestController
public class UserController {
	@Autowired
	private UserService userService;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@GetMapping("/users/{userId}")
	public CommonResult<User> getUserByIdByPass(@PathVariable("userId") String userId, String password,
			HttpServletRequest request) throws Exception {
		User param = new User();
		param.setUserId(userId);
		if (!StringUtils.hasText(password)) {
			return new CommonResult<User>(400, "password is required", null);
		}
		param.setPassword(password);

		User ret = userService.getUserByIdByPass(param);
		if (ret == null) {
			if (userService.getUserById(userId) != 0) {
				return new CommonResult<User>(403, "invalid password", null);
			}
			return new CommonResult<User>(404, "user not found", null);
		}
		ret.setPassword(null);
		return new CommonResult<User>(200, "success", ret);
	}

	@GetMapping("/users/exists/{userId}")
	public CommonResult<Integer> getUserById(@PathVariable("userId") String userId) throws Exception {
		int ret = userService.getUserById(userId);
		if (ret == 0) {
			return new CommonResult<Integer>(404, "user not exists", ret);
		}
		return new CommonResult<Integer>(200, "user exists", ret);
	}

	@PostMapping("/users/{userId}")
	public CommonResult<Integer> saveUser(@PathVariable("userId") String userId, HttpServletRequest request)
			throws Exception {
		Map<String, String[]> formData = request.getParameterMap();
		User param = new User();
		param.setUserId(userId);
		if (!hasField(formData, "password") || !hasField(formData, "userName") || !hasField(formData, "userSex")) {
			return new CommonResult<Integer>(400, "missing required fields", 0);
		}
		param.setPassword(formData.get("password")[0]);
		param.setUserName(formData.get("userName")[0]);
		param.setUserSex(Integer.valueOf(formData.get("userSex")[0]));

		int ret = userService.saveUser(param);
		if (ret == 0) {
			return new CommonResult<Integer>(409, "user already exists", ret);
		}
		return new CommonResult<Integer>(200, "register success", ret);
	}

	@PostMapping("/api/auth")
	public ResponseEntity<JwtTokenDto> authorize(@RequestBody LoginDto loginDto) {
		if (loginDto == null || !StringUtils.hasText(loginDto.getUsername())
				|| !StringUtils.hasText(loginDto.getPassword())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		User user = new User();
		user.setUserId(loginDto.getUsername());
		user.setPassword(loginDto.getPassword());
		User authResult = userService.getUserByIdByPass(user);
		if (authResult == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		boolean rememberMe = Boolean.TRUE.equals(loginDto.getRememberMe());
		String jwt = jwtTokenProvider.createToken(authResult.getUserId(), rememberMe);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + jwt);
		return new ResponseEntity<JwtTokenDto>(new JwtTokenDto(jwt), headers, HttpStatus.OK);
	}

	@PostMapping("/api/register")
	public CommonResult<User> register(@RequestBody RegisterDto registerDto) {
		if (registerDto == null) {
			return new CommonResult<User>(400, "missing required fields", null);
		}

		String legacyUsername = StringUtils.hasText(registerDto.getUsername()) ? registerDto.getUsername().trim() : null;
		String userId = StringUtils.hasText(registerDto.getUserId()) ? registerDto.getUserId().trim() : legacyUsername;
		String userName = StringUtils.hasText(registerDto.getUserName()) ? registerDto.getUserName().trim() : legacyUsername;
		String password = StringUtils.hasText(registerDto.getPassword()) ? registerDto.getPassword() : "password";
		Integer userSex = registerDto.getUserSex() != null ? registerDto.getUserSex() : 1;

		if (!StringUtils.hasText(userId) || !StringUtils.hasText(userName) || !StringUtils.hasText(password)) {
			return new CommonResult<User>(400, "missing required fields", null);
		}

		User user = new User();
		user.setUserId(userId);
		user.setPassword(password);
		user.setUserName(userName);
		user.setUserSex(userSex);
		int ret = userService.saveUser(user);
		if (ret == 0) {
			return new CommonResult<User>(409, "user already exists", null);
		}
		user.setPassword(null);
		return new CommonResult<User>(200, "register success", user);
	}

	@PostMapping("/api/users")
	public CommonResult<User> createUser(@RequestBody RegisterDto registerDto) {
		return register(registerDto);
	}

	@PostMapping("/api/persons")
	public CommonResult<User> createPerson(@RequestBody RegisterDto registerDto) {
		// Transitional implementation: map person creation to user creation.
		return register(registerDto);
	}

	@GetMapping("/api/user")
	public CommonResult<User> getCurrentUser(HttpServletRequest request) {
		String userId = resolveUserIdFromAuthorization(request);
		if (!StringUtils.hasText(userId)) {
			return new CommonResult<User>(401, "invalid or missing token", null);
		}
		User user = userService.getUser(userId);
		if (user == null) {
			return new CommonResult<User>(404, "user not found", null);
		}
		user.setPassword(null);
		return new CommonResult<User>(200, "success", user);
	}

	@GetMapping("/api/users")
	public CommonResult<List<User>> listUsers() {
		List<User> users = userService.listUsers();
		for (User user : users) {
			user.setPassword(null);
		}
		return new CommonResult<List<User>>(200, "success", users);
	}

	@GetMapping("/api/userByUsername")
	public CommonResult<User> getUserByUserName(String userName) {
		if (!StringUtils.hasText(userName)) {
			return new CommonResult<User>(400, "userName is required", null);
		}
		User user = userService.getUserByUserName(userName);
		if (user == null) {
			return new CommonResult<User>(404, "user not found", null);
		}
		user.setPassword(null);
		return new CommonResult<User>(200, "success", user);
	}

	@GetMapping("/api/userById")
	public CommonResult<Integer> getUserByIdApi(String userId) {
		if (!StringUtils.hasText(userId)) {
			return new CommonResult<Integer>(400, "userId is required", 0);
		}
		int count = userService.getUserById(userId);
		return new CommonResult<Integer>(200, "success", count > 0 ? 1 : 0);
	}

	@GetMapping("/api/userByIdByPass")
	public CommonResult<User> getUserByIdAndPassword(String userId, String password) {
		if (!StringUtils.hasText(userId) || !StringUtils.hasText(password)) {
			return new CommonResult<User>(400, "userId and password are required", null);
		}
		User query = new User();
		query.setUserId(userId);
		query.setPassword(password);
		User user = userService.getUserByIdByPass(query);
		if (user == null) {
			return new CommonResult<User>(403, "invalid userId or password", null);
		}
		user.setPassword(null);
		return new CommonResult<User>(200, "success", user);
	}

	@GetMapping("/api/user/birthday/modification/check")
	public CommonResult<BirthdayModificationCheckDto> checkBirthdayModification(HttpServletRequest request) {
		String userId = resolveUserIdFromAuthorization(request);
		if (!StringUtils.hasText(userId)) {
			return new CommonResult<BirthdayModificationCheckDto>(401, "invalid or missing token", null);
		}
		// Current data model does not persist birthday modification history yet.
		BirthdayModificationCheckDto result = new BirthdayModificationCheckDto(true,
				"birthday-modification-history-not-implemented-yet");
		return new CommonResult<BirthdayModificationCheckDto>(200, "success", result);
	}

	@GetMapping("/api/userExistsByUsername")
	public CommonResult<Integer> userExistsByUsername(String userName) {
		if (!StringUtils.hasText(userName)) {
			return new CommonResult<Integer>(400, "userName is required", 0);
		}
		boolean exists = userService.existsByUserName(userName);
		return new CommonResult<Integer>(200, "success", exists ? 1 : 0);
	}

	@PostMapping("/api/password")
	public CommonResult<Integer> changePassword(@RequestBody PasswordChangeDto dto, HttpServletRequest request) {
		String userId = resolveUserIdFromAuthorization(request);
		if (!StringUtils.hasText(userId)) {
			return new CommonResult<Integer>(401, "invalid or missing token", 0);
		}
		if (dto == null || !StringUtils.hasText(dto.getCurrentPassword()) || !StringUtils.hasText(dto.getNewPassword())) {
			return new CommonResult<Integer>(400, "missing required fields", 0);
		}
		boolean ok = userService.changePassword(userId, dto.getCurrentPassword(), dto.getNewPassword());
		if (!ok) {
			return new CommonResult<Integer>(403, "current password is incorrect", 0);
		}
		return new CommonResult<Integer>(200, "password updated", 1);
	}

	@PutMapping("/api/users/{userId}")
	public CommonResult<Integer> updateUser(@PathVariable("userId") String userId, @RequestBody UserUpdateDto dto,
			HttpServletRequest request) {
		String tokenUserId = resolveUserIdFromAuthorization(request);
		if (!StringUtils.hasText(tokenUserId)) {
			return new CommonResult<Integer>(401, "invalid or missing token", 0);
		}
		if (!tokenUserId.equals(userId)) {
			return new CommonResult<Integer>(403, "no permission to update this user", 0);
		}
		if (dto == null) {
			return new CommonResult<Integer>(400, "missing request body", 0);
		}
		boolean ok = userService.updateUserProfile(userId, dto.getUserName(), dto.getUserSex(), dto.getUserImg());
		if (!ok) {
			return new CommonResult<Integer>(404, "user not found", 0);
		}
		return new CommonResult<Integer>(200, "user profile updated", 1);
	}

	@PutMapping("/api/users/{userId}/username")
	public CommonResult<Integer> updateUserName(@PathVariable("userId") String userId,
			@RequestBody UserNameUpdateDto dto, HttpServletRequest request) {
		String tokenUserId = resolveUserIdFromAuthorization(request);
		if (!StringUtils.hasText(tokenUserId)) {
			return new CommonResult<Integer>(401, "invalid or missing token", 0);
		}
		if (!tokenUserId.equals(userId)) {
			return new CommonResult<Integer>(403, "no permission to update this user", 0);
		}
		if (dto == null || !StringUtils.hasText(dto.getNewUsername())) {
			return new CommonResult<Integer>(400, "newUsername is required", 0);
		}
		if (userService.existsByUserName(dto.getNewUsername())) {
			return new CommonResult<Integer>(409, "username already exists", 0);
		}
		boolean ok = userService.updateUserName(userId, dto.getNewUsername());
		if (!ok) {
			return new CommonResult<Integer>(404, "user not found", 0);
		}
		return new CommonResult<Integer>(200, "username updated", 1);
	}

	private boolean hasField(Map<String, String[]> formData, String key) {
		return formData.containsKey(key) && formData.get(key) != null && formData.get(key).length > 0
				&& StringUtils.hasText(formData.get(key)[0]);
	}

	private String resolveUserIdFromAuthorization(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
			return null;
		}
		String token = header.substring(7);
		return jwtTokenProvider.getUserIdFromToken(token);
	}
}
