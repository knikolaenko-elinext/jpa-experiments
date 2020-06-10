package com.knick.exp.jpa;

import com.knick.exp.jpa.domain.Message;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Exp13HibernateNativeUpgradeLocking {
    private static final Logger LOG = LoggerFactory.getLogger(Exp13HibernateNativeUpgradeLocking.class);

    private static final int SLEEP_MILLIS = 2000;
    private static final int THREADS_NUM = 10;

    private SessionFactory sf;
    private Long messageId;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");
        sf = emf.unwrap(SessionFactory.class);

        // create test message
        Session session = sf.openSession();
        session.beginTransaction();
        Message msg = Message.builder().text(Exp13HibernateNativeUpgradeLocking.class.getName()).build();
        session.save(msg);
        session.getTransaction().commit();
        messageId = msg.getId();
        LOG.info("Message ID: {}", messageId);
    }

    @Test
    public void testMysqlUpgradeLockKeptUntilSessionClosed() throws InterruptedException {
        Runnable raceRunnable = () -> {
            Session session = sf.openSession();

            Message message = session.get(Message.class, messageId);
            LOG.info("Fetched {}", message);

            session.buildLockRequest(LockOptions.UPGRADE).lock(message);
            LOG.info("Locked {}", message);

            // By default lock will be kept until session is closed
            sleep();

            session.close();
            LOG.info("Closed session");
        };

        List<Thread> threads = IntStream.range(0, THREADS_NUM).mapToObj(i -> new Thread(raceRunnable, "Thread #" + i)).collect(Collectors.toList());
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    @Test
    public void testMysqlUpgradeLockReleasesOnSave() throws InterruptedException {
        Runnable raceRunnable = () -> {
            Session session = sf.openSession();

            Message message = session.get(Message.class, messageId);
            LOG.info("Fetched {}", message);

            session.buildLockRequest(LockOptions.UPGRADE).lock(message);
            LOG.info("Locked {}", message);

            // After transaction commit "UPGRADE" lock is released immediately
            session.beginTransaction();
            message.setCounter(message.getCounter() + 1);
            session.save(message);
            LOG.info("Saved {}", message);
            sleep();
            session.getTransaction().commit();
            LOG.info("Committed {}", message);

            sleep();

            session.close();
            LOG.info("Closed session");
        };

        List<Thread> threads = IntStream.range(0, THREADS_NUM).mapToObj(i -> new Thread(raceRunnable, "Thread #" + i)).collect(Collectors.toList());
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(SLEEP_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
