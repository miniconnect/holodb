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

For some self-contained examples see the `examples` directory:

[https://github.com/miniconnect/holodb/tree/master/examples](https://github.com/miniconnect/holodb/tree/master/examples)

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

The `seed` option sets a random seed with which you can vary the content of the database.

If `writeable` option is set to true, then an additional layer
will be added over the read-only table,
which accepts and stores insertions, updates, and deletions,
and it gives the effect that the table is writeable.

Currently, there are three possible values of the `mode` option:

- `DEFAULT`: the column will be filled up randomly with the values specified in `values`
- `COUNTER`: column values will be ascending integers starting from 1
- `FIXED`: the values will be the same and in the same order as specified in `values`

`DEFAULT` and `COUNTER` columns are reverse-indexed
and can be queried efficiently even in case of very large table sizes.
For `FIXED` columns, the size of `values` must be equal to the size of the table
(recommended for smaller tables only).

## Run queries

You can connect to a holodb database via [miniconnect](https://github.com/miniconnect/miniconnect).

From code, you can open a miniconnect session like this:

```java

try (ClientMessenger clientMessenger = new ClientMessenger(host, port)) {
    MiniSessionManager sessionManager = new MessengerSessionManager(clientMessenger);
    try (MiniSession session = sessionManager.openSession()) {
        // ...
    }
}
```

If you want to use JDBC, you can wrap the miniconnect session in a JDBC adapter:

```java
Connection connection = new MiniJdbcConnection(session, null);
// ...
```

Note: one of the major goals of miniconnect is
to relieve the pains of JDBC users and implementors.
For more information see its repo.

There is also a
[miniconnect REPL](https://github.com/miniconnect/miniconnect/tree/master/projects/repl).
Just type the host and port, and execute your queries:

```
Host [localhost]: 
Port [3430]: 

Welcome in miniConnect SQL REPL!

SQL > USE my_schema

  Result contains no rows!

SQL > SELECT * FROM my_table ORDER BY id LIMIT 7

  +----+------------+
  | id | name       |
  +----+------------+
  |  1 | Some name  |
  |  2 | Some other |
  |  3 | Some other |
  |  4 | Some name  |
  |  5 | Other name |
  |  6 | Some name  |
  |  7 | Other name |
  +----+------------+

SQL > SELECT * FROM my_table WHERE name = 'Some name' ORDER BY id LIMIT 5

  +----+-----------+
  | id | name      |
  +----+-----------+
  |  1 | Some name |
  |  4 | Some name |
  |  6 | Some name |
  |  8 | Some name |
  | 20 | Some name |
  +----+-----------+

SQL > exit

Bye-bye!
```

## Predefined value sets

You can use predefined value sets too.
To do this, create a file with one value on each line.
Make this file available to the java classloader.
If you use docker, the easiest way to do this is to copy the file into the `/app/resources` directory:

```dockerfile
FROM miniconnect/holodb:latest

COPY config.yaml /app/config.yaml
COPY my-values.txt /app/resources/my-values.txt
```

There are some built-in value set resources too:

- `hu/webarticum/holodb/values/cities.txt`
- `hu/webarticum/holodb/values/colors.txt`
- `hu/webarticum/holodb/values/countries.txt`
- `hu/webarticum/holodb/values/female-forenames.txt`
- `hu/webarticum/holodb/values/forenames.txt`
- `hu/webarticum/holodb/values/fruits.txt`
- `hu/webarticum/holodb/values/log-levels.txt`
- `hu/webarticum/holodb/values/lorem.txt`
- `hu/webarticum/holodb/values/male-forenames.txt`
- `hu/webarticum/holodb/values/months.txt`
- `hu/webarticum/holodb/values/surnames.txt`
- `hu/webarticum/holodb/values/weekdays.txt`

You can use a predefined value set resource with the `valuesResource` key in `config.yaml`:

```yaml
          - name: color
            type: 'java.lang.String'
            valuesResource: 'hu/webarticum/holodb/values/colors.txt'
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

## How does it work?

Holographic databases store no real data and calculate field values and reverse indexes on-the-fly.
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

## Version table

<table>
  <thead>
    <tr>
      <th>
        miniconnect-api
      </th>
      <th>
        <a href="https://github.com/miniconnect/miniconnect">miniconnect</a>
      </th>
      <th>
        minibase
      </th>
      <th>
        <a href="https://github.com/miniconnect/holodb">holodb</a>
      </th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td rowspan="2">---</td>
      <td><a href="https://github.com/miniconnect/miniconnect#version-020">0.2.0</a></td>
      <td rowspan="2">---</td>
      <td><a href="#version-020">0.2.0</a></td>
    </tr>
    <tr>
      <td><a href="https://github.com/miniconnect/miniconnect#version-010">0.1.0</a></td>
      <td><a href="#version-010">0.1.0</a></td>
    </tr>
  </tbody>
</table>

## Changelog

### Version 0.2.0

- Add support for regular expression based value generation
- Add some other configuration possibilities
- Add proper support for NULL values
- Improve support for sequences
- Improve examples
- Fix many problems
- and more &hellip;

### Version 0.1.0

- Data source framework
- YAML configuration support
- Docker integration
- and more &hellip;
