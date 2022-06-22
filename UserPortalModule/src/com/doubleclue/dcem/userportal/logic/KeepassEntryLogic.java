package com.doubleclue.dcem.userportal.logic;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.userportal.entities.KeepassEntryEntity;
import com.doubleclue.dcem.core.logic.AttributeTypeEnum;
import com.doubleclue.utils.KaraUtils;

@ApplicationScoped
@Named("keepassEntryLogic")
public class KeepassEntryLogic {

	@Inject
	EntityManager em;

	// private Logger logger = LogManager.getLogger(KeepassEntryLogic.class);

	@DcemTransactional
	public KeepassEntryEntity getKeepassEntry(String uuid) {
		return em.find(KeepassEntryEntity.class, uuid);
	}

	@DcemTransactional
	public void deleteKeepassEntry(String uuid) {
		KeepassEntryEntity keepassEntryEntity = getKeepassEntry(uuid);
		if (keepassEntryEntity == null) {
			return;
		}
		em.remove(keepassEntryEntity);
	}

	@DcemTransactional
	public void updateEntry(KeepassEntryEntity keepassEntryEntity) {
		KeepassEntryEntity existingEntry = em.find(KeepassEntryEntity.class, keepassEntryEntity.getUuid());
		if (keepassEntryEntity.getApplicationEntity() == null
				&& (keepassEntryEntity.getApplication() == null || keepassEntryEntity.getApplication().getActions().isEmpty())) {
			// save nothing
			if (existingEntry != null) {
				em.remove(existingEntry);
			}
			return;
		}
		if (existingEntry != null) {
			existingEntry.setApplication(keepassEntryEntity.getApplication());
			existingEntry.setApplicationEntity(keepassEntryEntity.getApplicationEntity());
			existingEntry.setName(keepassEntryEntity.getName());
			em.flush();
		} else {
			em.persist(keepassEntryEntity);
		}
	}

	@DcemTransactional
	public void migrate26() {
		TypedQuery<KeepassEntryEntity> query = em.createQuery("SELECT ahe FROM KeepassEntryEntity ahe", KeepassEntryEntity.class);
		List<KeepassEntryEntity> list = query.getResultList();
		for (KeepassEntryEntity keepassEntryEntity : list) {
			if (keepassEntryEntity.getApplication() != null && keepassEntryEntity.getApplication().getActions() != null) {
				for (AppHubAction appHubAction : keepassEntryEntity.getApplication().getActions()) {
					int i = KaraUtils.getNumeric(appHubAction.getValueSourceType());
					if (i == -1) {
						continue;
					} else {
						appHubAction.setValueSourceType(AttributeTypeEnum.values()[i].name());
					}
				}
			}
		}
	}

}
