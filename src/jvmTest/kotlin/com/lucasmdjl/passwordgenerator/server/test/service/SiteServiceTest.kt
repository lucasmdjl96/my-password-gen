package com.lucasmdjl.passwordgenerator.server.test.service

import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.Site
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.plugins.DataConflictException
import com.lucasmdjl.passwordgenerator.server.plugins.DataNotFoundException
import com.lucasmdjl.passwordgenerator.server.repository.SiteRepository
import com.lucasmdjl.passwordgenerator.server.repository.UserRepository
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import com.lucasmdjl.passwordgenerator.server.service.impl.SiteServiceImpl
import com.lucasmdjl.passwordgenerator.server.tables.Emails
import com.lucasmdjl.passwordgenerator.server.tables.Sites
import com.lucasmdjl.passwordgenerator.server.tables.Users
import io.mockk.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals

class SiteServiceTest : ServiceTestParent() {

    private lateinit var siteRepositoryMock: SiteRepository
    private lateinit var userRepositoryMock: UserRepository
    private lateinit var sessionServiceMock: SessionService

    private lateinit var dummySessionId: UUID
    private lateinit var dummyEmail: Email
    private lateinit var dummySite: Site
    private lateinit var dummyUser: User
    private lateinit var dummySiteId: UUID
    private lateinit var dummySiteServerDto: SiteServerDto

    @BeforeAll
    override fun initMocks() {
        siteRepositoryMock = mockk()
        userRepositoryMock = mockk()
        sessionServiceMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        dummyEmail = Email(EntityID(UUID.fromString("3560340f-3b40-45d4-8f34-44390a405872"), Emails))
        dummySite = Site(EntityID(UUID.fromString("d9741b0d-d9c5-4609-8234-2555e7d6c749"), Sites))
        dummyUser = User(EntityID(UUID.fromString("fd0265c1-8dc7-4ad9-be00-e0616d6e107e"), Users))
        dummySiteId = UUID.fromString("3fd37556-8aeb-4174-aa48-707e134b8a2c")
        dummySiteServerDto = SiteServerDto("coolWeb")
    }

    @Nested
    inner class Create {

        @Test
        fun `create site when email exists and site doesn't exist yet`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns null
            every { siteRepositoryMock.createAndGetId(dummySiteServerDto.siteName, dummyEmail) } returns dummySiteId
            every { siteRepositoryMock.getById(dummySiteId) } returns dummySite
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionServiceMock)

            val siteResult = siteService.create(dummySiteServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
                siteRepositoryMock.createAndGetId(dummySiteServerDto.siteName, dummyEmail)
                siteRepositoryMock.getById(dummySiteId)
            }
            assertEquals(dummySite, siteResult)
        }

        @Test
        fun `create site when email exists and site already exists`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns dummySite
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionServiceMock)

            assertThrows<DataConflictException> {
                siteService.create(dummySiteServerDto, dummySessionId)
            }

            verify(exactly = 0) {
                siteRepositoryMock.createAndGetId(dummySiteServerDto.siteName, dummyEmail)
                siteRepositoryMock.getById(any())
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
            }
        }

        @Test
        fun `create site when email doesn't exist`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionServiceMock)

            assertThrows<Exception> { siteService.create(dummySiteServerDto, dummySessionId) }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
            }
            verify {
                siteRepositoryMock wasNot Called
            }
        }

        @Test
        fun `create site when last user doesn't exist`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionServiceMock)

            assertThrows<Exception> { siteService.create(dummySiteServerDto, dummySessionId) }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
            }
            verify {
                siteRepositoryMock wasNot Called
                userRepositoryMock wasNot Called
            }
        }

    }

    @Nested
    inner class Find {

        @Test
        fun `find site when email and site exist`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns dummySite
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionServiceMock)

            val siteResult = siteService.find(dummySiteServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
            }
            assertEquals(dummySite, siteResult)
        }

        @Test
        fun `find site when email exists but site doesn't exist`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionServiceMock)

            assertThrows<DataNotFoundException> {
                siteService.find(dummySiteServerDto, dummySessionId)
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
            }
        }

        @Test
        fun `find site when email doesn't exist`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionServiceMock)

            assertThrows<Exception> { siteService.find(dummySiteServerDto, dummySessionId) }
            verify {
                siteRepositoryMock wasNot Called
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
            }
        }

        @Test
        fun `find site when last user doesn't exist`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionServiceMock)

            assertThrows<Exception> { siteService.find(dummySiteServerDto, dummySessionId) }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
            }
            verify {
                siteRepositoryMock wasNot Called
                userRepositoryMock wasNot Called
            }
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete site when email and site exist`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns dummySite
            every { siteRepositoryMock.delete(dummySite) } just Runs
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionServiceMock)

            val result = siteService.delete(dummySiteServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
                siteRepositoryMock.delete(dummySite)
            }
            assertEquals(Unit, result)
        }

        @Test
        fun `delete site when email exists but site doesn't exist`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionServiceMock)

            assertThrows<DataNotFoundException> {
                siteService.delete(dummySiteServerDto, dummySessionId)
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
            }
            verify(exactly = 0) {
                siteRepositoryMock.delete(any())
            }
        }

        @Test
        fun `delete site when email doesn't exist`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionServiceMock)

            assertThrows<Exception> { siteService.delete(dummySiteServerDto, dummySessionId) }
            verify {
                siteRepositoryMock wasNot Called
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
            }
        }

        @Test
        fun `delete site when last user doesn't exist`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionServiceMock)

            assertThrows<Exception> { siteService.delete(dummySiteServerDto, dummySessionId) }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
            }
            verify {
                siteRepositoryMock wasNot Called
                userRepositoryMock wasNot Called
            }
        }

    }

}
