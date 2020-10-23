package hu.webarticum.holodb.hibernate.lab;

import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import hu.webarticum.holodb.hibernate.HoloHibernateDialect;

public class SimpleHibernateTestMain {

    public static void main(String[] args) throws Exception {
        Class.forName("hu.webarticum.holodb.jdbc.embedded.HoloEmbeddedJdbcDriver");
        Properties configProperties = new Properties();
        configProperties.put("hibernate.connection.url", "jdbc:holodb:lorem-ipsum");
        configProperties.put("hibernate.connection.username", "lorem");
        configProperties.put("hibernate.connection.password", "ipsum");
        //configProperties.put("hibernate.connection.driver_class", "alma");
        configProperties.put("hibernate.dialect", HoloHibernateDialect.class);
        Configuration configuration = new Configuration();
        configuration.addProperties(configProperties);
        configuration.addAnnotatedClass(ExampleEntity.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        ExampleEntity exampleEntity = session.load(ExampleEntity.class, 165L);
        System.out.println(exampleEntity);
        session.close();
    }
    
}
