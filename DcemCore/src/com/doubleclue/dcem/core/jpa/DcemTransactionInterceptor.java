package com.doubleclue.dcem.core.jpa;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Deque;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.JDBCException;
import org.hibernate.exception.ConstraintViolationException;

import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.utils.KaraUtils;
import com.mchange.v2.resourcepool.TimeoutException;

/**
 * Intercepter for database transaction
 * 
 * @author Emanuel Galea
 * 
 */
@Interceptor
@DcemTransactional
public class DcemTransactionInterceptor {

	private static final Logger logger = LogManager.getLogger(DcemTransactionInterceptor.class);

	@Inject
	EntityManager em;

	@Inject
	Deque<Method> methodStack;

	@Inject
	Event<Method> methodEvent;

	@AroundInvoke
	public Object aroundInvoke(InvocationContext ic) throws Exception {

		boolean firstInterceptorInChain = methodStack.isEmpty();
		Method method = ic.getMethod();
		EntityTransaction transaction = em.getTransaction();
		// System.out.println("DcemTransactionInterceptor.aroundInvoke() " + method.getName() + " firstinchain " + firstInterceptorInChain);

		try {
			methodStack.push(method);
			if (firstInterceptorInChain) {
				// is transaction already active?
				if (transaction.isActive()) {
					throw new RuntimeException("first interceptor in chain - but transaction already active before method invocation");
				}
				// System.out.println("DcemTransactionInterceptor.aroundInvoke() BEGIN " + method.getName() );
				transaction.begin();
			}
			final Object result = ic.proceed();
			if (firstInterceptorInChain) {
				if (transaction.isActive()) {
					// System.out.println("DcemTransactionInterceptor.aroundInvoke() COMMIT " + method.getName());
					transaction.commit();
					methodEvent.fire(ic.getMethod());
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("first interceptor in chain - but transaction not active any more after method invocation");
					}
				}
			}
			return result;
		} catch (JDBCException jdbcException) {
			logger.fatal("DB-Transaction Exception in " + method.getDeclaringClass().getName() + "." + method.getName() + ", Exception: "
					+ jdbcException.toString() + ", Cause: " + jdbcException.getCause());
			throw new DcemException(DcemErrorCodes.DATABASE_CONNECTION_ERROR, jdbcException.getMessage(), jdbcException);
		} catch (Exception exp) {
			if (logger.isDebugEnabled()) {
				logger.debug("DB-Transaction Exception in " + method.getDeclaringClass().getName() + "." + method.getName() + ", Exception: " + exp.toString()
						+ ", Cause: " + exp.getCause(), exp);
			} else {
				logger.info("DB-Transaction Exception in " + method.getDeclaringClass().getName() + "." + method.getName() + ", Exception: " + exp.toString()
						+ ", Cause: " + exp.getCause());
			}
			if (transaction.isActive()) {
				try {
					transaction.rollback();
					if (logger.isDebugEnabled()) {
						logger.debug("transaction rollback in " + method.getDeclaringClass().getName() + "." + method.getName() + ", transactionInstance: "
								+ transaction.hashCode() + ", transactionActive: " + transaction.isActive() + ", firstInterceptorInChain: "
								+ firstInterceptorInChain + ", methodStack :" + methodStack);
					}
				} catch (PersistenceException pe) {
					logger.error("failed rollback in " + method.getDeclaringClass().getName() + "." + method.getName() + ", transactionInstance: "
							+ transaction.hashCode() + ", transactionActive: " + transaction.isActive() + ", firstInterceptorInChain: "
							+ firstInterceptorInChain + ", methodStack :" + methodStack);
				}
			}
			if (exp.getCause() != null) {
				if (exp.getCause() instanceof javax.validation.ConstraintViolationException) {
					throw new DcemException(DcemErrorCodes.CONSTRAIN_VIOLATION, exp.getCause().getMessage(), exp);
				}
				ConstraintViolationException cve = DcemUtils.getConstainViolation(exp.getCause());
				if (cve != null) {
					throw new DcemException(DcemErrorCodes.CONSTRAIN_VIOLATION_DB, "from: " + method + "-" + cve.getConstraintName(), exp);
				}
				if (exp.getCause() instanceof SQLException) {
					if (KaraUtils.getRootCause(exp, TimeoutException.class) != null) {
						throw new DcemException(DcemErrorCodes.DATABASE_CONNECTION_ERROR, exp.getCause().getMessage(), exp);
					}
				}
			}
			if (exp instanceof DcemException) {
				throw exp;
			}

			throw new DcemException(DcemErrorCodes.DB_TRANSACTION_ERROR, exp.getMessage(), exp);
		} finally {
			if (!methodStack.isEmpty()) {
				Method m = methodStack.pop();
				if (m != method) {
					logger.warn("Async event occurred - removed " + m + " instead of " + method);
				}
			}
		}
	}
}