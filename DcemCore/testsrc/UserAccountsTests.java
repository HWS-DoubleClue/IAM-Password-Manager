import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.doubleclue.dcem.core.gui.UserAccount;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserAccountsTests {
	
	public static void main(String[] args) throws IOException {
		
		String user = "dom1\\emanuel=.tet";
	
		List<UserAccount> list = new ArrayList<>();
		UserAccount userAccount = new UserAccount(user, 44, "fingerPrint",  123456);
		list.add(userAccount);
		ObjectMapper objectMapper = new ObjectMapper();
	//	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		String output = objectMapper.writeValueAsString(list);
		System.out.println("UserAccountsTests " + output);
		
		
		TypeReference<List<UserAccount>> typeRef = new TypeReference<List<UserAccount>>() {
		};
		list = objectMapper.readValue (output, typeRef);
		
		System.out.println("UserAccountsTests " + list.toString());
		
		System.exit(1);
		
//		LicenceContent licenceContent = new LicenceContent("this is clusterid", "customer", new Date(), map);
//		
//		ObjectMapper objectMapper = new ObjectMapper();
//		try {
//			String json = objectMapper.writeValueAsString(licenceContent);
//			System.out.println(json);
//			LicenceContent licenceContentRead =objectMapper.readValue(json, LicenceContent.class);
//			System.out.println("CreateLicenceKey.main() " + licenceContentRead.toString());
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		System.exit(0);		
		
	}

}
