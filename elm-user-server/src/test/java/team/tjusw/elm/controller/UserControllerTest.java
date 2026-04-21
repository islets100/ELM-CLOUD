package team.tjusw.elm.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import team.tjusw.elm.po.User;
import team.tjusw.elm.security.JwtTokenProvider;
import team.tjusw.elm.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	private MockMvc mockMvc;

	@Mock
	private UserService userService;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private UserController userController;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
	}

	@Test
	void getCurrentUserShouldReturnUser() throws Exception {
		User user = new User();
		user.setUserId("u1001");
		user.setUserName("Alice");
		user.setUserSex(1);

		when(jwtTokenProvider.getUserIdFromToken("token-ok")).thenReturn("u1001");
		when(userService.getUser("u1001")).thenReturn(user);

		mockMvc.perform(get("/api/user").header("Authorization", "Bearer token-ok"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result.userId").value("u1001"))
				.andExpect(jsonPath("$.result.userName").value("Alice"));
	}

	@Test
	void getCurrentUserShouldReturnUnauthorizedWhenTokenInvalid() throws Exception {
		when(jwtTokenProvider.getUserIdFromToken("bad-token")).thenReturn(null);

		mockMvc.perform(get("/api/user").header("Authorization", "Bearer bad-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(401));
	}

	@Test
	void changePasswordShouldReturnSuccess() throws Exception {
		when(jwtTokenProvider.getUserIdFromToken("token-ok")).thenReturn("u1001");
		when(userService.changePassword("u1001", "oldPwd", "newPwd")).thenReturn(true);

		mockMvc.perform(post("/api/password").header("Authorization", "Bearer token-ok")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"currentPassword\":\"oldPwd\",\"newPassword\":\"newPwd\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result").value(1));
	}

	@Test
	void updateUserShouldReturnForbiddenWhenNotSelf() throws Exception {
		when(jwtTokenProvider.getUserIdFromToken("token-ok")).thenReturn("u1001");

		mockMvc.perform(put("/api/users/u2002").header("Authorization", "Bearer token-ok")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"userName\":\"Bob\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(403));
	}

	@Test
	void updateUserShouldReturnSuccess() throws Exception {
		when(jwtTokenProvider.getUserIdFromToken("token-ok")).thenReturn("u1001");
		when(userService.updateUserProfile(eq("u1001"), eq("Alice2"), eq(1), eq("img.png"))).thenReturn(true);

		mockMvc.perform(put("/api/users/u1001").header("Authorization", "Bearer token-ok")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"userName\":\"Alice2\",\"userSex\":1,\"userImg\":\"img.png\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result").value(1));
	}

	@Test
	void authShouldReturnUnauthorizedWhenPasswordWrong() throws Exception {
		when(userService.getUserByIdByPass(any(User.class))).thenReturn(null);

		mockMvc.perform(post("/api/auth").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"u1001\",\"password\":\"wrong\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void listUsersShouldReturnUsers() throws Exception {
		User user1 = new User();
		user1.setUserId("u1001");
		user1.setUserName("Alice");
		User user2 = new User();
		user2.setUserId("u1002");
		user2.setUserName("Bob");
		when(userService.listUsers()).thenReturn(Arrays.asList(user1, user2));

		mockMvc.perform(get("/api/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result[0].userId").value("u1001"))
				.andExpect(jsonPath("$.result[1].userId").value("u1002"));
	}

	@Test
	void getUserByUserNameShouldReturnBadRequestWhenMissingUserName() throws Exception {
		mockMvc.perform(get("/api/userByUsername"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(400));
	}

	@Test
	void userExistsByUserNameShouldReturnOneWhenExists() throws Exception {
		when(userService.existsByUserName("Alice")).thenReturn(true);

		mockMvc.perform(get("/api/userExistsByUsername").param("userName", "Alice"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result").value(1));
	}

	@Test
	void updateUserNameShouldReturnSuccess() throws Exception {
		when(jwtTokenProvider.getUserIdFromToken("token-ok")).thenReturn("u1001");
		when(userService.existsByUserName("Alice2")).thenReturn(false);
		when(userService.updateUserName("u1001", "Alice2")).thenReturn(true);

		mockMvc.perform(put("/api/users/u1001/username").header("Authorization", "Bearer token-ok")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"newUsername\":\"Alice2\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result").value(1));
	}

	@Test
	void updateUserNameShouldReturnConflictWhenDuplicated() throws Exception {
		when(jwtTokenProvider.getUserIdFromToken("token-ok")).thenReturn("u1001");
		when(userService.existsByUserName("Alice2")).thenReturn(true);

		mockMvc.perform(put("/api/users/u1001/username").header("Authorization", "Bearer token-ok")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"newUsername\":\"Alice2\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(409));
	}

	@Test
	void updateUserNameShouldReturnForbiddenWhenNotSelf() throws Exception {
		when(jwtTokenProvider.getUserIdFromToken("token-ok")).thenReturn("u1001");

		mockMvc.perform(put("/api/users/u2002/username").header("Authorization", "Bearer token-ok")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"newUsername\":\"Alice2\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(403));
	}

	@Test
	void getUserByIdApiShouldReturnOneWhenExists() throws Exception {
		when(userService.getUserById("u1001")).thenReturn(1);

		mockMvc.perform(get("/api/userById").param("userId", "u1001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result").value(1));
	}

	@Test
	void getUserByIdByPassShouldReturnBadRequestWhenMissingPassword() throws Exception {
		mockMvc.perform(get("/api/userByIdByPass").param("userId", "u1001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(400));
	}

	@Test
	void getUserByIdByPassShouldReturnUserWhenMatched() throws Exception {
		User user = new User();
		user.setUserId("u1001");
		user.setUserName("Alice");
		when(userService.getUserByIdByPass(any(User.class))).thenReturn(user);

		mockMvc.perform(get("/api/userByIdByPass").param("userId", "u1001").param("password", "123456"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result.userId").value("u1001"));
	}

	@Test
	void createUserShouldReturnSuccess() throws Exception {
		when(userService.saveUser(any(User.class))).thenReturn(1);

		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"userId\":\"u3001\",\"password\":\"123456\",\"userName\":\"Tom\",\"userSex\":1}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result.userId").value("u3001"));
	}

	@Test
	void registerShouldAcceptLegacyUsernamePayload() throws Exception {
		when(userService.saveUser(any(User.class))).thenReturn(1);

		mockMvc.perform(post("/api/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"legacyUser\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result.userId").value("legacyUser"))
				.andExpect(jsonPath("$.result.userName").value("legacyUser"));
	}

	@Test
	void createPersonShouldReturnConflictWhenExists() throws Exception {
		when(userService.saveUser(any(User.class))).thenReturn(0);

		mockMvc.perform(post("/api/persons")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"userId\":\"u3001\",\"password\":\"123456\",\"userName\":\"Tom\",\"userSex\":1}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(409));
	}

	@Test
	void checkBirthdayModificationShouldReturnUnauthorizedWithoutToken() throws Exception {
		mockMvc.perform(get("/api/user/birthday/modification/check"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(401));
	}

	@Test
	void checkBirthdayModificationShouldReturnDefaultResult() throws Exception {
		when(jwtTokenProvider.getUserIdFromToken("token-ok")).thenReturn("u1001");

		mockMvc.perform(get("/api/user/birthday/modification/check")
				.header("Authorization", "Bearer token-ok"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result.canModify").value(true));
	}
}
