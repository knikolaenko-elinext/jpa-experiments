package com.knick.exp.jpa;

import com.knick.exp.jpa.domain.eager.Color;
import com.knick.exp.jpa.domain.eager.Sku;
import com.knick.exp.jpa.domain.eager.Ware;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Exp11EagerOneToManyFetch {

    private Long wareId;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        Ware ware = Ware.builder().name("ware").skus(new LinkedHashSet<>()).build();
        for (int i = 0; i < 5; i++) {
            Sku sku = Sku.builder().name("sku #" + i).colors(new LinkedHashSet<>()).build();
            for (int j = 0; j < 7; j++) {
                Color color = Color.builder().name("color #" + j).build();
                sku.getColors().add(color);
                color.setSku(sku);
            }
            ware.getSkus().add(sku);
            sku.setWare(ware);
        }
        em.persist(ware);

        wareId = ware.getId();

        em.getTransaction().commit();
        em.close();
        emf.close();
    }

    @Test
    public void testFetchingOneToManyEager() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");
        EntityManager em = emf.createEntityManager();

        Ware ware = em.find(Ware.class, wareId);

        System.out.println("ware: " + ware);
    }

    @After
    public void tearDown() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_PU");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        em.remove(em.getReference(Ware.class, wareId));

        em.getTransaction().commit();
        em.close();
        emf.close();
    }
}
