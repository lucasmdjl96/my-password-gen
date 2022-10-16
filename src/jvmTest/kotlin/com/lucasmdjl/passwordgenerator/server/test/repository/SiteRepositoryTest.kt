package com.lucasmdjl.passwordgenerator.server.test.repository

import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.Site
import com.lucasmdjl.passwordgenerator.server.repository.impl.SiteRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.tables.Sites
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SiteRepositoryTest : RepositoryTestParent() {

    @Nested
    inner class CreateAndGetId {

        @Test
        fun `create when it doesn't exist`() {
            val siteRepository = SiteRepositoryImpl()
            testTransaction {
                val beforeSites = Sites.select { Sites.emailId eq 2 }
                val beforeCount = beforeSites.count()
                val beforeIds = beforeSites.map { it[Sites.id].value }
                val email = Email.findById(2)!!
                val siteId = siteRepository
                    .createAndGetId("not-site", email)
                val afterSites = Sites.select { Sites.emailId eq 2 }
                val afterCount = afterSites.count()
                val afterIds = afterSites.map { it[Sites.id].value }
                assertNotNull(siteId)
                assertTrue(siteId !in beforeIds)
                assertTrue(siteId in afterIds)
                assertEquals(beforeCount + 1, afterCount)
                val site = Site.findById(siteId)
                assertNotNull(site)
                assertEquals("not-site", site.name)
                assertEquals(2, site.email.id.value)
            }
        }

        @Test
        fun `create when it already exist`() {
            val siteRepository = SiteRepositoryImpl()
            testTransaction {
                val beforeSites = Sites.select { Sites.emailId eq 2 }
                val beforeCount = beforeSites.count()
                val email = Email.findById(2)!!
                val siteId = siteRepository
                    .createAndGetId("site002", email)
                val afterSites = Sites.select { Sites.emailId eq 2 }
                val afterCount = afterSites.count()
                assertNull(siteId)
                assertEquals(beforeCount, afterCount)
            }
        }

    }

    @Nested
    inner class GetById {

        @Test
        fun `get by id when it exists`() {
            val siteRepository = SiteRepositoryImpl()
            testTransaction {
                val site = siteRepository.getById(2)
                assertNotNull(site)
                assertEquals(2, site.id.value)
            }
        }

        @Test
        fun `get by id when it doesn't exist`() {
            val siteRepository = SiteRepositoryImpl()
            testTransaction {
                val site = siteRepository.getById(50)
                assertNull(site)
            }
        }

    }

    @Nested
    inner class GetByNameAndEmail {

        @Test
        fun `get when exists`() {
            val siteRepository = SiteRepositoryImpl()
            testTransaction {
                val email = Email.findById(2)!!
                val site = siteRepository.getByNameAndEmail("site002", email)
                assertNotNull(site)
                assertEquals("site002", site.name)
                assertEquals(2, site.email.id.value)
            }
        }

        @Test
        fun `get when exists in other email`() {
            val siteRepository = SiteRepositoryImpl()
            testTransaction {
                val email = Email.findById(3)!!
                val site = siteRepository.getByNameAndEmail("site002", email)
                assertNull(site)
            }
        }

        @Test
        fun `get when site doesn't exist`() {
            val siteRepository = SiteRepositoryImpl()
            testTransaction {
                val email = Email.findById(2)!!
                val site = siteRepository.getByNameAndEmail("not-site", email)
                assertNull(site)
            }
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete site`() {
            val siteRepository = SiteRepositoryImpl()
            testTransaction {
                val before = Sites.selectAll().count()
                val site = Site.findById(2)!!
                siteRepository.delete(site)
                val after = Sites.selectAll().count()
                assertEquals(before - 1, after)
                assertNull(Site.findById(2))
            }
        }

    }

}
