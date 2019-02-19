package com.knick.exp.jpa;

import com.knick.exp.jpa.domain.batchsize.Diagnosis;
import com.knick.exp.jpa.domain.batchsize.Patient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Exp12BatchSizeAnnotation {

    private List<Long> patientIds;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_BATCHSIZE_EXP");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        Set<Patient> patients = new LinkedHashSet<>();
        for (int i = 0; i < 5; i++) {
            Patient patient = Patient.builder().name("patient #" + i).build();
            Set<Diagnosis> diagnoses = new LinkedHashSet<>();
            for (int j = 0; j < 3; j++) {
                diagnoses.add(Diagnosis.builder().name("diagnosis #" + j).patient(patient).build());
            }
            patient.setDiagnoses(diagnoses);
            em.persist(patient);
            patients.add(patient);
        }
        patientIds = patients.stream().map(p -> p.getId()).collect(Collectors.toList());

        em.getTransaction().commit();
        em.close();
        emf.close();
    }

    @Test
    public void seeHow() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_BATCHSIZE_EXP");
        EntityManager em = emf.createEntityManager();

        List<Patient> patients = patientIds.stream().map(id -> em.find(Patient.class, id)).collect(Collectors.toList());
        for (int i = 0; i < patients.size(); i++) {
            Patient patient = patients.get(i);
            System.out.println("exploring: " + patient.getName());

            if (i % Patient.DIAGNOSES_BATCH_SIZE == 0) {
                System.out.println("Look, here will be a SQL query vvv");
            }
            // Since @BatchSize(size = 4) for Patient:diagnoses field, instead of making 5 separate queries
            // for fetching diagnoses we will make only 2: first for first 4 patients and second for remaining 1

            // Meaning is that if we retrieve lazy field and @BatchSize is set - this field is querying for more than one entity in session,
            // no matter if we actually need this field for other entities
            System.out.println(patient.getDiagnoses().size());
        }
    }


    @After
    public void tearDown() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MYSQL_BATCHSIZE_EXP");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        patientIds.stream().forEach(id -> em.remove(em.getReference(Patient.class, id)));

        em.getTransaction().commit();
        em.close();
        emf.close();
    }
}
