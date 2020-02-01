package com.knick.exp.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TransactionRequiredException;

import org.junit.Test;

import com.knick.exp.jpa.domain.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Exp02NoTransactionWithFlush {
	private static final Logger LOG = LoggerFactory.getLogger(Exp02NoTransactionWithFlush.class);

	@Test(expected = TransactionRequiredException.class)
	public void forceFlushOutsideTransaction() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("H2_PU");
		EntityManager em = emf.createEntityManager();
		try {
			Message msg = new Message(null, "First", 0);
			em.persist(msg);

			System.out.println(msg);

			em.flush(); // Exception - We can not save data without transaction
		} catch (Exception e) {
			LOG.error("Exception occurred", e);
			throw e;
		} finally {
			em.close();
			emf.close();
		}
	}
}
