package com.doubleclue.dcem.core.jpa;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.utils.ExportTsv;

@ApplicationScoped
public class ExportRecords {

	@Inject
	EntityManager em;

	/**
	 * @param days
	 * @return
	 * @throws DcemException
	 */
	@DcemTransactional
	public String[] archive(int days, Class<?> klass, String searchQuery, String deleteQuery) throws DcemException {
		LocalDateTime localDateTime = LocalDateTime.now();
		localDateTime.minusDays(days);
		
		ExportTsv exportTsv;
		int position = 0;
		List<EntityInterface> list = getRecordsAfter(localDateTime, position, searchQuery);
		String[] result = null;
		try {
			if (list.size() > 0) {
				result = new String[2];
				exportTsv = new ExportTsv();
				result[0] = exportTsv.start(klass);
				while (list.size() > 0) {
					exportTsv.addList(list);
					position += list.size();
					list = getRecordsAfter(localDateTime, position, searchQuery);
				}
				exportTsv.close();
				// now delete the records
				Query query = em.createNamedQuery(deleteQuery);
				query.setParameter(1, localDateTime);
				query.executeUpdate();
				result[1] = Integer.toString(list.size() + position);
			}
			return result;
		} catch (Exception exp) {
			throw new DcemException(DcemErrorCodes.ARCHIVE, "couldn't archive Reports", exp);
		}
	}

	private List<EntityInterface> getRecordsAfter(LocalDateTime localDateTime, int position, String searchQuery) {
		TypedQuery<EntityInterface> query = em.createNamedQuery(searchQuery, EntityInterface.class);
		query.setFirstResult(position);
		query.setParameter(1, localDateTime);
		query.setMaxResults(DcemConstants.MAX_ARCHIVE_RECORDS);
		return query.getResultList();
	}
}
