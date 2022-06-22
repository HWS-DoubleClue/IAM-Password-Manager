package com.doubleclue.dcem.core.jpa;

import com.doubleclue.dcem.core.entities.EntityInterface;

public interface JpaEntityUpdater<T extends EntityInterface> {

	boolean updateToDb(T entity);

	void onCacheFlushed();
}
