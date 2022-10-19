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
        fun `create when it doesn't exist`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
                INSERT INTO SITES (SITE_NAME, EMAIL_ID)
                    VALUES ('Site001', 1);
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val beforeSites = Sites.select { Sites.emailId eq 1 }
            val beforeCount = beforeSites.count()
            val beforeIds = beforeSites.map { it[Sites.id].value }
            val email = Email.findById(1)!!
            val siteId = siteRepository
                .createAndGetId("not-site", email)
            val afterSites = Sites.select { Sites.emailId eq 1 }
            val afterCount = afterSites.count()
            val afterIds = afterSites.map { it[Sites.id].value }
            assertNotNull(siteId)
            assertTrue(siteId !in beforeIds)
            assertTrue(siteId in afterIds)
            assertEquals(beforeCount + 1, afterCount)
            val site = Site.findById(siteId)
            assertNotNull(site)
            assertEquals("not-site", site.name)
            assertEquals(1, site.email.id.value)
        }

        @Test
        fun `create when it already exist`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
                INSERT INTO SITES (SITE_NAME, EMAIL_ID)
                    VALUES ('Site001', 1);
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val beforeSites = Sites.select { Sites.emailId eq 1 }
            val beforeCount = beforeSites.count()
            val email = Email.findById(1)!!
            val siteId = siteRepository
                .createAndGetId("Site001", email)
            val afterSites = Sites.select { Sites.emailId eq 1 }
            val afterCount = afterSites.count()
            assertNull(siteId)
            assertEquals(beforeCount, afterCount)
        }

    }

    @Nested
    inner class GetById {

        @Test
        fun `get by id when it exists`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
                INSERT INTO SITES (SITE_NAME, EMAIL_ID)
                    VALUES ('Site001', 1);
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val site = siteRepository.getById(1)
            assertNotNull(site)
            assertEquals(1, site.id.value)
        }

        @Test
        fun `get by id when it doesn't exist`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
                INSERT INTO SITES (SITE_NAME, EMAIL_ID)
                    VALUES ('Site001', 1);
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val site = siteRepository.getById(2)
            assertNull(site)
        }

    }

    @Nested
    inner class GetByNameAndEmail {

        @Test
        fun `get when exists`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
                INSERT INTO SITES (SITE_NAME, EMAIL_ID)
                    VALUES ('Site001', 1);
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val email = Email.findById(1)!!
            val site = siteRepository.getByNameAndEmail("Site001", email)
            assertNotNull(site)
            assertEquals("Site001", site.name)
            assertEquals(1, site.email.id.value)
        }

        @Test
        fun `get when exists in other email`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email002', 1);
                INSERT INTO SITES (SITE_NAME, EMAIL_ID)
                    VALUES ('Site001', 1);
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val email = Email.findById(2)!!
            val site = siteRepository.getByNameAndEmail("Site001", email)
            assertNull(site)
        }

        @Test
        fun `get when site doesn't exist`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
                INSERT INTO SITES (SITE_NAME, EMAIL_ID)
                    VALUES ('Site001', 1);
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val email = Email.findById(1)!!
            val site = siteRepository.getByNameAndEmail("not-site", email)
            assertNull(site)
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete site`() = testTransaction {
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO USERS (USERNAME, SESSION_ID) 
                    VALUES ('User123', '868f9d04-d1e8-44c9-84d3-2ef3da517d4c');
                INSERT INTO EMAILS (EMAIL_ADDRESS, USER_ID) 
                    VALUES ('Email001', 1);
                INSERT INTO SITES (SITE_NAME, EMAIL_ID)
                    VALUES ('Site001', 1);
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val before = Sites.selectAll().count()
            val site = Site.findById(1)!!
            siteRepository.delete(site)
            val after = Sites.selectAll().count()
            assertEquals(before - 1, after)
            assertNull(Site.findById(1))
        }

    }

}
