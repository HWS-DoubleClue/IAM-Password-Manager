package com.doubleclue.dcem.core.logic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.entities.DcemStatistic;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.StatisticCounter;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.tasks.CallGetLogFiles;
import com.doubleclue.dcem.core.tasks.CallGetStatistics;
import com.doubleclue.dcem.core.tasks.ResetDiagCountersTask;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiExecutionCallback;

@ApplicationScoped
@Named("diagnosticLogic")
public class DiagnosticLogic implements MultiExecutionCallback {

	private static final Logger logger = LogManager.getLogger(DiagnosticLogic.class);
	
	public final static String CURRENT_TIME = "Current Time";

	private DateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");

	private DateFormat dateFormatFile = new SimpleDateFormat("HH_mm_dd_MM_yyyy");

	@Inject
	EntityManager em;

	/**
	 * @return
	 */
	public List<ModuleStatistic> getNodeStatistics(boolean withStaticValues) {
//		if (DcemCluster.getDcemCluster().isMgtOnly()) {
//			return null;
//		}
		DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
		List<ModuleStatistic> list = new LinkedList<ModuleStatistic>();
		ModuleStatistic moduleStatistic;

		for (DcemModule module : applicationBean.getSortedModules()) {
			moduleStatistic = new ModuleStatistic(module.getId());
			moduleStatistic.counters = module.getStatisticCounters();
			moduleStatistic.values = module.getStatisticValues();
			if (withStaticValues) {
				moduleStatistic.staticValues = module.getStaticValues();
			}
			if (moduleStatistic.isEmpty() == false) {
				list.add(moduleStatistic);
			}
		}
		return list;
	}

	public Map<String, List<ModuleStatistic>> collect(boolean resetPeriod, boolean globalReset) {

		IExecutorService executorService = DcemCluster.getDcemCluster().getExecutorService();
		Callable<List<ModuleStatistic>> callStatistic = new CallGetStatistics();
		Map<Member, Future<List<ModuleStatistic>>> futures = executorService.submitToAllMembers(callStatistic);
		Map<String, List<ModuleStatistic>> map = new HashMap<>();

		for (Member member : futures.keySet()) {
			String nodeName = member.getStringAttribute(DcemConstants.NODE_NAME_ATTRIBUTE);
			try {
				map.put(nodeName, futures.get(member).get());
			} catch (Exception e) {
				logger.warn(e);
			}
		}
		return map;
	}

	@DcemTransactional
	public void saveNodeStatistics(LocalDateTime date) throws JsonProcessingException {
		DcemStatistic semStatistic = new DcemStatistic();
		semStatistic.setTimestamp(removeSeconds(date));
		semStatistic.setDcemNode(DcemCluster.getDcemCluster().getDcemNode());
		ObjectMapper objectMapper = new ObjectMapper();

		String data = objectMapper.writeValueAsString(getNodeStatistics(false));
		semStatistic.setData(data);
		em.persist(semStatistic);
	}

	public void resetCounters() {
		IExecutorService executorService = DcemCluster.getDcemCluster().getExecutorService();
		executorService.submitToAllMembers(new ResetDiagCountersTask(), this);

	}

	@Override
	public void onResponse(Member member, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onComplete(Map<Member, Object> values) {
		// TODO Auto-generated method stub

	}
	
	public String getDiagnosticFile(String diagnosticTime) throws Exception {
		Map<String, List<ModuleStatistic>> statistics;
		if (diagnosticTime.equals(DiagnosticLogic.CURRENT_TIME)) {
			statistics = collect(false, false);
		} else {
			statistics = getNodeStatisticDb(diagnosticTime);
		}
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(statistics);
	}

	public List<SelectItem> getDiagnosticTimes() {
		TypedQuery<LocalDateTime> query = em.createNamedQuery(DcemStatistic.GET_TIMESTAMPS, LocalDateTime.class);
		query.setMaxResults(4000);
		List<LocalDateTime> timestamps = query.getResultList();
		ArrayList<SelectItem> selectItems = new ArrayList<>(timestamps.size() + 1);
		selectItems.add(new SelectItem(CURRENT_TIME, CURRENT_TIME));
		for (LocalDateTime date : timestamps) {
			selectItems.add(new SelectItem(getDateFormat().format(date), getDateFormat().format(date)));
		}
		return selectItems;
	}

	@DcemTransactional
	public int deleteDiagnostics() {
		return em.createQuery("DELETE FROM DcemStatistic st where st.id > 0").executeUpdate();
	}

	public Map<String, List<ModuleStatistic>> getNodeStatisticDb(String timeString) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<LinkedList<ModuleStatistic>> typeRef = new TypeReference<LinkedList<ModuleStatistic>>() {
		};
		Date date = getDateFormat().parse(timeString);
		Map<String, List<ModuleStatistic>> map = new HashMap<>();
		TypedQuery<DcemStatistic> query = em.createNamedQuery(DcemStatistic.GET_STATISTICS_TIME, DcemStatistic.class);
		query.setParameter(1, date);

		List<DcemStatistic> statistics = query.getResultList();
		List<ModuleStatistic> moduleStatistics;
		for (DcemStatistic semStatistic : statistics) {
			try {
				moduleStatistics = objectMapper.readValue(semStatistic.getData(), typeRef);
				map.put(semStatistic.getDcemNode().getName(), moduleStatistics);
			} catch (Exception e) {
				logger.warn(e);
			}
		}
		return map;
	}
	
