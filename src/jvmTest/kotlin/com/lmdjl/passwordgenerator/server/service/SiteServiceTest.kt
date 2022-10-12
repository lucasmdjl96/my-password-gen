package com.lmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.Site
import com.lucasmdjl.passwordgenerator.server.repository.SiteRepository
import com.lucasmdjl.passwordgenerator.server.service.EmailService
import com.lucasmdjl.passwordgenerator.server.service.impl.SiteServiceImpl
import com.lucasmdjl.passwordgenerator.server.tables.Emails
import com.lucasmdjl.passwordgenerator.server.tables.Sites
import io.mockk.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SiteServiceTest : ServiceTestParent() {

    private lateinit var emailServiceMock: EmailService
    private lateinit var siteRepositoryMock: SiteRepository

    private lateinit var dummySessionId: UUID
    private lateinit var dummyEmail: Email
    private lateinit var dummySite: Site
    private var dummySiteId = 0
    private lateinit var dummySiteServerDto: SiteServerDto

    @BeforeAll
    override fun initMocks() {
        emailServiceMock = mockk()
        siteRepositoryMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        dummyEmail = Email(EntityID(1, Emails))
        dummySite = Site(EntityID(2, Sites))
        dummySiteId = 3
        dummySiteServerDto = SiteServerDto("coolWeb", "email@email.com", "user123")
    }

    @Nested
    inner class Create {

        @Test
        fun `create site when email exists and site doesn't exist yet`() {
            every { emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId) } returns dummyEmail
            every { siteRepositoryMock.createAndGetId(dummySiteServerDto.siteName, dummyEmail) } returns dummySiteId
            every { siteRepositoryMock.getById(dummySiteId) } returns dummySite
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, emailServiceMock)

            val siteResult = siteService.create(dummySiteServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId)
                siteRepositoryMock.createAndGetId(dummySiteServerDto.siteName, dummyEmail)
                siteRepositoryMock.getById(dummySiteId)
            }
            assertEquals(dummySite, siteResult)
        }

        @Test
        fun `create site when email exists and site already exists`() {
            every { emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId) } returns dummyEmail
            every { siteRepositoryMock.createAndGetId(dummySiteServerDto.siteName, dummyEmail) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, emailServiceMock)

            val siteResult = siteService.create(dummySiteServerDto, dummySessionId)

            verify(exactly = 0) {
                siteRepositoryMock.getById(any())
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId)
                siteRepositoryMock.createAndGetId(dummySiteServerDto.siteName, dummyEmail)
            }
            assertNull(siteResult)
        }

        @Test
        fun `create site when email doesn't exist`() {
            every { emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, emailServiceMock)

            assertThrows<Exception> { siteService.create(dummySiteServerDto, dummySessionId) }

            verify {
                transaction(statement = any<Transaction.() -> Any>())
                emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId)
                siteRepositoryMock wasNot Called
            }
        }

    }

    @Nested
    inner class Find {

        @Test
        fun `find site when email and site exist`() {
            every { emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns dummySite
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, emailServiceMock)

            val siteResult = siteService.find(dummySiteServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
            }
            assertEquals(dummySite, siteResult)
        }

        @Test
        fun `find site when email exists but site doesn't exist`() {
            every { emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, emailServiceMock)

            val siteResult = siteService.find(dummySiteServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
            }
            assertNull(siteResult)
        }

        @Test
        fun `find site when email doesn't exist`() {
            every { emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, emailServiceMock)

            assertThrows<Exception> { siteService.find(dummySiteServerDto, dummySessionId) }
            verify {
                siteRepositoryMock wasNot Called
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId)
            }
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete site when email and site exist`() {
            every { emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns dummySite
            every { siteRepositoryMock.delete(dummySite) } just Runs
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, emailServiceMock)

            val result = siteService.delete(dummySiteServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
                siteRepositoryMock.delete(dummySite)
            }
            assertEquals(Unit, result)
        }

        @Test
        fun `delete site when email exists but site doesn't exist`() {
            every { emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, emailServiceMock)

            val result = siteService.delete(dummySiteServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
            }
            verify(exactly = 0) {
                siteRepositoryMock.delete(any())
            }
            assertNull(result)
        }

        @Test
        fun `delete site when email doesn't exist`() {
            every { emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId) } returns null
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, emailServiceMock)

            assertThrows<Exception> { siteService.delete(dummySiteServerDto, dummySessionId) }
            verify {
                siteRepositoryMock wasNot Called
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                emailServiceMock.find(dummySiteServerDto.emailServerDto, dummySessionId)
            }
        }

    }

}
