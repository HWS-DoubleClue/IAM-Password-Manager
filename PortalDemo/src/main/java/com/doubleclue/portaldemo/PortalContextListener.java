package com.doubleclue.portaldemo;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.portaldemo.utils.ConfigUtil;

/**
 * @author Emanuel Galea
 */
// WebListener This Web listener is configured in web.xml after WELD but before

public class PortalContextListener implements ServletContextListener {

	// private static final Logger logger = LogManager.getLogger(ContextListener.class);

	/**
	 * HERE WE START
	 * 
	 * @param event
	 */
	public final void contextInitialized(ServletContextEvent event) {
		// System.out.println("PortalContextListener.contextInitialized()");

		PortalDemoConfig portalDemoConfig;
		try {
			portalDemoConfig = ConfigUtil.getPortalDemoConfig();
			AsClientRestApi.initilize(portalDemoConfig.getConnectionConfig());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		

	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

	}

}
