package hu.webarticum.holodb.admin;

import io.micronaut.configuration.picocli.PicocliRunner;

public class Application {

    public static void main(String[] args) {
        PicocliRunner.run(ApplicationCommand.class, args);
    }

}
