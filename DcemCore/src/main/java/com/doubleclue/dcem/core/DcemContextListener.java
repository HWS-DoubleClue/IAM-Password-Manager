package com.doubleclue.dcem.core;

import java.io.IOException;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemConfiguration;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.dcem.core.weld.WeldSessionContext;
import com.doubleclue.dcem.system.logic.SystemModule;

/**
 * @author Emanuel Galea
 */
// WebListener This Web listener is configured in web.xml after WELD but before

public class DcemContextListener implements ServletContextListener {

	// private static final Logger logger = LogManager.getLogger(ContextListener.class);

	/**
	 * HERE WE START
	 * 
	 * @param event
	 */
	public final void contextInitialized(ServletContextEvent event) {

		WeldRequestContext requestContext = null;
		WeldSessionContext weldSessionContext = null;

		try {

			requestContext = WeldContextUtils.activateRequestContext();
			weldSessionContext = WeldContextUtils.activateSessionContext(null);
			Logger logger = LogManager.getLogger(DcemContextListener.class);

			DcemApplicationBean applicationBean = null;
			try {
				applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
				ConfigLogic configLogic = CdiUtils.getReference(ConfigLogic.class); 
				if (DcemApplicationBean.getInitExceptions().isEmpty()) {
					try {
						applicationBean.initialize(event.getServletContext());
						configLogic.reloadPortInUseAlerts();
						applicationBean.startModules();
					} catch (DcemException dcemException) {
						logger.error("Couldn't initiate application: ", dcemException);
						DcemApplicationBean.addInitException(dcemException);
					} catch (Exception exp) {
						logger.error("Couldn't initiate application: ", exp);
						DcemException dcemException1 = new DcemException(DcemErrorCodes.INIT_APPLICATION, "Couldn't initiate application: " + exp.getMessage());
						DcemApplicationBean.addInitException(dcemException1);
					}
				}

				if (DcemApplicationBean.jUnitTestMode == false) {
					logger.info("TimeZone: " + TimeZone.getDefault());
					// logger.info(DcemUtils.getSystemProperties());
				}
				if (DcemCluster.getInstance().getClusterConfig().isToSave()) {
					DcemCluster.getInstance().getClusterConfig().setToSave(false);
					DcemConfiguration dcemConfiguration = configLogic.createClusterConfig(DcemCluster.getInstance().getClusterConfig());
					configLogic.setDcemConfiguration(dcemConfiguration);
				}
				SystemModule systemModule = CdiUtils.getReference(SystemModule.class);
				String captchaPrivate = systemModule.getPreferences().getCaptchaPrivateKey();
				if (captchaPrivate != null && captchaPrivate.isEmpty() == false) {
					ServletContext servletContext = event.getServletContext();
					servletContext.setInitParameter("primefaces.PRIVATE_CAPTCHA_KEY", captchaPrivate);
					servletContext.setInitParameter("primefaces.PUBLIC_CAPTCHA_KEY", systemModule.getPreferences().getCaptchaPublicKey());
					applicationBean.setCaptchaOn(true);
				}

			} catch (Exception e) {
				logger.error("DcemContextListener.contextInitialized() ", e);
			}

		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
			WeldContextUtils.deactivateSessionContext(weldSessionContext);
			if (DcemApplicationBean.getInitExceptions().isEmpty() == false) {
				// TODO INIT Failes
				System.out.println("ContextListener.contextInitialized()");
			}

		}

	}

	protected void initializeLogger(ServletContext context) throws IOException {

	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
		applicationBean.stopModules();

	}

}
