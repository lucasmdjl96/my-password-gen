package com.mypasswordgen.server.test.service

import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.common.dto.fullServer.FullSiteServerDto
import com.mypasswordgen.common.dto.server.SiteServerDto
import com.mypasswordgen.server.mapper.SiteMapper
import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.Site
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.SiteRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.impl.SiteServiceImpl
import com.mypasswordgen.server.tables.Emails
import com.mypasswordgen.server.tables.Sites
import com.mypasswordgen.server.tables.Users
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
    private lateinit var sessionRepositoryMock: SessionRepository
    private lateinit var siteMapperMock: SiteMapper

    private lateinit var dummySessionId: UUID
    private lateinit var dummyEmail: Email
    private lateinit var dummySite: Site
    private lateinit var dummyUser: User
    private lateinit var dummySiteId: UUID
    private lateinit var dummySiteServerDto: SiteServerDto
    private lateinit var dummySiteClientDto: SiteClientDto
    private lateinit var dummyFullSiteServerDto: FullSiteServerDto

    @BeforeAll
    override fun initMocks() {
        siteRepositoryMock = mockk()
        userRepositoryMock = mockk()
        sessionRepositoryMock = mockk()
        siteMapperMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        dummyEmail = Email(EntityID(UUID.fromString("3560340f-3b40-45d4-8f34-44390a405872"), Emails))
        dummySite = Site(EntityID(UUID.fromString("d9741b0d-d9c5-4609-8234-2555e7d6c749"), Sites))
        dummyUser = User(EntityID(UUID.fromString("fd0265c1-8dc7-4ad9-be00-e0616d6e107e"), Users))
        dummySiteId = UUID.fromString("3fd37556-8aeb-4174-aa48-707e134b8a2c")
        dummySiteServerDto = SiteServerDto("coolWeb")
        dummySiteClientDto = SiteClientDto("de7da9c3-bc14-4d29-aa6d-15fb2122e072")
        dummyFullSiteServerDto = FullSiteServerDto("site1")
    }

    @Nested
    inner class Create {

        @Test
        fun `create site when email exists and site doesn't exist yet`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns null
            every { siteRepositoryMock.createAndGetId(dummySiteServerDto.siteName, dummyEmail) } returns dummySiteId
            every { siteRepositoryMock.getById(dummySiteId) } returns dummySite
            with(siteMapperMock) {
                every { dummySite.toSiteClientDto() } returns dummySiteClientDto
            }
            mockTransaction()

            val siteService =
                SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            val siteResult = siteService.create(dummySiteServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
                siteRepositoryMock.createAndGetId(dummySiteServerDto.siteName, dummyEmail)
                siteRepositoryMock.getById(dummySiteId)
                with(siteMapperMock) {
                    dummySite.toSiteClientDto()
                }
            }
            assertEquals(dummySiteClientDto, siteResult)
        }

        @Test
        fun `create site when email exists and site already exists`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns dummySite
            mockTransaction()

            val siteService =
                SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            assertThrows<DataConflictException> {
                siteService.create(dummySiteServerDto, dummySessionId)
            }

            verify(exactly = 0) {
                siteRepositoryMock.createAndGetId(dummySiteServerDto.siteName, dummyEmail)
                siteRepositoryMock.getById(any())
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
            }
        }

        @Test
        fun `create site when email doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns null
            mockTransaction()

            val siteService =
                SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            assertThrows<Exception> { siteService.create(dummySiteServerDto, dummySessionId) }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
            }
            verify {
                siteRepositoryMock wasNot Called
            }
        }

        @Test
        fun `create site when last user doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns null
            mockTransaction()

            val siteService =
                SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            assertThrows<Exception> { siteService.create(dummySiteServerDto, dummySessionId) }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
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
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns dummySite
            with(siteMapperMock) {
                every { dummySite.toSiteClientDto() } returns dummySiteClientDto
            }
            mockTransaction()

            val siteService =
                SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            val siteResult = siteService.find(dummySiteServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
                with(siteMapperMock) {
                    dummySite.toSiteClientDto()
                }
            }
            assertEquals(dummySiteClientDto, siteResult)
        }

        @Test
        fun `find site when email exists but site doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns null
            mockTransaction()

            val siteService =
                SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            assertThrows<DataNotFoundException> {
                siteService.find(dummySiteServerDto, dummySessionId)
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
            }
        }

        @Test
        fun `find site when email doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns null
            mockTransaction()

            val siteService =
                SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            assertThrows<Exception> { siteService.find(dummySiteServerDto, dummySessionId) }
            verify {
                siteRepositoryMock wasNot Called
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
            }
        }

        @Test
        fun `find site when last user doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns null
            mockTransaction()

            val siteService =
                SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            assertThrows<Exception> { siteService.find(dummySiteServerDto, dummySessionId) }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
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
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns dummySite
            every { siteRepositoryMock.delete(dummySite) } just Runs
            with(siteMapperMock) {
                every { dummySite.toSiteClientDto() } returns dummySiteClientDto
            }
            mockTransaction()

            val siteService =
                SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            val result = siteService.delete(dummySiteServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
                with(siteMapperMock) {
                    dummySite.toSiteClientDto()
                }
                siteRepositoryMock.delete(dummySite)
            }
            assertEquals(dummySiteClientDto, result)
        }

        @Test
        fun `delete site when email exists but site doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns dummyEmail
            every { siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail) } returns null
            mockTransaction()

            val siteService =
                SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            assertThrows<DataNotFoundException> {
                siteService.delete(dummySiteServerDto, dummySessionId)
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
                siteRepositoryMock.getByNameAndEmail(dummySiteServerDto.siteName, dummyEmail)
            }
            verify(exactly = 0) {
                siteRepositoryMock.delete(any())
            }
        }

        @Test
        fun `delete site when email doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every { userRepositoryMock.getLastEmail(dummyUser) } returns null
            mockTransaction()

            val siteService =
                SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            assertThrows<Exception> { siteService.delete(dummySiteServerDto, dummySessionId) }
            verify {
                siteRepositoryMock wasNot Called
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                userRepositoryMock.getLastEmail(dummyUser)
            }
        }

        @Test
        fun `delete site when last user doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns null
            mockTransaction()

            val siteService =
                SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            assertThrows<Exception> { siteService.delete(dummySiteServerDto, dummySessionId) }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
            }
            verify {
                siteRepositoryMock wasNot Called
                userRepositoryMock wasNot Called
            }
        }

    }

    @Nested
    inner class CreateFullSite {

        @Test
        fun `create when it already exists`() {
            every { siteRepositoryMock.getByNameAndEmail(dummyFullSiteServerDto.siteName, dummyEmail.id.value) } returns dummySite
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)
            assertThrows<DataConflictException> {
                siteService.createFullSite(dummyFullSiteServerDto, dummyEmail.id.value)
            }

            verifyOrder {
                siteRepositoryMock.getByNameAndEmail(dummyFullSiteServerDto.siteName, dummyEmail.id.value)
            }
        }

        @Test
        fun `create when it doesn't exist`() {
            every { siteRepositoryMock.getByNameAndEmail(dummyFullSiteServerDto.siteName, dummyEmail.id.value) } returns null
            every { siteRepositoryMock.createAndGetId(dummyFullSiteServerDto.siteName, dummyEmail.id.value) } returns dummySiteId
            mockTransaction()

            val siteService = SiteServiceImpl(siteRepositoryMock, userRepositoryMock, sessionRepositoryMock, siteMapperMock)

            val result = siteService.createFullSite(dummyFullSiteServerDto, dummyEmail.id.value)

            assertEquals(dummyFullSiteServerDto.siteName, result.siteName)
            assertEquals(dummySiteId.toString(), result.id)
            verifyOrder {
                siteRepositoryMock.getByNameAndEmail(dummyFullSiteServerDto.siteName, dummyEmail.id.value)
            }
        }

    }

}
