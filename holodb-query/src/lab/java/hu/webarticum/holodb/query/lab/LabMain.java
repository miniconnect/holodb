package hu.webarticum.holodb.query.lab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import hu.webarticum.holodb.query.grammar.SimpleSelectLexer;
import hu.webarticum.holodb.query.grammar.SimpleSelectParser;
import hu.webarticum.holodb.query.grammar.SimpleSelectParser.SelectQueryContext;

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
        
        SimpleSelectLexer lexer = new SimpleSelectLexer(CharStreams.fromString(sql));
        SimpleSelectParser parser = new SimpleSelectParser(new CommonTokenStream(lexer));
        //parser.addParseListener(new SimpleSelectCompilerListener());
        SelectQueryContext context = parser.selectQuery();
        
       System.out.println(context.selectPart().selectableItem().size());
    }
    
}