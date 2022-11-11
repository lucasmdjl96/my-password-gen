package com.mypasswordgen.server.plugins.routing

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Routing.installHeaders() {
    intercept(ApplicationCallPipeline.Call) {
        call.response.header(
            "Content-Security-Policy",
            "default-src 'self';" +
                    "style-src 'unsafe-inline' 'self' https://fonts.googleapis.com;" +
                    "script-src 'unsafe-inline' 'self';" +
                    "font-src https://fonts.gstatic.com;" +
                    "connect-src 'self' https://fonts.googleapis.com https://fonts.gstatic.com;" +
                    "object-src 'none';" +
                    "img-src 'self' https://www.paypalobjects.com;" +
                    "base-uri 'none';" +
                    "frame-ancestors 'none';"
        )
        call.response.header("X-Content-Type-Options", "nosniff")
        when (call.request.path()) {
            "/static/js/service-worker.js" -> call.response.header("Service-Worker-Allowed", "/")
        }
    }
}
