package team.tjusw.elm.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import team.tjusw.elm.po.Food;
import team.tjusw.elm.service.FoodService;

@ExtendWith(MockitoExtension.class)
class FoodControllerTest {

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private FoodService foodService;

	@InjectMocks
	private FoodController foodController;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(foodController).build();
	}

	@Test
	void addFoodShouldReturnSuccess() throws Exception {
		Food food = new Food();
		food.setFoodId(301);
		food.setFoodName("Fried Rice");
		food.setFoodExplain("demo food");
		food.setFoodPrice(new BigDecimal("18.50"));
		food.setBusinessId(88);
		food.setValid(1);

		when(foodService.addFood(any(Food.class))).thenReturn(true);

		mockMvc.perform(post("/foods")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(food)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result").value(true));
	}

	@Test
	void updateFoodShouldReturnUpdatedFood() throws Exception {
		Food food = new Food();
		food.setFoodId(301);
		food.setFoodName("Fried Rice Plus");
		food.setFoodExplain("updated food");
		food.setFoodPrice(new BigDecimal("20.00"));
		food.setBusinessId(88);
		food.setValid(1);

		when(foodService.updateFood(eq(301), any(Food.class))).thenReturn(food);

		mockMvc.perform(put("/foods/301")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(food)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result.foodId").value(301))
				.andExpect(jsonPath("$.result.foodName").value("Fried Rice Plus"));
	}
}
