package hu.webarticum.holodb.admin.materialize;

import java.sql.Connection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import hu.webarticum.minibase.storage.api.StorageAccess;

public class Materializer {

    private final StorageAccess storageAccess;
    private final Connection connection;
    private final Optional<String> sourceSchemaName;
    private final Predicate<String> tableFilter;
    private final UnaryOperator<String> tableRenamer;

    private Materializer(MaterializerBuilder builder) {
        this.storageAccess = builder.storageAccess;
        this.connection = builder.connection;
        this.sourceSchemaName = builder.sourceSchemaName;
        this.tableFilter = builder.tableFilter;
        this.tableRenamer = builder.tableRenamer;
    }

    public static MaterializerBuilder builder(StorageAccess storageAccess, Connection connection) {
        return new MaterializerBuilder(storageAccess, connection);
    }

    public void materialize() {
        
        // TODO
        System.out.println(tableRenamer.apply("LoremIpsum"));
        
    }

    public static class MaterializerBuilder {

        private final StorageAccess storageAccess;
        private final Connection connection;

        private Optional<String> sourceSchemaName = Optional.empty();
        private Predicate<String> tableFilter = t -> true;
        private UnaryOperator<String> tableRenamer = t -> t;

        private MaterializerBuilder(StorageAccess storageAccess, Connection connection) {
            this.storageAccess = storageAccess;
            this.connection = connection;
        }

        public MaterializerBuilder sourceSchemaName(String sourceSchemaName) {
            this.sourceSchemaName = Optional.of(sourceSchemaName);
            return this;
        }

        public MaterializerBuilder tableFilter(Predicate<String> tableFilter) {
            this.tableFilter = tableFilter;
            return this;
        }

        public MaterializerBuilder tableRenamer(UnaryOperator<String> tableRenamer) {
            this.tableRenamer = tableRenamer;
            return this;
        }

        public Materializer build() {
            return new Materializer(this);
        }

    }

}
