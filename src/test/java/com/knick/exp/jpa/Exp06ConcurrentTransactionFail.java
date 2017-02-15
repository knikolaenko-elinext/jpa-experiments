package com.knick.exp.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.knick.exp.jpa.domain.Message;

public class Exp06ConcurrentTransactionFail {

	public static class Updater implements Runnable {
		private final EntityManagerFactory emf;
		private final Long testableEntityId;
		private final long sleepMillis;

		public Updater(EntityManagerFactory emf, Long testableEntityId, long sleepMillis) {
			super();
			this.emf = emf;
			this.testableEntityId = testableEntityId;
			this.sleepMillis = sleepMillis;
		}

		@Override
		public void run() {
			EntityManager em = emf.createEntityManager();
			for (int i = 0; i < 3; i++) {
				readAndUpdate(em);
			}
			em.close();
		}

		private void readAndUpdate(EntityManager em) {
			em.getTransaction().begin();
			Message msg = em.find(Message.class, testableEntityId);
			int counter = msg.getCounter();
			System.out.println(">> " + Thread.currentThread().getName() + ": " + counter);
			msg.setCounter(counter + 1);
			em.flush();
			em.clear();
			try {
				Thread.sleep(sleepMillis);
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
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");
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
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");

		Thread t1 = new Thread(new Updater(emf, testableEntityId, 1000));
		Thread t2 = new Thread(new Updater(emf, testableEntityId, 1000));

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
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");
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
