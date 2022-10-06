# HoloDB

**Relational database - seemingly filled with random data.**

However, by default, this data does not actually take up any space in memory or on a volume
(to use an analogy, it is as if the data set is projected as a hologram from a simple configuration).

The base layer is an arbitrarily large, read-only data set that is readable and searchable, and yet fully consistent.
Any pieces of data and index lookups are calculated on-the-fly.

An optional second layer is built on top of this, allowing read-write access
(stores differences while maintains consistency and searchability).

So, you can start an arbitrarily large database in moments, with minimal effort;
all you need is a configuration file.

## Use with Docker

HoloDB is available on [DockerHub](https://hub.docker.com/r/miniconnect/holodb).
You just need a configuration file (YAML, by default), and a Dockerfile like this:

```dockerfile
FROM miniconnect/holodb:latest

COPY config.yaml /app/config.yaml
```

For some self-contained examples
[see the examples directory](https://github.com/miniconnect/holodb/tree/master/examples).

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
            type: 'java.math.BigInteger'
            mode: COUNTER
          - name: name
            type: 'java.lang.String'
            values: ['Some name', 'Other name', 'Some other']
```

On the **top level** these keys are supported:

| Key | Type | Description |
| --- | ---- | ----------- |
| `seed` | `BigInteger` | global random seed |
| `schemas` | `List` | list of schemas (see below) |

The `seed` option sets a random seed with which you can vary the content of the database.

For each **schema**, these subkeys are supported:

| Key | Type | Description |
| --- | ---- | ----------- |
| `name` | `String` | name of the database schema |
| `tables` | `List` | list of tables in this schema (see below) |

For each **table**, these subkeys are supported:

| Key | Type | Description |
| --- | ---- | ----------- |
| `name` | `String` | name of the database table |
| `writeable` | `boolean` | writeable or not |
| `size` | `BigInteger` | number of records in this table |
| `columns` | `List` | list of columns in this table (see below) |

If `writeable` option is set to true, then an additional layer
will be added over the read-only table,
which accepts and stores insertions, updates, and deletions,
and it gives the effect that the table is writeable.

For each **column**, these subkeys are supported:

| Key | Type | Description |
| --- | ---- | ----------- |
| `name` | `String` | name of the table column |
| `type` | `String` (`Class<?>`) | java class name of column type |
| `mode` | `String` | filling mode (`DEFAULT`, `COUNTER`, `FIXED` or `ENUM`) |
| `nullCount` | `BigInteger` | count of null values (default: `0`) |
| `values` | `List` | explicit list of possible values |
| `valuesResource` | `String` | name of a java resource which contains the values line by line |
| `valuesBundle` | `String` | short name of a bundled value resource, otherwise similar to `valuesResource` (see below) |
| `valuesRange` | `List<BigInteger>` | start and end value of a numeric value range |
| `valuesPattern` | `String` | [strex](https://github.com/davidsusu/strex) regex pattern for values (reverse indexed) |
| `valuesDynamicPattern` | `String` | arbitrary regex pattern for values (not reverse indexed) |
| `valuesForeignColumn` | `List<String>` | use value set of a foreign `COUNTER` column |

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

For specifying the possible values in the column, one of
`values`, `valuesResource`, `valuesRange`, `valuesPattern`,  `valuesDynamicPattern` and `valuesForeignColumn`
can be used.
Currently, for a `FIXED` column, only `values` is supported.

In the case of `COUNTER` mode, values will be ignored and should be omitted.
The type of a `COUNTER` column is always `java.math.BigInteger`.

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


## Load values from resource

You can use custom predefined value sets too.
To do this, create a file with one value on each line.
Make this file available to the java classloader.
If you use docker, the easiest way to do this is to copy the file into the `/app/resources` directory:

```dockerfile
FROM miniconnect/holodb:latest

COPY config.yaml /app/config.yaml
COPY my-values.txt /app/resources/my-values.txt
```

You can use a predefined value set resource with the `valuesResource` key in `config.yaml`:

```yaml
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

## Run queries

There is a
[miniconnect REPL](https://github.com/miniconnect/miniconnect/tree/master/projects/repl).
Just type the host and port, and execute your queries:

```
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

SQL > SHOW TABLES

  Query was successfully executed!

  ┌───────────────────┐
  │ Tables_in_economy │
  ├───────────────────┤
  │ companies         │
  │ employees         │
  │ sales             │
  └───────────────────┘

SQL > SELECT * FROM companies

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

Also, you can use a MiniConnect server or even an existing MiniConnect `Session` via JDBC.
For more information,
see [MiniConnect JDBC compatibility](https://github.com/miniconnect/miniconnect#jdbc-compatibility).

## Connect to database from java code

You can connect to a HoloDB database via [miniconnect](https://github.com/miniconnect/miniconnect).

From code, you can open a miniconnect session like this:

```java

try (ClientMessenger clientMessenger = new ClientMessenger(host, port)) {
    MiniSessionManager sessionManager = new MessengerSessionManager(clientMessenger);
    try (MiniSession session = sessionManager.openSession()) {
        // ...
    }
}
```

## Embedded mode

You can use HoloDB as an embedded database.

To achieve this, specify a resource:

```
jdbc:holodb:embedded:resource://config.yaml
```

Or any file on the file system:

```
jdbc:holodb:embedded:file///path/to/config.yaml
```

(Note: Number of slashes does matter.)

## Mock JPA entities

To mock JPA entities, define the `jpa` subproject as a dependency, and change the JDBC connection URL to:

```
jdbc:holodb:jpa://
```

(Optionally, the schema can also be specified, e.g. `jdbc:holodb:jpa:///my_schema_name`.)

At the moment, schema construction is not automatic, it's necessary to explicitly pass the metamodel.
For example (if you use Micronaut):

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

For other frameworks, the solution should be similarly simple.

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
Additionally, by changing the root seed the entire dataset can be shuffled (also a near-no-op).

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
