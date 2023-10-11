package com.doubleclue.dcem.core.tasks;

import java.time.LocalDateTime;
import java.util.Date;


import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.hazelcast.core.IExecutorService;

public class MonitoringTask extends CoreTask {

	public MonitoringTask() {
		super (MonitoringTask.class.getSimpleName(), null);
	}
	
	@Override
	public void runTask() {
		if (DcemCluster.getDcemCluster().isClusterMaster() == false) {
			return;
		}
		IExecutorService executorService = DcemCluster.getDcemCluster().getExecutorService();
		executorService.execute(new MonitorClusterTask(LocalDateTime.now()));
	}

}
