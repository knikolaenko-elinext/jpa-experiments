package com.knick.exp.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

import com.knick.exp.jpa.domain.Message;
import com.knick.exp.jpa.domain.MessageDeliveryReport;

public class Exp08OneToOne {
	@Test
	public void persistAndThenCommitTransaction() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("H2_PU");
		EntityManager em = null;

		// CREATE

		em = emf.createEntityManager();

		em.getTransaction().begin();

		MessageDeliveryReport deliveryReport = MessageDeliveryReport.builder().status("delivered").build();
		Message msg = Message.builder().text("message").build();

		msg.setDeliveryReport(deliveryReport);
		deliveryReport.setMessage(msg);

		em.persist(msg);
		em.persist(deliveryReport);

		em.getTransaction().commit();

		em.close();

		System.out.println(msg);
		System.out.println(deliveryReport);

		// READ

		em = emf.createEntityManager();

		em.getTransaction().begin();

		@SuppressWarnings("unchecked")
		List<Message> messages = em.createQuery("SELECT m FROM Message m").getResultList();
		messages.forEach(message -> {
			System.out.println(message.getId());
		});

		em.getTransaction().commit();

		em.close();

		emf.close();
	}
}
