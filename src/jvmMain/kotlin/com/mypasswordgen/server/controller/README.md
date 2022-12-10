# Controllers

The controllers are responsible for receive all the
necessary arguments from the client request call and pass
them over to the appropriate service.

The controllers' functions are named after the http method
they respond to and take two parameters: the request call
and the route. So a typical function signature would be:

fun get(call: ApplicationCall, route: AbcRoute): Unit
