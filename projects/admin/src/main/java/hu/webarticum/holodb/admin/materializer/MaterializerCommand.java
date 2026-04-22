package hu.webarticum.holodb.admin.materializer;

import jakarta.inject.Singleton;
import picocli.CommandLine.Command;

@Singleton
@Command(
        name = "materialize",
        description = "Materializes a HoloDB virtual dataset",
        mixinStandardHelpOptions = true
)
public class MaterializerCommand implements Runnable {

    @Override
    public void run() {
        
        // TODO
        System.out.println("Materialize!");
        
    }

}
