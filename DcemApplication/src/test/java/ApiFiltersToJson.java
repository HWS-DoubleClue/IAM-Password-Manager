import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;

import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.jpa.ApiFilterItem.OperatorEnum;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.StandardCharset;

public class ApiFiltersToJson {
	
//	public static void main (String [] args) {
//		List<ApiFilterItem> filters = new ArrayList<>();
//		ApiFilterItem apiFilterItem = new ApiFilterItem ("title", "blabla", OperatorEnum.LIKE);
//		filters.add(apiFilterItem);
//		
//		ObjectMapper objectMapper = new ObjectMapper();
//		objectMapper.setSerializationInclusion(Include.NON_NULL);
//		try {
//			String output = objectMapper.writeValueAsString(filters);
//			System.out.println(output);
//			String encoded = URLEncoder.encode(output, StandardCharset.UTF_8);
//			System.out.println(encoded);
//			
//			System.exit(0);
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.exit(-1);
//		}
//		
//		
//		
//		
//	}

}
