package com.knick.exp.jpa;

import com.knick.exp.jpa.domain.Message;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Exp10NativeConcurrentSessions {
    @Test
    public void playWithSaveObjectInDifferentTransactions() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");
        SessionFactory sf = emf.unwrap(SessionFactory.class);

        ReentrantLock lock = new ReentrantLock();
        AtomicLong entityId = new AtomicLong();

        Thread t1 = new Thread(() -> {

            Session session = sf.openSession();

            // 1. Create entity and persist it
            lock(lock);
            session.getTransaction().begin();

            // new -> persistent
            Message entity = Message.builder().text(Exp10NativeConcurrentSessions.class.getName() + new Date()).build();
            session.save(entity);
            entityId.set(entity.getId());

            session.getTransaction().commit();

            unlock(lock);

            // 3. Update it
            lock(lock);

            entity.setCounter(3); // Notice, looks like we can change entity outside transaction, but commit it after
            session.getTransaction().begin();
            session.getTransaction().commit();
            System.out.println("T1: " + entity);

            unlock(lock);

            // 6. Update it
            lock(lock);

            entity.setCounter(6);
            session.getTransaction().begin();
            session.getTransaction().commit();
            System.out.println("T1: " + entity);

            // 7. Update, rollback then commit
            entity.setCounter(7);
            session.getTransaction().begin();
            session.getTransaction().rollback();
            Assert.assertFalse(session.contains(entity)); // Notice, entity become detached
            Assert.assertEquals(7, entity.getCounter()); // ..  but entity still says '7'

            session.getTransaction().begin();
            session.getTransaction().commit(); // Notice, that has no any effect, because entity is detached

            System.out.println("T1: " + entity);

            unlock(lock);

            // 9. Attach
            lock(lock);

            entity.setCounter(9);
            session.saveOrUpdate(entity);
            // session.flush(); -- wont work. transaction is needed
            session.getTransaction().begin();
            session.getTransaction().commit();

            unlock(lock);

        });
        Thread t2 = new Thread(() -> {
            Session session = sf.openSession();

            // 2. Get entity
            lock(lock);

            Message entity = session.get(Message.class, entityId.get());
            System.out.println("T2: " + entity);

            unlock(lock);

            // 4. Read it
            lock(lock);
            session.refresh(entity); // Notice, need refresh for getting updated state
            Assert.assertEquals(3, entity.getCounter());
            System.out.println("T2: " + entity);

            // 5. Update it
            entity.setCounter(5); // Notice, looks like we can change entity outside transaction, but commit it after
            session.getTransaction().begin();
            session.getTransaction().commit();
            Assert.assertEquals(5, entity.getCounter());
            System.out.println("T2: " + entity);

            unlock(lock);

            // 8. Read it
            lock(lock);

            session.refresh(entity); // Notice, need refresh for getting updated state
            Assert.assertEquals(6, entity.getCounter());
            System.out.println("T2: " + entity);

            unlock(lock);

            // 10. Read it
            lock(lock);

            session.refresh(entity); // Notice, need refresh for getting updated state
            Assert.assertEquals(9, entity.getCounter());
            System.out.println("T2: " + entity);

            unlock(lock);
        });

        t1.start();
        Thread.sleep(100);
        t2.start();

        t1.join();
        t2.join();

    }

    private void lock(ReentrantLock lock) {
        lock.lock();
    }

    private void unlock(ReentrantLock lock) {
        lock.unlock();
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread.yield();
    }
}
