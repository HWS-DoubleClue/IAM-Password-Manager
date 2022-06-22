import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HashMapSerialization {

	public static void main(String[] args) {
		ObjectMapper mapper = new ObjectMapper();
		// prepare a smple Map
		Map<String, String> map = new Hashtable<String, String>();
		map.put("key1", "value1");
		map.put("key2", "value3");
		try {
			/*
			 * Serialize from Map to String 
			 */
			String data = mapper.writeValueAsString(map);
			System.out.println("HashMapSerialization.main() Data: " + data);
			/*
			 * Now deserialization			  
			 */			
			TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
			Map<String, String> outputMap = mapper.readValue(data, typeRef);
			System.out.println("HashMapSerialization.main() Map=" + outputMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		} 
		System.exit(0);
	}
}
