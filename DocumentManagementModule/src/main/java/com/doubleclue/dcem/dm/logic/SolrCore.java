package com.doubleclue.dcem.dm.logic;

import com.fasterxml.jackson.databind.JsonNode;

public class SolrCore {
	
	public SolrCore(JsonNode coreNode) {
		JsonNode nameNode = coreNode.get("name");
		if (nameNode == null) {
			return;
		}
		name = coreNode.get("name").asText();
		instanceDir = coreNode.get("instanceDir").asText();		
		dataDir = coreNode.get("dataDir").asText();
		JsonNode indexNode = coreNode.get("index");
		numDocs = indexNode.get("numDocs").asInt();
		sizeInBytes = indexNode.get("sizeInBytes").asInt();
		version = indexNode.get("version").asInt();
	}
	public String name;
	public String instanceDir;
	public String dataDir;
	public int numDocs;
	public int version;
	public int sizeInBytes;
	
	@Override
	public String toString() {
		return "SolrCore [name=" + name + ", instanceDir=" + instanceDir + ", dataDir=" + dataDir + ", numDocs=" + numDocs + ", version=" + version
				+ ", sizeInBytes=" + sizeInBytes + "]";
	}
}
