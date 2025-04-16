import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrDocumentList;

public class SolrSchemaTest {

    public static void main(String[] args) {
        String solrUrl = "http://172.16.142.51:8983/solr/doubleclue-master";
        try (Http2SolrClient solrClient = new Http2SolrClient.Builder(solrUrl).build()) {
            // Add a document
        	SolrInputDocument document = new SolrInputDocument();
        	document.addField("id", "1");
        	document.addField("owner", "John Doe");
        	document.addField("user", "jdoe");
        	document.addField("group", "Admins");
        	document.addField("name", "Test Document");
        	document.addField("info", "This is a test document.");
        	document.addField("tags", new String[]{"tag1", "tag2", "tag3"});    
        	document.addField("textExtract", "This is the extracted text.");
        	solrClient.add(document);
        	solrClient.commit();
            System.out.println("Document added successfully.");

            // Query the document
            SolrQuery query = new SolrQuery();
            query.setQuery("id:1");
            QueryResponse response = solrClient.query(query);

            // Verify the results
            SolrDocumentList results = response.getResults();
            if (results.isEmpty()) {
                System.out.println("No document found.");
            } else {
                System.out.println("Document found: " + results);
                results.forEach(doc -> {
                    System.out.println("ID: " + doc.getFieldValue("id"));
                    System.out.println("Owner: " + doc.getFieldValue("owner"));
                    System.out.println("Tags: " + doc.getFieldValue("tags"));
                    System.out.println("Last Modified: " + doc.getFieldValue("lastModified"));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