	public List<DcemStatistic> getStatisticsFromTo (Date dateFrom, Date dateTo) {
		TypedQuery<DcemStatistic> query = em.createNamedQuery(DcemStatistic.GET_STATISTICS_FROM_TO, DcemStatistic.class);
		query.setParameter(1, dateFrom);
		query.setParameter(2, dateTo);
		query.setMaxResults(4000);
		return query.getResultList();
	}
	

	/**
	 * @param counterFilters
	 * @return
	 * @throws Exception
	 */
	public Map<String, List<ChartData>> getChartValues(Set<String> counterFilters, String dateFromStr, String dateToStr) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<LinkedList<ModuleStatistic>> typeRef = new TypeReference<LinkedList<ModuleStatistic>>() {
		};
		Map<String, List<ChartData>> result = new HashMap<>();
		if (dateFromStr == null) {
			return result;
		}
		Date dateFrom = getDateFormat().parse(dateFromStr);
		Date dateTo = getDateFormat().parse(dateToStr);
		List<DcemStatistic> statistics = getStatisticsFromTo(dateFrom, dateTo);
		List<ModuleStatistic> moduleStatistics;
		Map<String, String> valueMap;
		List<ChartData> listChartData;
		String value;
		Map<String, ChartData> mapNodeCounter = new HashMap<String, ChartData>();
		for (DcemStatistic dcemStatistic : statistics) {
			moduleStatistics = objectMapper.readValue(dcemStatistic.getData(), typeRef);
			for (ModuleStatistic moduleStatistic : moduleStatistics) {
				valueMap = moduleStatistic.getValues();
				for (String counterFilter : counterFilters) {
					value = valueMap.get(counterFilter);
					if (value == null) {
						continue;
					}
					Double numValue;
					try {
						if (value.contains("KB")) {
							String cutValue = value.substring(0, value.length() - 2);
							cutValue = cutValue.replace(",", "");
							numValue = Double.parseDouble(cutValue);
						} else {
							numValue = Double.parseDouble(value);
						}
					} catch (Exception exp) {
						continue;
					}
					listChartData = result.get(counterFilter);
					if (listChartData == null) {
						listChartData = new ArrayList<ChartData>(statistics.size());
					}
					ChartData chartData = mapNodeCounter.get(counterFilter);
					if (chartData == null) {
						chartData = new ChartData(dcemStatistic.getTimestamp(), dcemStatistic.getDcemNode().getName(), numValue);
						mapNodeCounter.put(counterFilter, chartData);
						listChartData.add(chartData);
					} else {
						if (chartData.getDate().equals(dcemStatistic.getTimestamp()) == false) {
							chartData = new ChartData(dcemStatistic.getTimestamp(),
									dcemStatistic.getDcemNode().getName(), numValue);
						} else {
							chartData.addNodeNumber(dcemStatistic.getDcemNode().getName(), numValue);
						}
						listChartData.add(chartData);
					}
					result.put(counterFilter, listChartData);
				}
			}
		}
		return result;

	}

	public Map<String, List<ChartCountersData>> getChartCounters(List<String> counterFilters, String nodeName, String dateFromStr, String dateToStr)
			throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<LinkedList<ModuleStatistic>> typeRef = new TypeReference<LinkedList<ModuleStatistic>>() {
		};
		Map<String, List<ChartCountersData>> result = new HashMap<>();
		if (dateFromStr == null) {
			return result;
		}
		Date dateFrom = getDateFormat().parse(dateFromStr);
		Date dateTo = getDateFormat().parse(dateToStr);
				
		List<DcemStatistic> statistics = getStatisticsFromTo(dateFrom, dateTo);
		
		
		List<ModuleStatistic> moduleStatistics;
		Map<String, StatisticCounter> counterMap;
		List<ChartCountersData> listChart;
		StatisticCounter statisticCounter;
		String counterNode;
		for (DcemStatistic dcemStatistic : statistics) {
			moduleStatistics = objectMapper.readValue(dcemStatistic.getData(), typeRef);
			for (ModuleStatistic moduleStatistic : moduleStatistics) {
				counterMap = moduleStatistic.getCounters();
				for (String counterFilter : counterFilters) {
					counterNode = counterFilter + "-" + dcemStatistic.getDcemNode().getName();
					statisticCounter = counterMap.get(counterFilter);
					if (statisticCounter != null) {
						listChart = result.get(counterNode);
						if (listChart == null) {
							listChart = new ArrayList<>(statistics.size());
						}
						listChart.add(new ChartCountersData(dcemStatistic.getTimestamp(), statisticCounter));
						result.put(counterNode, listChart);
					}
				}
			}
		}
		return result;
	}

	private LocalDateTime removeSeconds(LocalDateTime date) {
		return date.truncatedTo(ChronoUnit.SECONDS);
	}

	public DateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public DateFormat getDateFormatFile() {
		return dateFormatFile;
	}

	public void setDateFormatFile(DateFormat dateFormatFile) {
		this.dateFormatFile = dateFormatFile;
	}

	public byte[] getLogFiles() throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zipOutputStream = new ZipOutputStream(baos);
		zipOutputStream.setLevel(Deflater.BEST_SPEED);
		// String name = DcemCluster.getDcemCluster().getNodeName() + "_" +
		// dateFormatFile.format(new Date()) + "_"
		// + DcemConstants.LOG_FILE_NAME;
		File file = new File(LocalPaths.getDcemLogDir(), DcemConstants.LOG_FILE_NAME);
		if (file.exists()) {
			DcemUtils.zipFile(zipOutputStream, baos, file,
					DcemCluster.getDcemCluster().getNodeName() + DcemConstants.LOG_FILE_NAME,
					DcemConstants.MAX_FILE_LENGTH);
		}
		file = new File(LocalPaths.getDcemLogDir(), DcemConstants.LOG_FILE_NAME_1);
		if (file.exists()) {
			DcemUtils.zipFile(zipOutputStream, baos, file,
					DcemCluster.getDcemCluster().getNodeName() + DcemConstants.LOG_FILE_NAME_1,
					DcemConstants.MAX_FILE_LENGTH);
		}

		file = new File(LocalPaths.getDcemLogDir(), DcemConstants.LOG_TOMCAT_FILE_NAME);
		if (file.exists()) {
			DcemUtils.zipFile(zipOutputStream, baos, file,
					DcemCluster.getDcemCluster().getNodeName() + DcemConstants.LOG_TOMCAT_FILE_NAME,
					DcemConstants.MAX_FILE_LENGTH);
		}
		file = new File(LocalPaths.getDcemLogDir(), DcemConstants.LOG_TOMCAT_FILE_NAME_1);
		if (file.exists()) {
			DcemUtils.zipFile(zipOutputStream, baos, file,
					DcemCluster.getDcemCluster().getNodeName() + DcemConstants.LOG_TOMCAT_FILE_NAME_1,
					DcemConstants.MAX_FILE_LENGTH);
		}
		try {
			zipOutputStream.close();
		} catch (IOException e) {
		}
		return baos.toByteArray();
	}

	public void writeClusterLogFiles(OutputStream output) throws IOException, InterruptedException, ExecutionException {
		IExecutorService executorService = DcemCluster.getDcemCluster().getExecutorService();
		Callable<byte[]> callStatistic = new CallGetLogFiles();

		ZipOutputStream zos = new ZipOutputStream(output);
		zos.setLevel(Deflater.BEST_SPEED);
		Set<Member> members = DcemCluster.getInstance().getMembers();
		ZipInputStream zipInputStream = null;
		ByteArrayInputStream baip;
		ZipEntry zipEntry;
		int length;
		byte[] buffer = new byte[8192];
		for (Member member : members) {
			Future<byte[]> future = executorService.submitToMember(callStatistic, member);
			baip = new ByteArrayInputStream(future.get());
			zipInputStream = new ZipInputStream(baip);
			while ((zipEntry = zipInputStream.getNextEntry()) != null) {
				zos.putNextEntry(zipEntry);
				while ((length = zipInputStream.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
					zos.flush();
				}
			}
		}
		zos.close();
		output.close();
		zipInputStream.close();
	}

	@DcemTransactional
	public void cleanMonitoringRecords(int maxMonitoringRecords) {

		// Get the timestamp of the first record beyond the limit
		TypedQuery<Date> timestampQuery = em.createNamedQuery(DcemStatistic.GET_TIMESTAMPS, Date.class);
		timestampQuery.setFirstResult(maxMonitoringRecords);
		timestampQuery.setMaxResults(1);
		List<Date> timestamps = timestampQuery.getResultList();

		if (timestamps.size() == 1) { // if the record is found (i.e. limit
										// breached)
			Query deleteQuery = em.createQuery("DELETE FROM DcemStatistic ds WHERE ds.timestamp <= ?1");
			deleteQuery.setParameter(1, timestamps.get(0));
			deleteQuery.executeUpdate();
		}
	}
}
