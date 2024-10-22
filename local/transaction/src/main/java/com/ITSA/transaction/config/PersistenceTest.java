package com.ITSA.transaction.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class PersistenceTest {
    public static void main(String[] args) {
        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("ReadOnlyPU");
            if (emf != null) {
                System.out.println("Persistence unit is loaded successfully.");
                // Optionally, perform further checks here
                emf.close();
            } else {
                System.out.println("Failed to load persistence unit.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
