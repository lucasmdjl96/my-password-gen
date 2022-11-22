/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object Users : UUIDTable() {

    val username = varchar("username", 64)
    var sessionId = reference("session_id", Sessions.id, onDelete = ReferenceOption.CASCADE)
    var lastEmailId = reference("last_email_id", Emails.id, onDelete = ReferenceOption.SET_NULL).nullable()

    init {
        uniqueIndex(username, sessionId)
        index(isUnique = false, sessionId)
        index(isUnique = true, lastEmailId)
    }

}
