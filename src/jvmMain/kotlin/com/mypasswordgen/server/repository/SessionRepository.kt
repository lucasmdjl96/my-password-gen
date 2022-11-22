/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.repository

import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import java.util.*

interface SessionRepository {

    fun create(): Session

    fun getById(sessionId: UUID): Session?

    fun delete(session: Session)

    fun setLastUser(session: Session, user: User?)

    fun getLastUser(session: Session): User?

    fun getLastUser(sessionId: UUID): User?

    fun setLastUser(sessionId: UUID, user: User?)

    fun getIfLastUser(sessionId: UUID, username: String): User?

}
