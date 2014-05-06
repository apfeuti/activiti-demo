package ch.lws.activiti.demo.rest;

import org.activiti.rest.service.application.ActivitiRestServicesApplication;

public class LWSActivitiRestServicesApplication extends
		ActivitiRestServicesApplication {

	public LWSActivitiRestServicesApplication() {
		setRestAuthenticator(new LWSRestAuthenticator());
	}

}
