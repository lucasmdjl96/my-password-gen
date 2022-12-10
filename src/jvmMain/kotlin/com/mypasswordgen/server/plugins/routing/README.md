# Server Routes

Routing on the server is done by defining extension functions
on the Route class. Each file groups routes according
to their theme, and each function in the file groups routes
according to the authentication needed.

Each routing file should be handled by one controller
which is added inside the function body via dependency injection.
Inside the function body the call should be passed to the
appropriate controller by using one of the http method
functions which take as parameter a controller method, e.g:

get&lt;AbcRoute&gt;(abcController::get)

The functions defined like this are then called in the Application.installRoutes
function in the Routes.kt file.
