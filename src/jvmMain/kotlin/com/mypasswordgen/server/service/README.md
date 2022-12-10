# Service

The services handle the logic of the application. They work with the
arguments passed by the controller and access the database through
the repositories. Services may use other services that are lower in the
following chain:

Session &gt; User &gt; Email &gt; Site

so the session service may use the user service, but not the other way around.

If a transaction is needed to perform the logic, it is started in a service
function.

Services may also use mappers to transform data from one class to another.
