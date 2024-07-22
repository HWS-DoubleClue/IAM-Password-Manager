

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.doubleclue.utils.KaraUtils;

class UrlEncodeTest {

	@Test
	void test() {
		Map<String, String> map = new HashMap<String,String>();
		map.put("k+ey= with ~space", "[value+~ 1=1^ - % ");
		map.put("secondkey", "secondValue");
		String url = KaraUtils.mapToUrlParamString(map);
		System.out.println("UrlEncodeTest.test() " + url);
		
		Map<String,String> mapResult = KaraUtils.urlParamStringToMap(url);
		assertTrue(areEqual(map, mapResult));
	}
	
	private boolean areEqual(Map<String, String> first, Map<String, String> second) {
	    if (first.size() != second.size()) {
	        return false;
	    }

	    return first.entrySet().stream()
	      .allMatch(e -> e.getValue().equals(second.get(e.getKey())));
	}

}
