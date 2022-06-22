import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;

public class LocaleTest {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Arg is missing");
			System.exit(-1);
		}
		Locale.setDefault(new Locale("en"));
		TreeMap<String, Locale> set = new TreeMap<>();
		for (Locale locale : Locale.getAvailableLocales()) {
			System.out.println(
					locale.getLanguage() + ", DisplayLangauge=" + locale.getDisplayLanguage() + ", Country: " + locale.getCountry());
			set.put(locale.getLanguage(), locale);
		}
		Iterator<String> iter = set.keySet().iterator();

		String content = "package com.doubleclue.dcem.admin.resources;\n" + "import com.doubleclue.dcem.admin.logic.AdminModule;\n"
				+ "import com.doubleclue.dcem.core.logic.DbResourceBundle;\n" + "public class DbMsg_## extends DbResourceBundle {\n"
				+ "	 public DbMsg_##() {\n" + "		    super();\n" + "	 }\n" + "};\n";

		File fileEnum = new File(args[0] + "/DcemCore/src/com/doubleclue/dcem/admin/resources/DbMsg_Enum.java");
		FileOutputStream fosEnum = null;
		try {
			fosEnum = new FileOutputStream(fileEnum);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(-2);
		}
		Locale locale;
		while (iter.hasNext()) {
			String lang = iter.next();
			if (lang.isEmpty()) {
				continue;
			}
			FileOutputStream fos;
			try {
				File file = new File(args[0] + "/DcemCore/src/com/doubleclue/dcem/admin/resources/DbMsg_" + lang + ".java");
				// if (file.exists() == false) {
				// file.createNewFile();
				// }
				fos = new FileOutputStream(file);
				String fileContent = content.replace("##", lang);
				fos.write(fileContent.getBytes("UTF-8"));
				fos.close();
				locale = set.get(lang);
				fosEnum.write((locale.getDisplayLanguage() + "(new Locale(\"" + lang + "\")),\n").getBytes("UTF-8"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-2);
			}

		}
		try {
			fosEnum.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);

	}

}
