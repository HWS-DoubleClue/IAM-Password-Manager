import java.util.Iterator;

import com.doubleclue.dcem.core.utils.rest.ClientRestApi;
import com.doubleclue.dcem.core.utils.rest.ClientRestApiParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SolrGetCollections {

	public static void main(String[] args) {
				
		ClientRestApi clientRestApi = new ClientRestApi();
		String url = "http://172.16.142.51:8983/solr/admin/cores?action=STATUS";
		ClientRestApiParams apiParams = new ClientRestApiParams(url, null, 10);
		try {
			clientRestApi.restGet(apiParams);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(apiParams.getResponseBody());
			JsonNode statusNode = rootNode.get("status");
			Iterator<JsonNode> cores = statusNode.elements();
			while (cores.hasNext()) {
				cores.next();
			}
		//sonNode coreNode = statusNode.get(coreName);
			System.out.println("SolrCreateCollection.main() " + statusNode.toString());
			System.out.println("SolrCreateCollection.main() " + apiParams.getResponseBody());
		} catch (Exception e) {
			System.out.println("SolrCreateCollection.main() Exception: " + apiParams.getResponseBody());
			e.printStackTrace();
		}
		
	//	curl -v http://localhost:8983/solr/admin/cores?action=UNLOAD&deleteInstanceDir=true&core=collectionName
		System.exit(0);
	}

}
