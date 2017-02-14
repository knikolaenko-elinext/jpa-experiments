package com.knick.exp.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.knick.exp.jpa.domain.Message;

public class Exp06ConcurrentTransaction {

	public static class Updater implements Runnable {
		private final EntityManagerFactory emf;
		private final Long testableEntityId;

		public Updater(EntityManagerFactory emf, Long testableEntityId) {
			super();
			this.emf = emf;
			this.testableEntityId = testableEntityId;
		}

		@Override
		public void run() {
			EntityManager em = emf.createEntityManager();
			for (int i = 0; i < 10; i++) {
				readAndUpdate(em);
			}
			em.close();
		}

		private void readAndUpdate(EntityManager em) {
			em.getTransaction().begin();
			Message msg = em.find(Message.class, testableEntityId);
			int counter = msg.getCounter();
			System.out.println(">> "+Thread.currentThread().getName()+": "+counter);
			msg.setCounter(counter + 1);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			em.getTransaction().commit();
		}
	}

	private Long testableEntityId;

	@Before
	public void before() {
		System.out.println(">> before start");
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("CRM_PU");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		Message msg = new Message(null, "None", 0);
		em.persist(msg);

		testableEntityId = msg.getId();

		em.getTransaction().commit();
		em.close();
		emf.close();
		System.out.println(">> before finish");
	}

	@Test
	public void raceIt() throws InterruptedException {
		System.out.println(">> race start");
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("CRM_PU");

		Thread t1 = new Thread(new Updater(emf, testableEntityId));
		Thread t2 = new Thread(new Updater(emf, testableEntityId));

		t1.start();
		Thread.sleep(500);
		t2.start();

		t1.join();
		t2.join();

		emf.close();
		System.out.println(">> race finish");
	}

	@After
	public void after() {
		System.out.println(">> after start");
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("CRM_PU");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		Message msg = em.find(Message.class, testableEntityId);
		em.remove(msg);

		em.getTransaction().commit();
		em.close();
		emf.close();
		System.out.println(">> after finish");
	}
}
