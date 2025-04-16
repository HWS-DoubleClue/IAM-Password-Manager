package com.doubleclue.dcem.dm.logic;

import org.apache.solr.client.solrj.impl.Http2SolrClient;

import com.doubleclue.dcem.core.logic.module.ModuleTenantData;


public class DmTenantData extends ModuleTenantData {

	private Http2SolrClient solrClient;

	public Http2SolrClient getSolrClient() {
		return solrClient;
	}

	public void setSolrClient(Http2SolrClient solrClient) {
		this.solrClient = solrClient;
	}
	

}
