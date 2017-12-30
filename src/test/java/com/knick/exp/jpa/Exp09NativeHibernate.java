package com.knick.exp.jpa;

import com.knick.exp.jpa.domain.Message;
import org.hibernate.Session;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TransactionRequiredException;

public class Exp09NativeHibernate {

    @Test(expected = TransactionRequiredException.class)
    public void failToSaveWithoutTransaction() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");
        EntityManager em = emf.createEntityManager();
        Session session = em.unwrap(Session.class);
        Assert.assertEquals(TransactionStatus.NOT_ACTIVE, session.getTransaction().getStatus());
        Message msg = Message.builder().text("message").build();
        session.save(msg);
        session.flush();
        session.close();
    }

    @Test
    public void saveWithTransaction() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");
        EntityManager em = emf.createEntityManager();
        Session session = em.unwrap(Session.class);
        Assert.assertEquals(TransactionStatus.NOT_ACTIVE, session.getTransaction().getStatus());
        session.beginTransaction();
        Assert.assertEquals(TransactionStatus.ACTIVE, session.getTransaction().getStatus());
        Message msg = Message.builder().text("message").build();
        session.save(msg);
        session.getTransaction().commit();
        Assert.assertEquals(TransactionStatus.COMMITTED, session.getTransaction().getStatus());
    }
}
