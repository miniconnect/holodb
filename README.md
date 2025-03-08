# HoloDB - the on-the-fly relational database

No data generation.
No storage costs.
No migrations.
Start from zero and immediately work with
realistic, arbitrarily large, fully queryable datasets.
Simply describe your data as a declarative configuration,
and it's there in a flash.

HoloDB is a full-featured relational database engine
with a completely virtual starting dataset.
Field values and query results are calculated on the fly,
based on the given configuration in a highly efficient way.
Further modifications are stored in diff layers,
making the database effectively writable.


**What is all this for?**

- **prototyping**: include the database in your stack, even in live mode
- **demonstration**: showcase your app with realistic data, without materializing it
- **integration testing**: add a full-featured relational database to your pipeline
- **mocking**: put a functional database into the stack, even based on ORM entities
- **feeding**: use it as a data source to populate a traditional database
- **teaching**: provide a dummy database for your students

**How to try it out quickly?**

You can use HoloDB in many ways,
e.g. in embedded mode or as a server, from a program,
from an ORM system, or interactively with a REPL.
But the easiest way to try it out is using Docker.

You can download a ready-made configuration file from the example projects:

```bash
curl -o /tmp/config.yaml https://raw.githubusercontent.com/miniconnect/general-docs/refs/heads/main/examples/holodb-standalone/config.yaml
```

Then, just load the configuration into a HoloDB container:

```bash
docker run --rm -p 3430:3430 -v /tmp/config.yaml:/app/config.yaml miniconnect/holodb
```

