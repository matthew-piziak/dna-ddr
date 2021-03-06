# TODO

This application is intended to be something to build on, rather than a finished production, so here are potential
improvements.

# Input Validation

The current app does not perform any input validation, so you can type all manner of garbage into the input and the app
does not appropriately warn you or prevent your misbehavior. It should appropriately validate on the clientside and
serverside. The current version does give you a break with capital letters, and it lets the user use ATCG instead of
↑←↓→, in case the instructions aren't clear.

## spec

Clojure has an outstanding data-validation library called [spec](https://clojure.org/guides/spec). It should be used to
specify allowed values throughout the program to increase robustness, not just for input validation.

# Tests

The application should be tested. In particular, there is an excellent opportunity for a good property test: pick out a
random subsequence from a protein (handling complements), and then running it back through the search function to see if
it finds that protein again.

# `join` and `complement` cases should be verified

Right now I don't have a lot of confidence that this is correct.

# Some repeated code can be made terser

Editor-driven coding is a great thing, but not for a collaborative codebase. Repeated lines should be abstracted away,
within reason.

# Client code should be better modularized

To better separate views, subscriptions, and styles.

# Structured Responses

Right now they return raw responses. The server should do more work here to prepare them for the client. It can be
encoded using Extensible Data Notation.

# Disable arrow-key scrolling

The down-key scrolls down, as well as inputting G, which is confusing.
