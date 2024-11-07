import com.doubleclue.dcem.core.utils.rest.ClientRestApi;
import com.doubleclue.dcem.core.utils.rest.ClientRestApiParams;

public class SolrCreateCollection {

	public static void main(String[] args) {
				
		ClientRestApi clientRestApi = new ClientRestApi();
		String url = "http://172.16.142.51:8983/solr/admin/cores?action=CREATE&name=doubleclue-master&configSet=doubleclue_configs";
		ClientRestApiParams apiParams = new ClientRestApiParams(url, null, 10);
		try {
			clientRestApi.restGet(apiParams);
			System.out.println("SolrCreateCollection.main() " + apiParams.getResponseBody());
		} catch (Exception e) {
			System.out.println("SolrCreateCollection.main() " + apiParams.getResponseBody());
			e.printStackTrace();
		}
		System.exit(0);
	}

}
