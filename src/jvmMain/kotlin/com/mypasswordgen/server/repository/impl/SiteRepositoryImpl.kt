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

import com.mypasswordgen.server.model.Site
import com.mypasswordgen.server.repository.SiteRepository
import com.mypasswordgen.server.repository.crypto.encode
import com.mypasswordgen.server.tables.Sites
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import java.util.*

private val logger = KotlinLogging.logger("SiteRepositoryImpl")

class SiteRepositoryImpl : SiteRepository {

    override fun createAndGetId(siteName: String, emailId: UUID): UUID {
        logger.debug { "createAndGetId" }
        return Sites.insertAndGetId {
            it[this.siteName] = siteName.encode()
            it[this.emailId] = emailId
        }.value
    }

    override fun getById(id: UUID): Site? {
        logger.debug { "getById" }
        return Site.findById(id)
    }


    override fun getByNameAndEmail(siteName: String, emailId: UUID): Site? {
        logger.debug { "getByNameAndEmail" }
        return Site.find {
            Sites.siteName eq siteName.encode() and (Sites.emailId eq emailId)
        }.firstOrNull()
    }

    override fun delete(site: Site) {
        logger.debug { "delete" }
        return site.delete()
    }


}
