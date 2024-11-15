import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.util.NamedList;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;

public class TestSolr {

	public static void main(String[] args) {
		String urlString = "http://172.16.142.51:8983/solr/doubleclue-master";
		Http2SolrClient solrClient = new Http2SolrClient.Builder(urlString).build();
		// solr.setParser(new XMLResponseParser());

		try {
			SolrInputDocument document = new SolrInputDocument();
			document.addField("id", "123456");
			document.addField("name", "Kenmore Dishwasher");
			document.addField("price", "599.99");
			document.addField("text", "ÖÄÜxxxxxxxxxxxxxxxöäü");
			solrClient.add(document);
			BodyContentHandler textHandler = new BodyContentHandler();
			ParseContext context = new ParseContext();
			try {
				// autoParser.parse(input, textHandler, metadata, context);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("TestSolr.main() EXIT");
				System.exit(-1);
			}
			solrClient.commit();
		} catch (Exception e) {
			System.out.println("TestSolr.main() EXIT " + e.toString());
			e.printStackTrace();
			System.exit(0);
		}

		try {
			SolrQuery query = new SolrQuery();
			//query.set("q", "price:599.99");
			query.set("q", "text:ÖÄÜxxxxxxxxxxxxxxxöäü");
			QueryResponse response = solrClient.query(query);

			SolrDocumentList docList = response.getResults();
			if (docList.isEmpty()) {
				System.out.println("TestSolr.main() " + " No results");
			}
			// assertEquals(docList.getNumFound(), 1);

			for (SolrDocument doc : docList) {
				System.out.println("TestSolr.main() " + doc.getFieldValue("id"));
				System.out.println("TestSolr.main() " + doc.getFieldValue("price"));
				System.out.println("TestSolr.main() Nanme: " + doc.getFieldValue("name"));
				System.out.println("TestSolr.main() text=" + doc.getFieldValue("contents"));
			}

			// solrClient.setParser(new XMLResponseParser());
			// File file = new File("C:\\Temp\\tesseract\\adidas.txt");
			//
			// ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("/update/extract");
			//
			// // req.addFile(file, "application/pdf");//change the content type for different input files
			// req.addFile(file, "text/plain");
			// String fileName = file.getName();
			// req.setParam("literal.id", fileName);
			// req.setAction(req.getAction().COMMIT, true, true);
			// NamedList<Object> result = solrClient.request(req);
			// int status = (Integer) ((org.apache.solr.common.util.SimpleOrderedMap) (result.get("responseHeader"))).get("status");
			//
			// System.out.println("Result: " +result);
			// System.out.println("solr query"+ solrClient.query(new SolrQuery("*.*")));

			// System.out.println("TestTess4.main() " + result);
		} catch (Exception e) {
			System.out.println("TestSolr.main() " + e.toString());
			e.printStackTrace();
		}
		System.exit(0);
	}

}
