/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.controller.impl

import com.mypasswordgen.common.routes.AboutRoute
import com.mypasswordgen.common.routes.ContributeRoute
import com.mypasswordgen.server.controller.AboutController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

class AboutControllerImpl : AboutController {

    override suspend fun get(call: ApplicationCall, aboutRoute: AboutRoute) {
        call.respondText(
            this::class.java.classLoader.getResource("html/about.html")!!.readText(),
            ContentType.Text.Html
        )
    }

    override suspend fun get(call: ApplicationCall, aboutRoute: ContributeRoute) {
        call.respondText(
            this::class.java.classLoader.getResource("contribute.json")!!.readText(),
            ContentType.Text.Plain
        )
    }

}
