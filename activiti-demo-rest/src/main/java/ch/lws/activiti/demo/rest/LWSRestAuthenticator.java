package ch.lws.activiti.demo.rest;

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.rest.common.filter.RestAuthenticator;
import org.restlet.Request;

public class LWSRestAuthenticator implements RestAuthenticator {

	public boolean isRequestAuthorized(Request request) {
		return true;
	}

	public boolean requestRequiresAuthentication(Request request) {
		Authentication.setAuthenticatedUserId("Bruce");
		// request.getClientInfo().setUser(new User("Bruce"));
		return false;
	}

}
