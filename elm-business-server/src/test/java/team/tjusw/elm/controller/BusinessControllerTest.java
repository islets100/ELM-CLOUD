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

import team.tjusw.elm.po.Business;
import team.tjusw.elm.service.BusinessService;

@ExtendWith(MockitoExtension.class)
class BusinessControllerTest {

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private BusinessService businessService;

	@InjectMocks
	private BusinessController businessController;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(businessController).build();
	}

	@Test
	void createBusinessShouldReturnCreatedBusiness() throws Exception {
		Business business = new Business();
		business.setBusinessId(88);
		business.setBusinessName("Demo Shop");
		business.setBusinessAddress("Tianjin");
		business.setOrderTypeId(1);
		business.setStartPrice(new BigDecimal("20.00"));
		business.setDeliveryPrice(new BigDecimal("4.00"));
		business.setUserId("merchant1001");

		when(businessService.createBusiness(any(Business.class))).thenReturn(business);

		mockMvc.perform(post("/businesses")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(business)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(201))
				.andExpect(jsonPath("$.result.businessId").value(88))
				.andExpect(jsonPath("$.result.userId").value("merchant1001"));
	}

	@Test
	void updateBusinessShouldReturnUpdatedBusiness() throws Exception {
		Business business = new Business();
		business.setBusinessId(88);
		business.setBusinessName("Demo Shop Plus");
		business.setBusinessAddress("Beijing");
		business.setOrderTypeId(1);
		business.setStartPrice(new BigDecimal("25.00"));
		business.setDeliveryPrice(new BigDecimal("5.00"));
		business.setUserId("merchant1001");

		when(businessService.updateBusiness(eq(88), any(Business.class))).thenReturn(business);

		mockMvc.perform(put("/businesses/88")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(business)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.result.businessName").value("Demo Shop Plus"))
				.andExpect(jsonPath("$.result.businessAddress").value("Beijing"));
	}
}