Then use the `micl` command from [miniconnect-client](https://github.com/miniconnect/miniconnect-client) to run queries in a REPL:

```
$ micl

Welcome in miniConnect SQL REPL! - localhost:3430

SQL > SHOW SCHEMAS

  Query was successfully executed!

  ┌─────────┐
  │ Schemas │
  ├─────────┤
  │ economy │
  └─────────┘

SQL > USE economy

  Query was successfully executed!

SQL > SHOW TABLES;

  Query was successfully executed!

  ┌───────────────────┐
  │ Tables_in_economy │
  ├───────────────────┤
  │ companies         │
  │ employees         │
  │ sales             │
  └───────────────────┘

SQL > SELECT * FROM companies;

  Query was successfully executed!

  ┌────┬──────────────────────┬──────────────┬─────────────────┐
  │ id │ name                 │ headquarters │ contact_phone   │
  ├────┼──────────────────────┼──────────────┼─────────────────┤
  │  1 │ Fav Fruits Inc.      │ Stockholm    │ [NULL]          │
  │  2 │ Fru-fru Sales Inc.   │ Tel Aviv     │ +1 143-339-0981 │
  │  3 │ Fructose Palace Inc. │ Baku         │ +1 295-272-4854 │
  │  4 │ Vega Veterans Inc.   │ New York     │ +1 413-876-4936 │
  │  5 │ Goods of Nature Inc. │ Paris        │ [NULL]          │
  └────┴──────────────────────┴──────────────┴─────────────────┘

SQL > exit

Bye-bye!
```

Visit the [SQL guide](https://github.com/miniconnect/minibase/blob/master/SQL.md)
to learn more about the SQL features supported by the default query engine.
Alternatively, you can try the experimental integration with the
[Apache Calcite](https://github.com/miniconnect/calcite-integration) query planner.

You can connect to HoloDB directly via the MiniConnect API.
For more information,
see [MiniConnect API](https://github.com/miniconnect/miniconnect?tab=readme-ov-file#getting-started-with-the-api).

Also, you can use a MiniConnect server or even an existing MiniConnect `Session` via JDBC.
For more information,
see [MiniConnect JDBC compatibility](https://github.com/miniconnect/miniconnect#jdbc-compatibility).

## Configuration

In `config.yaml` you can specify the structure of your data (schemas, tables, columns, data, etc.):

```yaml
seed: 98765
schemas:
  - name: my_schema
    tables:
      - name: my_table
        writeable: true
        size: 150
        columns:
          - name: id
            mode: COUNTER
          - name: name
            values: ['Some name', 'Other name', 'Some other']
```

You can generate a JSON schema for this configuration data structure
by executing the `config:generateSchema` gradle task inside the holodb gradle project.
Then the generated schema file will be found here:

```
projects/config/build/schemas/holodb-config.schema.json
```

On the **top level** these keys are supported:

| Key | Type | Description |
| --- | ---- | ----------- |
| `seed` | `LargeInteger` | global random seed (global default: `0`) |
| `schemas` | `List` | list of schemas (see below) |

The `seed` option sets a random seed with which you can vary the content of the database.

For each **schema**, these subkeys are supported:

| Key | Type | Description |
| --- | ---- | ----------- |
| `name` | `String` | name of the database schema |
| `tables` | `List` | list of tables in this schema, see below (global default: none) |

For each **table**, these subkeys are supported:

| Key | Type | Description |
| --- | ---- | ----------- |
| `name` | `String` | name of the database table |
| `writeable` | `boolean` | writeable or not (global default: `false`) |
| `size` | `LargeInteger` | number of records in this table (global default: `50`) |
| `columns` | `List` | list of columns in this table, see below (global default: none) |

If `writeable` option is set to true, then an additional layer
will be added over the read-only table,
which accepts and stores insertions, updates, and deletions,
and it gives the effect that the table is writeable.

For each **column**, these subkeys are supported:

| Key | Type | Description |
| --- | ---- | ----------- |
| `name` | `String` | name of the table column |
| `type` | `String` (`Class<?>`) | java class name of column type |
| `mode` | `String` | filling mode: `DEFAULT`, `COUNTER`, `FIXED`, or `ENUM` (global default: `DEFAULT`) |
| `nullCount` | `LargeInteger` | count of null values (global default: `0`) |
| `values` | `Object[]` | explicit list of possible values |
| `valuesResource` | `String` | name of a java resource which contains the values line by line |
| `valuesBundle` | `String` | short name of a bundled value resource, otherwise similar to `valuesResource` (see below) |
| `valuesRange` | `LargeInteger[]` | start and end value of a numeric value range |
| `valuesPattern` | `String` | regex pattern for values (reverse indexed) |
| `valuesDynamicPattern` | `String` | regex pattern processed by [Generex](https://github.com/mifmif/Generex) (not reverse indexed) |
| `valuesForeignColumn` | `String[]` | use value set of a foreign `COUNTER` column |
| `distributionQuality` | `String` | distribution quality: `LOW`, `MEDIUM`, or `HIGH` (global default: `MEDIUM`) |
| `shuffleQuality` | `String` | shuffle quality: `NOOP`, `VERY_LOW`, `LOW`, `MEDIUM`, `HIGH`, or `VERY_HIGH` (global default: `MEDIUM`) |
| `sourceFactory` | `String` | java class name of source factory (must implement `hu.webarticum.holodb.spi.config.SourceFactory`) |
| `sourceFactoryData` | *any* | data will be passed to the source factory |
| `defaultValue` | *any* | default insert value for the column |

In most cases, `type` can be omitted.
If the configuration loader cannot guess the type, the startup aborts with an error.
However, the type can always be overridden (e. g. numbers can be generated using a regular expression).

The meaning of `mode` values:

| Mode | Description |
| ---- | ----------- |
| `DEFAULT` | randomly distributed, non-unique values, indexed (except in case of `valuesDynamicPattern` used) |
| `COUNTER` | fill with increasing whole numbers starting from `1`, unique, indexed (good choice for ID columns) |
| `FIXED` | values will not be shuffled, the count of values must be equal to the table size, non-indexed |
| `ENUM` | similar to `DEFAULT`, but with different proper rules for equality check, sort order and insertion/update |

In the case of writable tables, if other than the `ENUM` mode is used,
users can also put values ​​different from the initial ones.

If `nullCount` is specified (even if `0`), then the column will be nullable.
Omit `nullCount` to make the column `NOT NULL`.
In case of custom `sourceFactory`, the column will be `NOT NULL` only iff
the source is an `IndexedSource` and has at least one null value.

For specifying the possible values in the column, one of
`values`, `valuesResource`, `valuesRange`, `valuesPattern`,  `valuesDynamicPattern` and `valuesForeignColumn`
can be used.
Currently, for a `FIXED` column, only `values` is supported.

In the case of `COUNTER` mode, values will be ignored and should be omitted.
The type of a `COUNTER` column is always `java.math.LargeInteger`.

If used, the value of `valuesForeignColumn` must be an array of lengths 1, 2, or 3.
The one-element version contains a column name in the same table.
The two-element version contains a \[*\<table\>*, *\<column\>*\] pair in the same schema.
The three-element version contains the \[*\<schema\>*, *\<table\>*, *\<column\>*\] triplet.

There are several possible values for `valuesBundle`:

| Bundle name | Description |
| ----------- | ----------- |
| `cities` | 100 major world cities |
| `colors` | 147 color names (from CSS3) |
| `countries` | 197 country names |
| `female-forenames` | 100 frequent English female forenames |
| `forenames` | 100 frequent English forenames (50 female, 50 male) |
| `fruits` | 26 of the best selling fruits |
| `log-levels` | 6 standard log levels (from log4j) |
| `lorem` | 49 lower-case words of the *Lorem ipsum* text |
| `male-forenames` | 100 frequent English male forenames |
| `months` | the 12 month names |
| `surnames` | 100 frequent English surnames |
| `weekdays` | the names of the 7 days of the week |

You can set default values for schemas, tables, and columns at any higher level in the configuration tree.
Any value set at a lower lever will override any value set at a higher level (and, of course, the global default).

| Key | Available in |
| --- | ------------ |
| `schemaDefaults` | root |
| `tableDefaults` | root, `schemas.*` |
| `columnDefaults` | root, `schemas.*`, `schemas.*.tables.*` |

For example:

```yaml
tableDefaults:
  writeable: false
  size: 120
columnDefaults:
  shuffleQuality: NOOP
schemas:
  - name: schema_1
    tables:
      # ...
schemas:
  - name: schema_2
    tableDefaults:
      writeable: true
    tables:
      # ...
```

Using this config all table with no explicit `size` will have the size 120,
all table with no explicit `writeable` will read-only in `schema_1`, and writeable in `schema_2`.
Also, data shuffling is disabled by default.


## Load values from resource

You can use custom predefined value sets too.
To do this, create a file with one value on each line.
Make this file available to the java classloader.
If you use docker, the easiest way to do this is to copy the file into the `/app/resources` directory:

```dockerfile
FROM miniconnect/holodb:latest

COPY config.yaml /app/config.yaml
COPY my-car-brands.txt /app/resources/my-car-brands.txt
```

You can use a predefined value set resource with the `valuesResource` key in `config.yaml`:

```yaml
          # ...
          - name: car_brand
            valuesResource: 'my-car-brands.txt'
```

If you don't already have a value list, you can retrieve existing data from several sources,
for example [WikiData](https://www.wikidata.org/),
[JSONPlaceholder](https://jsonplaceholder.typicode.com/)
or [Kaggle](https://www.kaggle.com/).

Here is an example, where we get data from WikiData, process it with `jq`, then save it to the docker image.
To safely achieve this, we use a builder image:

```dockerfile
FROM dwdraju/alpine-curl-jq:latest AS builder
RUN curl --get \
  --data-urlencode 'query=SELECT ?lemma WHERE \
    { ?lexemeId dct:language wd:Q1860; wikibase:lemma ?lemma. ?lexemeId wikibase:lexicalCategory wd:Q9788 } \
    ORDER BY ?lemma' \
  'https://query.wikidata.org/bigdata/namespace/wdq/sparql' \
  -H 'Accept: application/json' \
  | jq -r '.results.bindings[].lemma.value' \
  > en-letters.txt

FROM miniconnect/holodb:latest
COPY config.yaml /app/config.yaml
COPY --from=builder /en-letters.txt /app/resources/en-letters.txt
```


## Generate from an existing database

You can find an experimental python script in the `tools` directory
that creates a HoloDB configuration from an existing MySQL database.

Here is an example of how you can use it:

```bash
python3 mysql_scanner.py -u your_user -p your_password -d your_database -w
```

Use the `-h` or `--help` option for more details.


## Embedded mode via JDBC

You can use HoloDB as an embedded database.

To achieve this, first add the required dependency:

```gradle
implementation "hu.webarticum.holodb:embedded:${holodbVersion}"
```

Set the JDBC connection URL, specifying a resource:

```
jdbc:holodb:embedded:resource://config.yaml
```

Or any file on the file system:

```
jdbc:holodb:embedded:file:///path/to/config.yaml
```

Or with selecting a specific schema:

```
jdbc:holodb:embedded:resource://config.yaml?schema=university
```

(Note: Number of slashes does matter.)

Use the `hu.webarticum.holodb.embedded.HoloEmbeddedDriver` driver class if its explicit setting is mandatory.


## Client-server mode via JDBC

To achieve this, first add the required dependency:

```gradle
implementation "hu.webarticum.miniconnect:jdbc:${miniConnectVersion}"
```

Set the JDBC connection URL, specifying a resource:

```
jdbc:miniconnect://localhost:3430
```

Or with selecting a specific schema:

```
jdbc:miniconnect://localhost:3430/university
```

In this case, use the `hu.webarticum.miniconnect.jdbc.MiniJdbcDriver` driver class if necessary.


## Mock JPA entities

To use the annotations below, set the `jpa-annotations` subproject as a dependency:

```gradle
implementation "hu.webarticum.holodb:jpa-annotations:${holodbVersion}"
```

If you want to use the service providers (e. g. `SourceFactory`), include the `spi` subproject too:

```gradle
implementation "hu.webarticum.holodb:spi:${holodbVersion}"
```

Actually running it requires the `jpa` subproject instead of the `jpa-annotations`:

```gradle
implementation "hu.webarticum.holodb:jpa:${holodbVersion}"
```

The `jpa` subproject has several dependencies (while `jpa-annotations` is near pure).
If you only use it for tests, define it as a test-only dependency.

Set this JDBC connection URL to use HoloDB as the database backend:

```
jdbc:holodb:jpa://
```

(Optionally, the schema can also be specified, e.g. `jdbc:holodb:jpa:///my_schema_name`.)

At the moment, schema construction is not fully automatic, it's necessary to explicitly pass the metamodel.
For example in Micronaut:

```java
@Singleton
public class HoloInit {
    
    private final EntityManager entityManager;
    
    public HoloInit(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @EventListener
    @Transactional
    public void onStartup(StartupEvent startupEvent) {
        JpaMetamodelDriver.setMetamodel(entityManager.getMetamodel());
    }
    
}
```

The solution should be similarly simple for Spring or other frameworks.

Now, all of your entities will be backed by HoloDB tables with automatic configuration.
To fine-tune this configuration, you can use some annotation on the entity classes.

| Annotation | Target | Description |
| ---------- | ------ | ----------- |
| `@HoloTable` | class | Overrides table parameters (schema, name, writeable, size) |
| `@HoloColumn` | field, method | Overrides column parameters |
| `@HoloIgnore` | class, field, method | Ignores an entity or attribute |
| `@HoloVirtualColumn` | class | Defines an additional column for the entity (multiple occurrences allowed) |

`@HoloColumn` and `@HoloVirtualColumn` accepts all the columns configurations
(for `@HoloVirtualColumn` `name` and `type` are mandatory).

Some numeric settings have two variants, one for usual and one for large values:

| Annotation | Usual field | Large field |
| ---------- | ----------- | ----------- |
| `@HoloTable` | `size` (`long`) | `largeSize` (`String`) |
| `@HoloColumn` | `nullCount` (`long`) | `largeNullCount` (`String`) |
| `@HoloColumn` | `valuesRange` (`long[]`) | `largeValuesRange` (`String[]`) |
| `@HoloVirtualColumn` | `nullCount` (`long`) | `largeNullCount` (`String`) |
| `@HoloVirtualColumn` | `valuesRange` (`long[]`) | `largeValuesRange` (`String[]`) |

Some settings accepts custom data:

| Annotation | Annotation field | Type | Config field |
| ---------- | ---------------- | ---- | ------------ |
| `@HoloColumn` | `sourceFactoryData` | `@HoloValue` | `sourceFactoryData` |
| `@HoloColumn` | `sourceFactoryDataMap` | `@HoloValue[]` | `sourceFactoryData` |
| `@HoloColumn` | `defaultValue` | `@HoloValue` | `defaultValue` |
| `@HoloVirtualColumn` | `sourceFactoryData` | `@HoloValue` | `sourceFactoryData` |
| `@HoloVirtualColumn` | `sourceFactoryDataMap` | `@HoloValue[]` | `sourceFactoryData` |
| `@HoloVirtualColumn` | `defaultValue` | `@HoloValue` | `defaultValue` |

Fields ending with the 'Map' suffix accepts an array of `@HoloValue`s,
you can use `@HoloValue.key` to set map entry key for each.

Example:

```java
@Entity
@Table(name = "companies")
@HoloTable(size = 25)
@HoloVirtualColumn(name = "extracol", type = Integer.class, valuesRange = {10, 20})
public class Company {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "birth_country", nullable = false)
    @HoloColumn(valuesBundle = "countries")
    private String country;
    
    // ...
    
}
```


## How does it work?

HoloDB introduces the concept of *holographic databases*.
A holographic database stores no real data and calculates field values and reverse indexes on-the-fly.
Nonetheless, you as a user experience a consistent, searchable (and optionally writable) database.
Such a database consumes little memory (even for large "data") and needs near-zero startup time.
Additionally, by changing the root seed the entire dataset can be shuffled.

So, HoloDB provides an arbitrarily large relational database filled with constrained random data.
Parameters and constraints can be specified in a configuration file.
Initialization ("filling" with data) of the tables is a no-op.
Query results are calculated on-the-fly.
Value providers are encouraged to calculate any single field of a column
practically in `O(1)`, but at most in `O(log(tableSize))` time.

As initialization is a no-op, it's particularly suitable for demonstrations, testing
and, in the case of a read-only database,
flexible orchestration, replication like some static content.


## Changelog

See [CHANGELOG.md](CHANGELOG.md).
