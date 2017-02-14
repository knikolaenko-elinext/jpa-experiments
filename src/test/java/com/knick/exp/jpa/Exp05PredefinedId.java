package com.knick.exp.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.junit.Test;

import com.knick.exp.jpa.domain.Message;

public class Exp05PredefinedId {
	@Test(expected=PersistenceException.class)
	public void persistDetachedEntity() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("CRM_PU");
		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin(); 

		Message msg = new Message(10001L, "First", 0);
		em.persist(msg); // It is not allowed to persist detached entity (with id set) - throw an exception

		em.getTransaction().commit();

		em.close();
		emf.close();
	}
	
	@Test
	public void mergeDetachedEntity() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("CRM_PU");
		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();

		Message msg = new Message(10001L, "First", 0);
		msg = em.merge(msg); // if entity with this id does not exist - generate new row with new id	
		System.out.println(msg);
		
		msg = new Message(2L, "Second", 0);
		msg = em.merge(msg); // if entity with this id does exist - updates this row
		System.out.println(msg);

		em.getTransaction().commit();

		em.close();
		emf.close();
	}
}
