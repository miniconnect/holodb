package hu.webarticum.holodb.jdbc.embedded.lab;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SimpleEmbeddedTestMain {

    public static void main(String[] args) throws Exception {
        Class.forName("hu.webarticum.holodb.jdbc.embedded.HoloEmbeddedJdbcDriver");  
        try (
                Connection connection = DriverManager.getConnection("jdbc:holodb:lorem-ipsum", "lorem", "ipsum");
                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
                ResultSet resultSet = statement.executeQuery("SELECT LOREM_IPSUM")) {
            
            resultSet.absolute(2);
            System.out.println(String.format("%s; %s; %s", // NOSONAR
                    resultSet.getString(3), resultSet.getString(5), resultSet.getString(14)));

            resultSet.absolute(7);
            System.out.println(String.format("%s; %s; %s", // NOSONAR
                    resultSet.getString(3), resultSet.getString(5), resultSet.getString(16)));
            
        }
    }
    
}
