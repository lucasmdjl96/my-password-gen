/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.controller

import com.mypasswordgen.common.routes.SiteRoute
import io.ktor.server.application.*

interface SiteController {

    suspend fun post(call: ApplicationCall, siteRoute: SiteRoute.New)

    suspend fun get(call: ApplicationCall, siteRoute: SiteRoute.Find)

    suspend fun delete(call: ApplicationCall, siteRoute: SiteRoute.Delete)

}
