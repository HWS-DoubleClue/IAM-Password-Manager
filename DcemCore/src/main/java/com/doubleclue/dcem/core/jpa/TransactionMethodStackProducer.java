package com.doubleclue.dcem.core.jpa;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

/**
 * 
 * @author Emanuel Galea
 *
 */
@ApplicationScoped
public class TransactionMethodStackProducer {

	@Produces
	@RequestScoped
	public Deque<Method> getMethodStack() {
		return new ArrayDeque<Method>();
	}
}