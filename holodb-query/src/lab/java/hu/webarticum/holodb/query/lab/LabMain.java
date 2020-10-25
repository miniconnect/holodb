package hu.webarticum.holodb.query.lab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

import hu.webarticum.holodb.query.common.Result;
import hu.webarticum.holodb.query.common.ResultRow;
import hu.webarticum.holodb.query.common.ResultSet;
import hu.webarticum.holodb.query.common.SqlExecutor;
import hu.webarticum.holodb.query.dummy.DummySqlExecutor;

public class LabMain {

    public static void main(String[] args) throws IOException {
        String filename = "simple-select.sql";
        
        String packageName = LabMain.class.getPackageName();
        String resourceParent = packageName.replace('.', '/');
        String resourcePath = resourceParent + "/" + filename;
        String sql;
        try (BufferedReader sqlReader = new BufferedReader(new InputStreamReader(
                LabMain.class.getClassLoader().getResourceAsStream(resourcePath)))) {
            sql = sqlReader.lines().collect(Collectors.joining("\n"));
        }
        
        SqlExecutor sqlExecutor = new DummySqlExecutor();
        Result result = sqlExecutor.execute(sql);
        
        if (result.hasResultSet()) {
            dumpResultSet(result.resultSet());
        } else {
            System.out.println("No result set");
        }
    }
    
    private static void dumpResultSet(ResultSet resultSet) {
        boolean first = true;
        for (ResultRow row : resultSet) {
            Map<String, Object> rowData = row.data();
            if (first) {
                StringBuilder lineBuilder = new StringBuilder();
                for (String key : rowData.keySet()) {
                    System.out.print(String.format("| %-20s ", key));
                    lineBuilder.append("+----------------------");
                }
                System.out.println("|");
                lineBuilder.append("+");
                System.out.println(lineBuilder);
                first = false;
            }
            
            for (Object value : rowData.values()) {
                System.out.print(String.format("| %-20s ", value));
            }
            System.out.println("|");
        }
        if (first) {
            System.out.println("No data found!");
        }
    }
    
    
}