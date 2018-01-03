package com.knick.exp.jpa;

import com.knick.exp.jpa.domain.Message;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TransactionRequiredException;
import java.util.Iterator;

public class Exp09NativeHibernate {

    @Test(expected = TransactionRequiredException.class)
    public void failToSaveWithoutTransaction() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");
        SessionFactory sf = emf.unwrap(SessionFactory.class);
        Session session = sf.openSession();
        Assert.assertEquals(TransactionStatus.NOT_ACTIVE, session.getTransaction().getStatus());
        Message msg = Message.builder().text("message").build();
        session.save(msg);
        session.flush();
        session.close();
    }

    @Test
    public void saveWithTransaction() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");
        SessionFactory sf = emf.unwrap(SessionFactory.class);
        Session session = sf.withOptions().interceptor(new TestInterceptor()).openSession();
        Assert.assertEquals(TransactionStatus.NOT_ACTIVE, session.getTransaction().getStatus());
        session.beginTransaction();
        Assert.assertEquals(TransactionStatus.ACTIVE, session.getTransaction().getStatus());
        Message msg = Message.builder().text("message").build();
        session.save(msg);
        session.getTransaction().commit();
        Assert.assertEquals(TransactionStatus.COMMITTED, session.getTransaction().getStatus());
    }

    @Test
    public void playWithMultipleTransactions() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");
        SessionFactory sf = emf.unwrap(SessionFactory.class);
        Session session = sf.openSession();
        Assert.assertEquals(TransactionStatus.NOT_ACTIVE, session.getTransaction().getStatus());
        session.beginTransaction();
        Assert.assertEquals(TransactionStatus.ACTIVE, session.getTransaction().getStatus());
        Message msg = Message.builder().text("message").build();
        session.save(msg);
        session.getTransaction().rollback();
        Assert.assertEquals(TransactionStatus.ROLLED_BACK, session.getTransaction().getStatus());

        session.beginTransaction();
        Assert.assertEquals(TransactionStatus.ACTIVE, session.getTransaction().getStatus());
        msg = Message.builder().text("message1").build();
        session.save(msg);
        session.getTransaction().commit();
        Assert.assertEquals(TransactionStatus.COMMITTED, session.getTransaction().getStatus());
    }

    private static class TestInterceptor extends EmptyInterceptor {
        @Override
        public void postFlush(Iterator entities) {
            System.out.println("PostFlush!!!");
        }

        @Override
        public void preFlush(Iterator entities) {
            System.out.println("PreFlush!!!");
        }
    }
}
