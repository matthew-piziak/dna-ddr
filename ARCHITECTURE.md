# Architecture

## Implementation

Although the problem description recommended `biopython`, I thought that given the small data space (only a few million
base pairs in total) I'd try simplifying the service by querying the data directly. Naturally this only makes sense when
new genomes do not have to be added. With a greater expectation of extensibility, the library becomes a better bet.

## Data Model (a.k.a. what, no database?)

A quick inspection revealed that the genomes in the requirements only had a few million base pairs. This fits easily
into memory, so I decided to go for speed and simplicity forego a database. The `dna.clj` module loads all necessary
information.

### Extending to more data

As we add more and/or longer genomes, this model becomes too hard on memory. I would probably choose PostgreSQL as my
database implementation because it knocks this out of the park while also being incredibly flexible; additional
requirements are unlikely to require a change of database implementation.

A correct extension (while preserving the ability to return multiple results) is to index genomes using an [inverted
index](https://en.wikipedia.org/wiki/Inverted_index), mapping search strings to locations in genomes. Then a [range index](https://www.postgresql.org/docs/9.5/rangetypes.html) can map from locations to genes.

## User sessions

This site uses simple HTML5 web storage to persist user sessions.

In the code, this fits naturally into `re-frame`'s coeffects model. Storage is made available to an event handler with
`(rf/inject-cofx :store)`.

## Asynchronous requests

The reactive architecture of `re-frame` means that updates happen without blocking user input. Since the entire dataset
is in memory and the operations are fast, any delays should not be perceptible at the current scale.

To support more expensive operations or greater scale, a simple job queue like could be included.

## Results

The requirements only ask for a single genome result, but I return _all_ genomes that contain the queried string.
