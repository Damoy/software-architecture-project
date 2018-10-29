package com.xaamruda.bbm.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xaamruda.bbm.commons.json.JsonUtils;
import com.xaamruda.bbm.users.dbaccess.service.IUserService;
import com.xaamruda.bbm.users.identification.UserIdentificator;
import com.xaamruda.bbm.users.info.UserDataManager;
import com.xaamruda.bbm.users.model.UserCreationContainer;

/**
 * Entry point to users module. 
 */
@Component
public class UsersIOHandler {
	
	@Autowired
	private IUserService service;
	
	@Autowired
	public UsersIOHandler() {
		UserIdentificator.init(service);
		UserDataManager.init(service);
	}
	
	// TODO
	public boolean identifyUserByMailPlusPassword(String wholeUserData, String userMail, String userPassword){
		boolean exists = UserIdentificator.getInstance().identify(userMail, userPassword);
		
		if(!exists){
			UserDataManager.getInstance().storeNewUser(
					JsonUtils.getFromJson(wholeUserData, UserCreationContainer.class));
		}
		
		return exists;
	}
	
}