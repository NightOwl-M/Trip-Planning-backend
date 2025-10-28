package app.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public final class JPAConfig {
    private static EntityManagerFactory emf;
    private static EntityManagerFactory emfTest;

    private JPAConfig() {}

    /** Dev/Prod EMF */
    public static synchronized EntityManagerFactory emf() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("app-unit", runtimeOverrides(false));
        }
        return emf;
    }

    /** Test EMF (Testcontainers JDBC) */
    public static synchronized EntityManagerFactory emfTest() {
        if (emfTest == null) {
            emfTest = Persistence.createEntityManagerFactory("app-unit", runtimeOverrides(true));
        }
        return emfTest;
    }

    private static Map<String, Object> runtimeOverrides(boolean test) {
        Map<String, Object> m = new HashMap<>();

        if (test) {
            // Testcontainers JDBC driver
            m.put("jakarta.persistence.jdbc.driver", "org.testcontainers.jdbc.ContainerDatabaseDriver");
            m.put("jakarta.persistence.jdbc.url", "jdbc:tc:postgresql:15-alpine:///test_db");
            m.put("jakarta.persistence.jdbc.user", "postgres");
            m.put("jakarta.persistence.jdbc.password", "postgres");

            // Frisk schema til hver testkørsel
            m.put("hibernate.hbm2ddl.auto", "create-drop");

        } else if (System.getenv("DEPLOYED") != null) {
            // Prod / deployed
            String conn = getenvOr("CONNECTION_STR", "jdbc:postgresql://localhost:5432/");
            String name = getenvOr("DB_NAME", "app");
            String url  = conn + name;

            m.put("jakarta.persistence.jdbc.url", url);
            m.put("jakarta.persistence.jdbc.user", getenvOr("DB_USERNAME", "postgres"));
            m.put("jakarta.persistence.jdbc.password", getenvOr("DB_PASSWORD", "postgres"));

            // I prod: aldrig auto-ændre schema
            m.put("hibernate.hbm2ddl.auto", "validate");

        } else {
            // Lokal udvikling
            String url  = getenvOr("DB_URL", "jdbc:postgresql://localhost:5433/fitness");
            String user = getenvOr("DB_USER", "app");
            String pass = getenvOr("DB_PASSWORD", "secret");

            m.put("jakarta.persistence.jdbc.url", url);
            m.put("jakarta.persistence.jdbc.user", user);
            m.put("jakarta.persistence.jdbc.password", pass);

            // I dev: lad Hibernate vedligeholde kolonner/tabeller
            m.put("hibernate.hbm2ddl.auto", "update");
        }

        // Fælles
        m.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        m.put("hibernate.show_sql", getenvOr("HIBERNATE_SHOW_SQL", "false"));
        m.put("hibernate.format_sql", getenvOr("HIBERNATE_FORMAT_SQL", "false"));
        m.put("hibernate.jdbc.time_zone", getenvOr("DB_TIMEZONE", "UTC"));

        // Hikari pool (via Hibernate integration)
        m.put("hibernate.hikari.maximumPoolSize", getenvOr("DB_POOL_MAX", "10"));
        m.put("hibernate.hikari.minimumIdle",     getenvOr("DB_POOL_MIN_IDLE", "2"));
        m.put("hibernate.hikari.poolName",        getenvOr("DB_POOL_NAME", "ApiPool"));

        // Classpath scanning af @Entity
        m.put("hibernate.archive.autodetection", getenvOr("HIBERNATE_AUTODETECT", "class"));

        return m;
    }

    private static String getenvOr(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (emf != null && emf.isOpen()) emf.close();
            if (emfTest != null && emfTest.isOpen()) emfTest.close();
        }));
    }
}
