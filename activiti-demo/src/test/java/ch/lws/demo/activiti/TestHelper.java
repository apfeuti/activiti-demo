package ch.lws.demo.activiti;

import java.sql.SQLException;

public class TestHelper {

	public static void startDBBrowser() {
		try {
			org.h2.tools.Server.createWebServer("-web").start();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
