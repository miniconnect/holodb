package hu.webarticum.holodb.admin;

import hu.webarticum.holodb.admin.materializer.MaterializerCommand;
import hu.webarticum.holodb.admin.buildconfig.BuildConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "holodb-admin",
        description = BuildConfig.APP_DESCRIPTION,
        version = BuildConfig.APP_VERSION,
        mixinStandardHelpOptions = true,
        subcommandsRepeatable = false,
        subcommands = { MaterializerCommand.class }
)
public class ApplicationCommand implements Runnable {

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }

}
