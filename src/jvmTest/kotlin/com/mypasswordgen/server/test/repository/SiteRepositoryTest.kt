package com.mypasswordgen.server.test.repository

import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.Site
import com.mypasswordgen.server.repository.impl.SiteRepositoryImpl
import com.mypasswordgen.server.tables.Sites
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SiteRepositoryTest : RepositoryTestParent() {

    @Nested
    inner class CreateAndGetId {

        @Test
        fun `create when it doesn't exist`() = testTransaction {
            val initSessionId = UUID.fromString("04747f3c-cabe-4c2c-8012-4db5a2ff852c")
            val initUserId = UUID.fromString("2085bfdc-661c-4fad-9dd4-872f7255034c")
            val initEmailId = UUID.fromString("3a031c0f-9e9a-4e5d-a794-1dc91cbe5454")
            val initSiteId = UUID.fromString("892ffb40-0ee1-4100-a2fd-a8f80bfd4da0")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initSiteName = "Site001"
            val initSiteName2 = "not-site"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
                    VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val beforeSites = Sites.select { Sites.emailId eq initEmailId }
            val beforeCount = beforeSites.count()
            val beforeIds = beforeSites.map { it[Sites.id].value }
            val email = Email.findById(initEmailId)!!
            val siteId = siteRepository
                .createAndGetId(initSiteName2, email)
            val afterSites = Sites.select { Sites.emailId eq initEmailId }
            val afterCount = afterSites.count()
            val afterIds = afterSites.map { it[Sites.id].value }
            assertNotNull(siteId)
            assertTrue(siteId !in beforeIds)
            assertTrue(siteId in afterIds)
            assertEquals(beforeCount + 1, afterCount)
            val site = Site.findById(siteId)
            assertNotNull(site)
            assertEquals(initSiteName2, site.name)
            assertEquals(initEmailId, site.email.id.value)
        }

        @Test
        fun `create when it already exist`() {
            val initSessionId = UUID.fromString("04747f3c-cabe-4c2c-8012-4db5a2ff852c")
            val initUserId = UUID.fromString("dcc60fab-7766-4e3b-8860-c8d1a79e3be0")
            val initEmailId = UUID.fromString("9b6358e2-d9a3-4fd0-84fc-93d75ce0d3a9")
            val initSiteId = UUID.fromString("364e80e8-b98b-4e0e-afcf-257602068358")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initSiteName = "Site001"
            var beforeCount = 0L
            testTransaction {
                exec(
                    """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
                    VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                COMMIT;
            """.trimIndent()
                )
                val siteRepository = SiteRepositoryImpl()
                val beforeSites = Sites.select { Sites.emailId eq initEmailId }
                beforeCount = beforeSites.count()
                val email = Email.findById(initEmailId)!!
                assertThrows<Exception> {
                    siteRepository
                        .createAndGetId(initSiteName, email)
                }
            }
            testTransaction {
                val afterSites = Sites.select { Sites.emailId eq initEmailId }
                val afterCount = afterSites.count()
                assertEquals(beforeCount, afterCount)
            }
        }

    }

    @Nested
    inner class GetById {

        @Test
        fun `get by id when it exists`() = testTransaction {
            val initSessionId = UUID.fromString("04747f3c-cabe-4c2c-8012-4db5a2ff852c")
            val initUserId = UUID.fromString("dc1a9820-0e96-4fd0-a2d0-5df0bbe912ce")
            val initEmailId = UUID.fromString("40b2d4a9-db62-4896-8273-74785397f58c")
            val initSiteId = UUID.fromString("8ec81a0d-768a-4c17-8b52-5cf247656689")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initSiteName = "Site001"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
                    VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val site = siteRepository.getById(initSiteId)
            assertNotNull(site)
            assertEquals(initSiteId, site.id.value)
        }

        @Test
        fun `get by id when it doesn't exist`() = testTransaction {
            val initSessionId = UUID.fromString("04747f3c-cabe-4c2c-8012-4db5a2ff852c")
            val initUserId = UUID.fromString("281cd8b9-0b85-4b2b-a0cf-819cf2a4ce64")
            val initEmailId = UUID.fromString("0aefc461-8a62-4b94-b4d7-c02a130fe1f9")
            val initSiteId = UUID.fromString("2bcd8961-314a-47d5-bbee-0931a7127721")
            val initSiteId2 = UUID.fromString("1409db60-20cb-4f3f-89df-f9314c49914f")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initSiteName = "Site001"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
                    VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val site = siteRepository.getById(initSiteId2)
            assertNull(site)
        }

    }

    @Nested
    inner class GetByNameAndEmail {

        @Test
        fun `get when exists`() = testTransaction {
            val initSessionId = UUID.fromString("04747f3c-cabe-4c2c-8012-4db5a2ff852c")
            val initUserId = UUID.fromString("e451f501-6e22-4d74-ad50-9db4366c9b18")
            val initEmailId = UUID.fromString("9d267c0e-13e2-4c5c-a046-f90df628033e")
            val initSiteId = UUID.fromString("61205a47-46bb-4f58-a2f1-67dff414ef57")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initSiteName = "Site001"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
                    VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val email = Email.findById(initEmailId)!!
            val site = siteRepository.getByNameAndEmail(initSiteName, email)
            assertNotNull(site)
            assertEquals(initSiteName, site.name)
            assertEquals(initEmailId, site.email.id.value)
        }

        @Test
        fun `get when exists in other email`() = testTransaction {
            val initSessionId = UUID.fromString("04747f3c-cabe-4c2c-8012-4db5a2ff852c")
            val initUserId = UUID.fromString("e451f501-6e22-4d74-ad50-9db4366c9b18")
            val initEmailId = UUID.fromString("9d267c0e-13e2-4c5c-a046-f90df628033e")
            val initEmailId2 = UUID.fromString("7e551791-bf84-44a6-8d2b-f33f60b5bb63")
            val initSiteId = UUID.fromString("61205a47-46bb-4f58-a2f1-67dff414ef57")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initEmailAddress2 = "Email002"
            val initSiteName = "Site001"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId2', '$initEmailAddress2', '$initUserId');
                INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
                    VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val email = Email.findById(initEmailId2)!!
            val site = siteRepository.getByNameAndEmail(initSiteName, email)
            assertNull(site)
        }

        @Test
        fun `get when site doesn't exist`() = testTransaction {
            val initSessionId = UUID.fromString("04747f3c-cabe-4c2c-8012-4db5a2ff852c")
            val initUserId = UUID.fromString("e451f501-6e22-4d74-ad50-9db4366c9b18")
            val initEmailId = UUID.fromString("9d267c0e-13e2-4c5c-a046-f90df628033e")
            val initSiteId = UUID.fromString("61205a47-46bb-4f58-a2f1-67dff414ef57")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initSiteName = "Site001"
            val initSiteName2 = "not-site"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
                    VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val email = Email.findById(initEmailId)!!
            val site = siteRepository.getByNameAndEmail(initSiteName2, email)
            assertNull(site)
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete site`() = testTransaction {
            val initSessionId = UUID.fromString("04747f3c-cabe-4c2c-8012-4db5a2ff852c")
            val initUserId = UUID.fromString("e451f501-6e22-4d74-ad50-9db4366c9b18")
            val initEmailId = UUID.fromString("9d267c0e-13e2-4c5c-a046-f90df628033e")
            val initSiteId = UUID.fromString("61205a47-46bb-4f58-a2f1-67dff414ef57")
            val initUsername = "User123"
            val initEmailAddress = "Email001"
            val initSiteName = "Site001"
            exec(
                """
                INSERT INTO SESSIONS (ID) VALUES ('$initSessionId');
                INSERT INTO USERS (ID, USERNAME, SESSION_ID) 
                    VALUES ('$initUserId', '$initUsername', '$initSessionId');
                INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID) 
                    VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
                    VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
            """.trimIndent()
            )
            val siteRepository = SiteRepositoryImpl()
            val before = Sites.selectAll().count()
            val site = Site.findById(initSiteId)!!
            siteRepository.delete(site)
            val after = Sites.selectAll().count()
            assertEquals(before - 1, after)
            assertNull(Site.findById(initSiteId))
        }

    }

}
