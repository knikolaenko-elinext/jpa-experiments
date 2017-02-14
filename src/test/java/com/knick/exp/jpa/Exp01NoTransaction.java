package com.knick.exp.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Assert;
import org.junit.Test;

import com.knick.exp.jpa.domain.Message;

public class Exp01NoTransaction {

	@Test
	public void persistWithoutTransaction() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("CRM_PU");
		EntityManager em = emf.createEntityManager();

		Message msg = new Message(null, "First", 0);
		em.persist(msg);

		System.out.println(msg);
		Assert.assertNotNull(msg.getId());

		em.close();
		emf.close();

		// Data won't be actually inserted. Persist doesn't mean 'INSERT' - we
		// need either flush() or 'SELECT' after
	}
}
