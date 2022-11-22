/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.model

import com.mypasswordgen.server.tables.Sessions
import com.mypasswordgen.server.tables.Users
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Session(id: EntityID<UUID>) : Entity<UUID>(id) {

    val users by User referrersOn Users.sessionId
    var lastUser by User optionalReferencedOn Sessions.lastUserId

    override fun toString(): String {
        return "[Session#${id.value}]"
    }

    companion object : EntityClass<UUID, Session>(Sessions)

}
