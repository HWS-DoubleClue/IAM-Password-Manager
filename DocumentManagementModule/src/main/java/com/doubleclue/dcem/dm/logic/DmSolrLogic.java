package com.doubleclue.dcem.dm.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeTagEntity;
import com.doubleclue.dcem.as.logic.CloudSafeDto;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.utils.rest.ClientRestApi;
import com.doubleclue.dcem.core.utils.rest.ClientRestApiParams;
import com.doubleclue.dcem.dm.preferences.DmPreferences;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of SOLR search logic.
 */
@ApplicationScoped
public class DmSolrLogic {

	protected static final Logger logger = LogManager.getLogger(DmSolrLogic.class);

	@Inject
	DocumentManagementModule documentManagementModule;

	@Inject
	CloudSafeLogic cloudSafeLogic;
	
	@Inject 
	DocumentLogic documentLogic;
	
	static final String SEARCH_ALL = "(name:%s || description:%s || text:%s)";

	static final String SEARCH_WO_TAGS = "(name:%s || description:%s || text:%s)";

	@PostConstruct
	public void init() {
	}

	public void indexDocument(CloudSafeEntity cloudSafeEntity, String ocrText) throws Exception {
		// removeDocument(cloudSafeEntity.getId().toString());
		Http2SolrClient solrClient = getSolrClient();

		SolrInputDocument document = new SolrInputDocument();
		document.addField("id", cloudSafeEntity.getId());
		document.addField("parentId", cloudSafeEntity.getParent().getId());
		document.addField("name", cloudSafeEntity.getName());
		if (cloudSafeEntity.getInfo() != null && cloudSafeEntity.getInfo().isBlank() == false) {
			document.addField("description", cloudSafeEntity.getInfo());
		}
		if (cloudSafeEntity.getOwner() == CloudSafeOwner.USER) {
			document.addField("userId", cloudSafeEntity.getUser().getId());
		} else {
			document.addField("groupId", cloudSafeEntity.getGroup().getId());
		}
		if (ocrText != null) {
			document.addField("text", ocrText);
		}
		SortedSet<CloudSafeTagEntity> tags = cloudSafeLogic.getTagsSafely(cloudSafeEntity);
		if (tags != null && tags.isEmpty() == false) {
			Set<String> tagsText = tags.stream().map(tag -> tag.getId().toString()).collect(Collectors.toSet());
			document.addField("tags", tagsText);
		}

		solrClient.add(document);
		solrClient.commit();
	}

	public List<Integer> searchDocuments(List<CloudSafeTagEntity> tags, String query, Integer userId, List<DcemGroup> groups, String idFilter, int maxResults)
			throws Exception {
		Http2SolrClient solrClient = getSolrClient();
		boolean solrSearch = false;
		char ch; // check if direct solr or own simple search
		for (int i = 0; i < query.length(); i++) {
			ch = query.charAt(i);
			if (ch == ':' || ch == '~' || ch == '^' || ch == '*' || ch == '?') {
				solrSearch = true;
				break;
			}
		}
		SolrQuery solrQuery = new SolrQuery();
		String search;
		if (solrSearch == false) {
			if (query.isEmpty() == false) {
				search = "*" + query + "*";
			} else {
				search = query;
			}
			if (tags.isEmpty()) {
				search = SEARCH_ALL.formatted(search, search, search, search);
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				for (CloudSafeTagEntity tag : tags) {
					if (sb.length() > 2) {
						sb.append(" OR ");
					}
					sb.append("tags:");
					sb.append(tag.getId());
				}
				sb.append(")");
				if (query.isEmpty() == true) {
					search = sb.toString();
				} else {
					search = SEARCH_WO_TAGS.formatted(search, search, search);
					search = search + " AND " + sb.toString();
				}
			}
		} else {
			search = query; // direct to solr search
		}

		String groupFilter = "";
		if (groups != null) {
			StringBuilder sb = new StringBuilder();
			if (groups.isEmpty() == false) {
				sb.append(" OR groupId:(");
				for (int i = 0; i < groups.size(); i++) {
					if (i > 0 && i < groups.size()) {
						sb.append(" OR ");
					}
					DcemGroup group = groups.get(i);
					sb.append(group.getId());

				}
				sb.append(")");
			}
			groupFilter = sb.toString();
		}
		if (idFilter != null) {
			solrQuery.setFilterQueries("id:" + idFilter + groupFilter);
		} else {
			solrQuery.setFilterQueries("userId:" + userId + groupFilter);
		}
		solrQuery.setQuery(search);
		solrQuery.addSort("score", SolrQuery.ORDER.desc);
		solrQuery.setRows(maxResults + 1);
		solrQuery.setShowDebugInfo(true);
		QueryResponse response = solrClient.query(solrQuery);
		List<Integer> documentIds = new ArrayList<>();
		if (logger.isDebugEnabled()) {
			showDebug("Solr Debug Map", response.getDebugMap());
		}

		for (SolrDocument document : response.getResults()) {
			Object idField = document.getFieldValue("id");
			documentIds.add(Integer.valueOf(idField.toString()));
		}
		if (logger.isDebugEnabled()) {
			showDebug("Solr Explain", response.getExplainMap());
		}
		return documentIds;
	}

	private void showDebug(String mapName, Map<String, Object> debugMap) {
		logger.debug(mapName);
		for (Map.Entry<String, Object> entry : debugMap.entrySet()) {
			logger.debug("SOLR Debug Info: " + entry.getKey() + " = " + entry.getValue());
		}
	}

