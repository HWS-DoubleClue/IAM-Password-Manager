package com.doubleclue.dcem.core.jpa;

import javax.persistence.EntityManager;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.hazelcast.core.IList;

public class JpaEntityCache<T extends EntityInterface> {

	private final IList<T> cache;
	private final JpaEntityUpdater<T> updater;

	public JpaEntityCache(IList<T> cache, JpaEntityUpdater<T> updater) {
		this.cache = cache;
		this.updater = updater;
	}

	public void add(T entity) {
		cache.add(entity);
	}

	public int flush(EntityManager em) {
		int count = 0;
		if (cache.size() > 0) {
			boolean canUpdate = updater != null;
			
			for (T entity : cache) {
				boolean entityUpdated = canUpdate ? updater.updateToDb(entity) : false;
				if (entityUpdated == false) {
					try {
						em.persist(entity);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				count++;
			}
			cache.clear();
			if (canUpdate) {
				updater.onCacheFlushed();
			}
		}
		return count;
	}
}
