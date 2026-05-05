package team.tjusw.elm.service;

import java.util.List;

import team.tjusw.elm.po.Business;

public interface BusinessService {
	public List<Business> listBusinessByOrderTypeId(Integer orderTypeId);

	public Business getBusinessById(Integer businessId);

	public List<Business> listBusinessByUserId(String userId);

	public Business createBusiness(Business business);

	public Business updateBusiness(Integer businessId, Business business);
}
