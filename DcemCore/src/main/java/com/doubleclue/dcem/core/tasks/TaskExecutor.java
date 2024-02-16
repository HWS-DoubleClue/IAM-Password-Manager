package com.doubleclue.dcem.core.tasks;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ClusterConfig;
import com.hazelcast.core.Cluster;

/**
 * 
 * This class provides wrapper class of scheduleThreadPoolExecutor for executing any SEM Task(Thread).
 *  
 * @author 
 *
 */

@ApplicationScoped
public class TaskExecutor {

	private static final Logger logger = LogManager.getLogger(TaskExecutor.class);

	private ScheduledThreadPoolExecutor executor;

	private static final int KEEP_ALIVE_TIME = 300; // 5 Minutes
	private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

	@PostConstruct
	public void init() {
		ClusterConfig clusterConfig = DcemCluster.getInstance().getClusterConfig();
		int noOfParallelRequest = 4;
		if (clusterConfig != null) {
			noOfParallelRequest = DcemCluster.getInstance().getClusterConfig().getScaleFactor();
		}

		executor = new ScheduledThreadPoolExecutor(noOfParallelRequest);
		executor.setKeepAliveTime(KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT);
		executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

		if (logger.isDebugEnabled()) {
			logger.debug("DCEM task executor initialized successfully with core_pool_size:" + noOfParallelRequest + ", keep_alive_time:" + KEEP_ALIVE_TIME + " "
					+ KEEP_ALIVE_TIME_UNIT.name());
		}
	}

	private void logCurrentStateOfExecutor() {
		if (logger.isTraceEnabled()) {
			logger.trace("DCEM task executor current_pool_size:" + executor.getPoolSize() + ", current_active_ threads: " + executor.getActiveCount()
					+ ", current_queue_size:" + executor.getQueue().size());
		}
	}

	public void execute(Runnable runnable) {
		logCurrentStateOfExecutor();
		executor.execute(runnable);
	}

	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		logCurrentStateOfExecutor();
		return executor.schedule(command, delay, unit);
	}

	public ScheduledFuture<?> schedule(Callable<?> callable, long delay, TimeUnit unit) {
		logCurrentStateOfExecutor();
		return executor.schedule(callable, delay, unit);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		logCurrentStateOfExecutor();
		return executor.scheduleAtFixedRate(command, initialDelay, period, unit);
	}
	

	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long period, TimeUnit unit) {
		logCurrentStateOfExecutor();
		return executor.scheduleWithFixedDelay(command, initialDelay, period, unit);
	}

	public <T> Future<T> submit(Callable<T> task) {
		logCurrentStateOfExecutor();
		return executor.submit(task);
	}

	public Future<?> submit(Runnable task) {
		logCurrentStateOfExecutor();
		return executor.submit(task);
	}

	public <T> Future<T> submit(Runnable task, T result) {
		logCurrentStateOfExecutor();
		return executor.submit(task, result);
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		logCurrentStateOfExecutor();
		return executor.invokeAll(tasks);
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		logCurrentStateOfExecutor();
		return executor.invokeAll(tasks, timeout, unit);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		logCurrentStateOfExecutor();
		return executor.invokeAny(tasks);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		logCurrentStateOfExecutor();
		return executor.invokeAny(tasks, timeout, unit);
	}

	public boolean remove(Runnable task) {
		logCurrentStateOfExecutor();
		return executor.remove(task);
	}

	public int getActiveCount() {
		return executor.getActiveCount();
	}

	public long getCompletedTaskCount() {
		return executor.getCompletedTaskCount();
	}

	public long getTaskCount() {
		return executor.getTaskCount();
	}

	public int getPoolSize() {
		return executor.getPoolSize();
	}

	public int getLargestPoolSize() {
		return executor.getLargestPoolSize();
	}

	@PreDestroy
	public void preDestroySEMTaskExecutor() {

		if (logger.isDebugEnabled()) {
			logger.debug("Before shutdown SEM tasks core_pool_size:" + executor.getCorePoolSize() + ", current pool_size" + executor.getPoolSize()
					+ ", current active threads:" + executor.getActiveCount() + ", current queue_size :" + executor.getQueue().size());
		}

		executor.shutdown();

		try {
			if (executor.awaitTermination(60, TimeUnit.SECONDS)) {
				logger.info("SEM task executor completed units of work successfully.");
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Before shutdownNow SEM tasks core_pool_size:" + executor.getCorePoolSize() + ", current pool_size" + executor.getPoolSize()
							+ ", current active threads:" + executor.getActiveCount() + ", current queue_size :" + executor.getQueue().size());
				}

				logger.warn("Cannot execute tasks in allowed time period. trying to shutdown SEM executor now.");

				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			logger.error("Could not dispose SEM task Executor.", e);
		}
	}
}
