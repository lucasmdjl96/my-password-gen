# Repositories

The repositories are responsible to relaying to the database the work
that the services need to perform.

All functions are assumed to be performed inside a transaction, so no
transactions will be created here.

The repositories are also responsible for encoding the data before it is
sent to the database.
