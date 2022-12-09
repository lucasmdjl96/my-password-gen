/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.repository.impl

import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.SessionRepository
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger("SessionRepositoryImpl")

class SessionRepositoryImpl : SessionRepository {

    override fun create(): Session {
        logger.debug { "create" }
        return Session.new {}
    }

    override fun getById(sessionId: UUID): Session? {
        logger.debug { "getById" }
        return Session.findById(sessionId)
    }

    override fun delete(session: Session) {
        logger.debug { "delete" }
        session.delete()
    }

    override fun setLastUser(session: Session, user: User?) {
        logger.debug { "setLastUser" }
        session.lastUser = user
    }

    override fun getLastUser(session: Session): User? {
        logger.debug { "getLastUser" }
        return session.lastUser
    }

    override fun getLastUser(sessionId: UUID): User? {
        logger.debug { "getLastUser" }
        return getById(sessionId)?.lastUser
    }

    override fun setLastUser(sessionId: UUID, user: User?) {
        logger.debug { "setLastUser" }
        getById(sessionId)?.lastUser = user
    }

}
