package hu.webarticum.holodb.admin;

import jakarta.inject.Singleton;
import picocli.CommandLine.Command;

@Singleton
@Command(
        name = "holodb-admin",
        description = "Administration tools for HoloDB",
        mixinStandardHelpOptions = true
)
public class ApplicationCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Hello world!");
    }

}
