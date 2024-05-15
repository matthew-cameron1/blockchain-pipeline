package ai.utiliti.bes.services;

import ai.utiliti.bes.model.BlockchainEvent;
import ai.utiliti.bes.model.EventLog;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.cfgxml.spi.LoadedConfig;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;

/*
    This is an unmanaged bean for use by the lambda function since we don't wanna use a whole spring boot app
 */
public class LambdaDBService {
    private final SessionFactory factory;

    public LambdaDBService() {
        this.factory = createSessionFactory();
    }

    SessionFactory createSessionFactory() {
        Map<String, String> settings = new HashMap<>();
        settings.put("connection.driver_class", "org.postgresql.Driver");
        settings.put("dialect", "org.hibernate.dialect.PostgreSQLDialect");
        settings.put("hibernate.connection.url",
                     System.getenv("DATABASE_URL"));
        settings.put("hibernate.current_session_context_class", "thread");
        settings.put("hibernate.show_sql", "true");
        settings.put("hibernate.format_sql", "true");

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                                    .applySettings(settings).build();

        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        metadataSources.addAnnotatedClass(EventLog.class);
        metadataSources.addAnnotatedClass(BlockchainEvent.class);
        Metadata metadata = metadataSources.buildMetadata();

        // here we build the SessionFactory (Hibernate 5.4)
        return metadata.getSessionFactoryBuilder().applyAutoClosing(true).build();
    }

    public void save(EventLog eventLog) {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(eventLog);
            transaction.commit();
        }
    }
}