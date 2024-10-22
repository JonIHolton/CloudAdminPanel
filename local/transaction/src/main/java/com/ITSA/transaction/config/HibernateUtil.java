package com.ITSA.transaction.config;


import com.ITSA.transaction.enums.DataSourceType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class HibernateUtil {

    private static final EntityManagerFactory readOnlyEMF = 
        Persistence.createEntityManagerFactory("ReadOnlyPU");
    private static final EntityManagerFactory readWriteEMF = 
        Persistence.createEntityManagerFactory("ReadWritePU");

    public static EntityManager getEntityManager(DataSourceType dataSourceType) {
        return dataSourceType == DataSourceType.READ_ONLY ? readOnlyEMF.createEntityManager()
                                                           : readWriteEMF.createEntityManager();
    }

    /**
     * Health check to ensure EntityManagerFactory is up and entities are scanned.
     * 
     * @return boolean - true if the health check passes, false otherwise.
     */
    public static boolean performHealthCheck() {
        EntityManager em = null;
        try {
            em = getEntityManager(DataSourceType.READ_ONLY);
            // Perform a simple JPQL query to check connectivity and proper setup
            em.createQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception e) {
            log.error("Health check failed", e);
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    }

