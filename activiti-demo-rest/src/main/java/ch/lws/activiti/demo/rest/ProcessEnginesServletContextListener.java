package ch.lws.activiti.demo.rest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.activiti.engine.ProcessEngines;

public class ProcessEnginesServletContextListener implements
		ServletContextListener {

	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		ProcessEngines.destroy();
	}

	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ProcessEngines.init();
	}

}
