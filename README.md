# HoloDB

Relational database seemingly filled with random data.

> :construction: This project is in an incubating state. See [TODOs](./TODO.md).

## A holographic database

HoloDB is a storage engine for the `rdbms-framework`,
which introduces the concept of *holographic databases*.

It provides an arbitrarily large database filled with constrained random data.
Parameters and constraints can be specified in a configuration file.
Initialization ("filling" with data) of the tables is a no-op.
Query results are calculated on-the-fly.
Value providers are encouraged to calculate any single field of a column
practically in `O(1)`, but at most in `O(log(tableSize))` time.

As initialization is a no-op, it's particularly suitable for testing
and, in the case of a read-only database,
flexible orchestration, replication like a static content.
