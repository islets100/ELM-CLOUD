package team.tjusw.elm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import team.tjusw.elm.po.Business;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.service.BusinessService;

@RestController
@RequestMapping("/businesses")
public class BusinessController {
	@Autowired
	private BusinessService businessService;

	@GetMapping("/order-type/{orderTypeId}")
	public CommonResult<List<Business>> listBusinessByOrderTypeId(@PathVariable("orderTypeId") Integer orderTypeId)
			throws Exception {
		List<Business> ret = businessService.listBusinessByOrderTypeId(orderTypeId);
		return new CommonResult<List<Business>>(200, "ok", ret);
	}

	@GetMapping("/{businessId}")
	public CommonResult<Business> getBusinessById(@PathVariable("businessId") Integer businessId) throws Exception {
		Business ret = businessService.getBusinessById(businessId);
		if (ret == null) {
			return new CommonResult<Business>(404, "business not found", null);
		}
		return new CommonResult<Business>(200, "ok", ret);
	}

	@GetMapping("/user/{userId}")
	public CommonResult<List<Business>> listBusinessByUserId(@PathVariable("userId") String userId) throws Exception {
		List<Business> ret = businessService.listBusinessByUserId(userId);
		return new CommonResult<List<Business>>(200, "ok", ret);
	}

	@PostMapping
	public CommonResult<Business> createBusiness(@RequestBody Business business) throws Exception {
		Business ret = businessService.createBusiness(business);
		if (ret == null) {
			return new CommonResult<Business>(500, "create business failed", null);
		}
		return new CommonResult<Business>(201, "created", ret);
	}

	@PutMapping("/{businessId}")
	public CommonResult<Business> updateBusiness(@PathVariable("businessId") Integer businessId,
			@RequestBody Business business) throws Exception {
		Business ret = businessService.updateBusiness(businessId, business);
		if (ret == null) {
			return new CommonResult<Business>(404, "business not found", null);
		}
		return new CommonResult<Business>(200, "updated", ret);
	}
}
