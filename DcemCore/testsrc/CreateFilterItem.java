
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.jpa.FilterItem;
import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.core.jpa.GuiFilters;

public class CreateFilterItem {

	public static void main(String[] args) throws IOException {
		
		GuiFilters guiFilters = new GuiFilters();
//		guiFilters.setViewName("ViewName");
		ArrayList<FilterItem> filters = new ArrayList<FilterItem> ();

		FilterItem filterItem = new FilterItem("licenseOem.name", ((Object) "LANCOM"), ((Object) null), FilterOperator.EQUALS, 1, SortOrder.ASCENDING);
		StringWriter result = new StringWriter();
		filters.add(filterItem);
		guiFilters.setFilters(filters);
		try {

			JAXBContext jc = JAXBContext.newInstance(FilterItem.class);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(filterItem, result);

		} catch (JAXBException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("CreateFilterItem.main() " + result.toString());
		try {

			JAXBContext.newInstance(FilterItem.class);


		} catch (JAXBException e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

}