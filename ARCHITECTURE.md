# Architecture

## Data Model (a.k.a. what, no database?)

Although the problem description recommended `biopython`, I thought that given the small data space (only a few million
base pairs in total) I'd try simplifying the service by managing all data directly. This kind of data fits easily into
memory, and the right data structures make queries fast, so I forego a database! The `dna.clj` module loads all
necessary information. Naturally, with a greater expectation of extensibility, the library becomes a better bet; and
with a greater expectation of certain kinds of scaling, a database becomes a better bet. I discuss this more below. See
also [TODO.md](TODO.md) for further potential improvements.

### The data structures

Each genome has a full-genome file, and a FASTA file listing gene locations. The full genome is loaded into memory as a
simple string, and the gene locations are put into an interval map, which is a data structure that efficiently queries a
location against overlapping intervals. This is particularly useful in this case because of the potential for
overlapping genes.

![downstream](diagrams/downstream.png?raw=true "Downstream")

### Extending to more data

As we add more and/or longer genomes, this model becomes too hard on memory. I would probably choose PostgreSQL as my
database implementation because it knocks this out of the park while also being incredibly flexible; additional
requirements are unlikely to require a change of database implementation.

A correct extension (while preserving the ability to return multiple results) is to index genomes using an [inverted
index](https://en.wikipedia.org/wiki/Inverted_index), mapping search strings to locations in genomes. Then a [range
index](https://www.postgresql.org/docs/9.5/rangetypes.html) can map from locations to genes.

## User sessions

This site uses simple HTML5 web storage to persist user sessions.

In the code, this fits naturally into `re-frame`'s coeffects model. Storage is made available to an event handler with
`(rf/inject-cofx :store)`.

## Asynchronous requests

The reactive architecture of `re-frame` means that updates happen without blocking user input. Since the entire dataset
is in memory and the operations are fast, any delays should not be perceptible at the current scale.

To support more expensive operations or greater scale, a simple job queue could be included.

I've taken some creative liberty by making this app give live feedback, and a "query history" doesn't fit well into that
model, but for reference, such a feature could be built into web storage by creating a `re-frame` subscription that
accumulates all queries and results.

### `re-frame` model

1. Event Dispatch, e.g. an arrow key triggering a nucleotide event
2. Event Handling, e.g. the nucleotide event appending a letter to the query
3. Effect Handling, e.g. the query being sent to the server
4. Query, e.g. the amino-acid decorator querying the last three triplet of the query
5. View, e.g. the component that renders the amino acid

## Results

The requirements only ask for a single genome result, but I return _all_ genomes that contain the queried string.

## Why Clojure and Luminus?

I happened to have used `re-frame` most recently and it's React-based. It has a nice functional-reactive-programming
model that is natural for this exercise.

## Flexibility

This architecture is flexible enough to move in a number of different directions with speed. There are doubtless errors
or deficiencies, but they are correctable. Client debugging is easy, either client or server could be swapped out
entirely (for example, replacing the server with a biopython backend), and more infrastructure (database, data
warehouse, job queue) is easy to integrate.
