package team.tjusw.elm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import team.tjusw.elm.mapper.BusinessMapper;
import team.tjusw.elm.po.Business;
import team.tjusw.elm.service.BusinessService;

@Service
public class BusinessServiceImpl implements BusinessService {

	@Autowired
	private BusinessMapper businessMapper;

	@Override
	public Business getBusinessById(Integer businessId) {
		return businessMapper.getBusinessById(businessId);
	}

	@Override
	public List<Business> listBusinessByOrderTypeId(Integer orderTypeId) {
		return businessMapper.listBusinessByOrderTypeId(orderTypeId);
	}

	@Override
	public List<Business> listBusinessByUserId(String userId) {
		return businessMapper.listBusinessByUserId(userId);
	}

	@Override
	public Business createBusiness(Business business) {
		if (businessMapper.insertBusiness(business) <= 0) {
			return null;
		}
		return businessMapper.getBusinessById(business.getBusinessId());
	}

	@Override
	public Business updateBusiness(Integer businessId, Business business) {
		Business existing = businessMapper.getBusinessById(businessId);
		if (existing == null) {
			return null;
		}
		business.setBusinessId(businessId);
		if (businessMapper.updateBusiness(business) <= 0) {
			return null;
		}
		return businessMapper.getBusinessById(businessId);
	}
}
