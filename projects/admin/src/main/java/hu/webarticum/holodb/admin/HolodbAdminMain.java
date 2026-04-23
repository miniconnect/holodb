package hu.webarticum.holodb.admin;

import picocli.CommandLine;

public class HolodbAdminMain {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ApplicationCommand()).execute(args);
        System.exit(exitCode); 
    }

}
