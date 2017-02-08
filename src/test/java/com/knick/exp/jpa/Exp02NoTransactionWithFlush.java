package com.knick.exp.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TransactionRequiredException;

import org.junit.Test;

import com.knick.exp.jpa.domain.Message;

public class Exp02NoTransactionWithFlush {
	@Test(expected = TransactionRequiredException.class)
	public void forceFlushOutsideTransaction() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("CRM_PU");
		EntityManager em = emf.createEntityManager();
		try {
			Message msg = new Message(null, "First");
			em.persist(msg);

			System.out.println(msg);

			em.flush(); // Exception - We can not save data without transaction
		} finally {
			em.close();
			emf.close();
		}
	}
}
