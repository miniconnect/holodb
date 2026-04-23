package hu.webarticum.holodb.admin;

import hu.webarticum.holodb.admin.buildconfig.BuildConfig;
import hu.webarticum.holodb.admin.materialize.MaterializeCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "holodb-admin",
        description = BuildConfig.APP_DESCRIPTION,
        version = BuildConfig.APP_VERSION,
        mixinStandardHelpOptions = true,
        subcommandsRepeatable = false,
        subcommands = { MaterializeCommand.class }
)
public class HolodbAdminMain implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new HolodbAdminMain()).execute(args);
        System.exit(exitCode); 
    }

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }

}
