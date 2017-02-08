package com.knick.exp.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

import com.knick.exp.jpa.domain.Message;

public class Exp04WithTransactionRollback {
	@Test
	public void persistAndThenRollbackTransaction() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("CRM_PU");
		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();

		em.persist(new Message(null, "First"));
		em.persist(new Message(null, "Second"));

		List<Message> rs = em.createQuery("from Message where text=:text", Message.class).setParameter("text", "First").getResultList();
		System.out.println(rs);

		em.getTransaction().rollback();

		em.close();
		emf.close();
		// No data in DB because transaction was rolled back
	}
}
