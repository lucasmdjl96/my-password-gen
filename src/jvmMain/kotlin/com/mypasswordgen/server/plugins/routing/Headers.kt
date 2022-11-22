/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

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
                    "connect-src 'self' https://fonts.googleapis.com https://fonts.gstatic.com https://www.paypalobjects.com;" +
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