	public void removeDocument(String documentId) throws Exception {
		Http2SolrClient solrClient = getSolrClient();
		solrClient.deleteById(documentId);
		solrClient.commit();
	}

	public void indexUserDocuments(List<CloudSafeDto> dtos, DcemUser dcemUser, List<DcemGroup> groups) throws Exception {
		for (CloudSafeDto dto : dtos) {
			if (dto.isFolder() == true) {
				continue; // list contains all sub files
			}
			CloudSafeEntity cloudSafeEntity = cloudSafeLogic.getCloudSafe(dto.getId());
			indexUserDocument(cloudSafeEntity, dcemUser, groups);
		}
		List<CloudSafeEntity> trashedFiles = cloudSafeLogic.getCloudSafeByUserFlat(dcemUser, groups, true);
		List<CloudSafeDto> list = new ArrayList<>(trashedFiles.size());
		for (CloudSafeEntity cloudSafeEntity2 : trashedFiles) {
			list.add(new CloudSafeDto(cloudSafeEntity2));
		}
		removeDocumentsIndex(list);
	}
//todo 
	public void indexUserDocument(CloudSafeEntity cloudSafeEntity, DcemUser dcemUser, List<DcemGroup> groups) throws Exception {
		String ocrText = null;
		if (cloudSafeEntity.isFolder()) {
			List<CloudSafeEntity> entities = cloudSafeLogic.getCloudSafeByUserAndParentId(cloudSafeEntity.getId(), dcemUser, groups, false);
			for (CloudSafeEntity entity : entities) {
				indexUserDocument(entity, dcemUser, groups);
			}
		} else {
			ocrText = null;
			if (cloudSafeEntity.getTextLength() > 0) {
				File contentFile = documentLogic.getDocumentContent(cloudSafeEntity);
				ocrText = documentLogic.generateOcr(cloudSafeEntity, contentFile);
			}
			indexDocument(cloudSafeEntity, ocrText);
		}
	}

	public void removeDocumentsIndex(List<CloudSafeDto> cloudSafeDtos) throws Exception {
		Http2SolrClient solrClient = getSolrClient();
		List<String> failedRemovals = new ArrayList<>();
		for (CloudSafeDto cloudSafeDto : cloudSafeDtos) {
			if (cloudSafeDto.isFolder() == false) {
				try {
					solrClient.deleteById(String.valueOf(cloudSafeDto.getId()));
				} catch (Exception e) {
					failedRemovals.add(String.valueOf(cloudSafeDto.getId())); // Track failed removals
				}
			}
		}
		try {
			solrClient.commit();
		} catch (Exception e) {
			throw new Exception("Failed to commit Solr delete operation after batch removal", e);
		}
		if (failedRemovals.isEmpty() == false) {
			throw new Exception("Failed to remove the following documents from Solr: " + String.join(", ", failedRemovals));
		}
	}

	private Http2SolrClient getSolrClient() throws Exception {
		Http2SolrClient solrClient = documentManagementModule.getDmTenantData().getSolrClient();
		if (solrClient == null) {
			solrClient = initializeSolr(TenantIdResolver.getCurrentTenant());
		}
		return solrClient;
	}

	public SolrCore getSolrCore(String coreName) throws Exception {
		DmPreferences preferences = documentManagementModule.getMasterPreferences();
		ClientRestApi clientRestApi = new ClientRestApi();
		String url = preferences.getSolrUrl() + "admin/cores?action=STATUS&core=" + coreName;
		ClientRestApiParams apiParams = new ClientRestApiParams(url, null, 10);
		clientRestApi.restGet(apiParams);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(apiParams.getResponseBody());
		JsonNode statusNode = rootNode.get("status");
		// System.out.println("DmSolrLogic.getSolrCore() " + statusNode);
		JsonNode coreNode = statusNode.get(coreName);
		if (coreNode == null || coreNode.get("name") == null) {
			return null;
		}
		return new SolrCore(coreNode);
	}

	public void createSolrCore(String coreName) throws Exception {
		ClientRestApi clientRestApi = new ClientRestApi();
		String url = documentManagementModule.getMasterPreferences().getSolrUrl() + "admin/cores?action=CREATE&name=" + coreName
				+ "&configSet=doubleclue_configs";
		ClientRestApiParams apiParams = new ClientRestApiParams(url, null, 4);
		clientRestApi.restGet(apiParams);
		// System.out.println("SolrCreateCollection.main() " + apiParams.getResponseBody());
	}

	public void deleteSolrCore() {

	}

	public Http2SolrClient initializeSolr(TenantEntity tenantEntity) throws Exception {
		DmPreferences preferences = documentManagementModule.getMasterPreferences();
		String solrUrl = preferences.getSolrUrl();
		if (solrUrl == null || solrUrl.isEmpty()) {
			logger.warn("Solr URL not configured");
			throw new DcemException(DcemErrorCodes.SOLR_NOT_CONFIGURED, solrUrl);
		}
		String coreName = "dcem-" + DcemCluster.getInstance().getClusterConfig().getGivenName() + "-" + tenantEntity.getName();
		SolrCore solrCore = getSolrCore(coreName);
		if (solrCore == null) {
			logger.info("Solr-Core does not exists for " + coreName);
			createSolrCore(coreName);
			solrCore = getSolrCore(coreName);
		}
		logger.info("Solr-Core: " + solrCore);
		Http2SolrClient solrClient = new Http2SolrClient.Builder(solrUrl + coreName).withConnectionTimeout(5, TimeUnit.SECONDS).build();
		documentManagementModule.getDmTenantData().setSolrClient(solrClient);
		return solrClient;
	}

}
