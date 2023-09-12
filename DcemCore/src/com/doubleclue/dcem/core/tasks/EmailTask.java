package com.doubleclue.dcem.core.tasks;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.weld.CdiUtils;

public class EmailTask extends CoreTask {

	private static final Logger logger = LogManager.getLogger(EmailTask.class);

	List<DcemUser> users;
	Map<String, String> map;
	String templateName;

	public EmailTask(List<DcemUser> users, Map<String, String> map, String templateName) {
		super(EmailTask.class.getSimpleName(), null);
		this.users = users;
		this.map = map;
		this.templateName = templateName;
	}
	
	

	@Override
	public void runTask() {
		
		logger.info("Nightly run started");
		long start = System.currentTimeMillis();
		DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
			
		logger.info("Nightly run ends: " + (System.currentTimeMillis() - start));
	}

}
