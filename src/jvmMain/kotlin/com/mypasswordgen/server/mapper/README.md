# Mappers

Mappers transform an element of a class into another class. Typically,
they transform an object of a model class into an object of a dto class.

There should always be both a function taking the source object as an argument,
and one taking the source object as a receiver, one defined in terms of
the other.

If access to the database is required it should be assumed that the
function is already inside a transaction, so no transaction should be
created in a mapper.
