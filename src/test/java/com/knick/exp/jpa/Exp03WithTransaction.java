package com.knick.exp.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

import com.knick.exp.jpa.domain.Message;

public class Exp03WithTransaction {
	@Test
	public void persistAndThenCommitTransaction() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("CRM_PU");
		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();

		Message msg = new Message(null, "First", 0);
		em.persist(msg);

		System.out.println(msg);

		em.getTransaction().commit();

		em.close();
		emf.close();
	}
}
