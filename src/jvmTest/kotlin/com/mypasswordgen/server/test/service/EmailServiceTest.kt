/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.service

import com.mypasswordgen.common.dto.client.EmailClientDto
import com.mypasswordgen.common.dto.fullServer.FullEmailServerDto
import com.mypasswordgen.common.dto.fullServer.FullSiteServerDto
import com.mypasswordgen.common.dto.idb.SiteIDBDto
import com.mypasswordgen.common.dto.server.EmailServerDto
import com.mypasswordgen.server.mapper.EmailMapper
import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.repository.EmailRepository
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.SiteService
import com.mypasswordgen.server.service.impl.EmailServiceImpl
import com.mypasswordgen.server.tables.Emails
import com.mypasswordgen.server.tables.Users
import io.mockk.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals

class EmailServiceTest : ServiceTestParent() {

    private lateinit var siteServiceMock: SiteService
    private lateinit var emailRepositoryMock: EmailRepository
    private lateinit var userRepositoryMock: UserRepository
    private lateinit var sessionRepositoryMock: SessionRepository
    private lateinit var emailMapperMock: EmailMapper

    private lateinit var dummySessionId: UUID
    private lateinit var dummyEmailServerDto: EmailServerDto
    private lateinit var dummyUser: User
    private lateinit var dummyEmailId: UUID
    private lateinit var dummyEmail: Email
    private lateinit var dummyEmailClientDto: EmailClientDto
    private lateinit var dummyFullEmailServerDto: FullEmailServerDto
    private lateinit var dummySiteIDBDtoList: List<SiteIDBDto>

    @BeforeAll
    override fun initMocks() {
        siteServiceMock = mockk()
        emailRepositoryMock = mockk()
        userRepositoryMock = mockk()
        sessionRepositoryMock = mockk()
        emailMapperMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        dummyEmailServerDto = EmailServerDto("email@gmail.com")
        dummyUser = User(EntityID(UUID.fromString("c26ffe47-d0dd-402c-a692-16552cb039ec"), Users))
        dummyEmailId = UUID.fromString("b4145017-ce98-439b-91c8-4a6c05ecbcc9")
        dummyEmail = Email(EntityID(UUID.fromString("7e91ff67-3de2-47bb-970d-9b76ea4c7883"), Emails))
        dummyEmailClientDto = EmailClientDto(
            "1c749bba-b9c5-43c7-8a2b-d6656b9e54b6",
            listOf("61a403d1-dead-4404-8597-093e3ea94ebf", "525c3b27-efc1-4d1a-a3bf-27c6f6d2f1ea")
        )
        dummyFullEmailServerDto = FullEmailServerDto(
            "email1", mutableListOf(
                FullSiteServerDto("site1"), FullSiteServerDto("site2")
            )
        )
        dummySiteIDBDtoList = listOf(SiteIDBDto("id1", "site1x"), SiteIDBDto("id2", "site2x"))
    }

    @Nested
    inner class Create {

        @Test
        fun `create email when user exists and email doesn't exist yet`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every {
                emailRepositoryMock.getByAddressAndUser(
                    dummyEmailServerDto.emailAddress, dummyUser
                )
            } returns null
            every {
                emailRepositoryMock.createAndGetId(
                    dummyEmailServerDto.emailAddress, dummyUser
                )
            } returns dummyEmailId
            every { emailRepositoryMock.getById(dummyEmailId) } returns dummyEmail
            every { userRepositoryMock.setLastEmail(dummyUser, dummyEmail) } just Runs
            with(emailMapperMock) {
                every { dummyEmail.toEmailClientDto() } returns dummyEmailClientDto
            }
            mockTransaction()

            val emailService = EmailServiceImpl(
                siteServiceMock,
                emailRepositoryMock,
                userRepositoryMock,
                sessionRepositoryMock,
                emailMapperMock
            )

            val emailResult = emailService.create(dummyEmailServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                emailRepositoryMock.getByAddressAndUser(dummyEmailServerDto.emailAddress, dummyUser)
                emailRepositoryMock.createAndGetId(dummyEmailServerDto.emailAddress, dummyUser)
                emailRepositoryMock.getById(dummyEmailId)
                userRepositoryMock.setLastEmail(dummyUser, dummyEmail)
                with(emailMapperMock) {
                    dummyEmail.toEmailClientDto()
                }
            }

            assertEquals(dummyEmailClientDto, emailResult)
        }

        @Test
        fun `create email when user exists and email already exists`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every {
                emailRepositoryMock.getByAddressAndUser(
                    dummyEmailServerDto.emailAddress, dummyUser
                )
            } returns dummyEmail
            mockTransaction()

            val emailService = EmailServiceImpl(
                siteServiceMock,
                emailRepositoryMock,
                userRepositoryMock,
                sessionRepositoryMock,
                emailMapperMock
            )

            assertThrows<DataConflictException> {
                emailService.create(dummyEmailServerDto, dummySessionId)
            }

            verify(exactly = 0) {
                emailRepositoryMock.createAndGetId(dummyEmailServerDto.emailAddress, dummyUser)
                emailRepositoryMock.getById(any())
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                emailRepositoryMock.getByAddressAndUser(dummyEmailServerDto.emailAddress, dummyUser)
            }
            verify {
                userRepositoryMock wasNot Called
            }
        }

        @Test
        fun `create email when user doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns null
            mockTransaction()

            val emailService = EmailServiceImpl(
                siteServiceMock,
                emailRepositoryMock,
                userRepositoryMock,
                sessionRepositoryMock,
                emailMapperMock
            )

            assertThrows<Exception> { emailService.create(dummyEmailServerDto, dummySessionId) }
            verify {
                emailRepositoryMock wasNot Called
                userRepositoryMock wasNot Called
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
            }
        }

    }

    @Nested
    inner class Find {

        @Test
        fun `find email when user and email exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every {
                emailRepositoryMock.getByAddressAndUser(
                    dummyEmailServerDto.emailAddress, dummyUser
                )
            } returns dummyEmail
            every { userRepositoryMock.setLastEmail(dummyUser, dummyEmail) } just Runs
            with(emailMapperMock) {
                every { dummyEmail.toEmailClientDto() } returns dummyEmailClientDto
            }
            mockTransaction()

            val emailService = EmailServiceImpl(
                siteServiceMock,
                emailRepositoryMock,
                userRepositoryMock,
                sessionRepositoryMock,
                emailMapperMock
            )

            val emailResult = emailService.find(dummyEmailServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                emailRepositoryMock.getByAddressAndUser(dummyEmailServerDto.emailAddress, dummyUser)
                userRepositoryMock.setLastEmail(dummyUser, dummyEmail)
                with(emailMapperMock) {
                    dummyEmail.toEmailClientDto()
                }
            }
            assertEquals(dummyEmailClientDto, emailResult)
        }

        @Test
        fun `find email when user exists but email doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every {
                emailRepositoryMock.getByAddressAndUser(
                    dummyEmailServerDto.emailAddress, dummyUser
                )
            } returns null
            mockTransaction()

            val emailService = EmailServiceImpl(
                siteServiceMock,
                emailRepositoryMock,
                userRepositoryMock,
                sessionRepositoryMock,
                emailMapperMock
            )

            assertThrows<DataNotFoundException> {
                emailService.find(dummyEmailServerDto, dummySessionId)
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                emailRepositoryMock.getByAddressAndUser(dummyEmailServerDto.emailAddress, dummyUser)
            }
            verify {
                userRepositoryMock wasNot Called
            }
        }

        @Test
        fun `find email when user doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns null
            mockTransaction()

            val emailService = EmailServiceImpl(
                siteServiceMock,
                emailRepositoryMock,
                userRepositoryMock,
                sessionRepositoryMock,
                emailMapperMock
            )

            assertThrows<Exception> { emailService.find(dummyEmailServerDto, dummySessionId) }
            verify {
                emailRepositoryMock wasNot Called
                userRepositoryMock wasNot Called
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
            }
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete email when user and email exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every {
                emailRepositoryMock.getByAddressAndUser(
                    dummyEmailServerDto.emailAddress, dummyUser
                )
            } returns dummyEmail
            every { emailRepositoryMock.delete(dummyEmail) } just Runs
            every { userRepositoryMock.setLastEmail(dummyUser, dummyEmail) } just Runs
            with(emailMapperMock) {
                every { dummyEmail.toEmailClientDto() } returns dummyEmailClientDto
            }
            mockTransaction()

            val emailService = EmailServiceImpl(
                siteServiceMock,
                emailRepositoryMock,
                userRepositoryMock,
                sessionRepositoryMock,
                emailMapperMock
            )

            val result = emailService.delete(dummyEmailServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                emailRepositoryMock.getByAddressAndUser(dummyEmailServerDto.emailAddress, dummyUser)
                with(emailMapperMock) {
                    dummyEmail.toEmailClientDto()
                }
                emailRepositoryMock.delete(dummyEmail)
            }
            assertEquals(dummyEmailClientDto, result)
        }

        @Test
        fun `delete email when user exists but email doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns dummyUser
            every {
                emailRepositoryMock.getByAddressAndUser(
                    dummyEmailServerDto.emailAddress, dummyUser
                )
            } returns null
            mockTransaction()

            val emailService = EmailServiceImpl(
                siteServiceMock,
                emailRepositoryMock,
                userRepositoryMock,
                sessionRepositoryMock,
                emailMapperMock
            )

            assertThrows<DataNotFoundException> {
                emailService.delete(dummyEmailServerDto, dummySessionId)
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                emailRepositoryMock.getByAddressAndUser(dummyEmailServerDto.emailAddress, dummyUser)
            }
            verify(exactly = 0) {
                emailRepositoryMock.delete(any())
            }
            verify {
                userRepositoryMock wasNot Called
            }
        }

        @Test
        fun `delete email when user doesn't exist`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns null
            mockTransaction()

            val emailService = EmailServiceImpl(
                siteServiceMock,
                emailRepositoryMock,
                userRepositoryMock,
                sessionRepositoryMock,
                emailMapperMock
            )

            assertThrows<Exception> { emailService.delete(dummyEmailServerDto, dummySessionId) }
            verify {
                emailRepositoryMock wasNot Called
                userRepositoryMock wasNot Called
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
            }
        }

    }

    @Nested
    inner class CreateFullEmail {

        @Test
        fun `create when it already exists`() {
            every {
                emailRepositoryMock.getByAddressAndUser(
                    dummyFullEmailServerDto.emailAddress, dummyEmail.id.value
                )
            } returns dummyEmail
            mockTransaction()

            val emailService = EmailServiceImpl(
                siteServiceMock,
                emailRepositoryMock,
                userRepositoryMock,
                sessionRepositoryMock,
                emailMapperMock
            )
            assertThrows<DataConflictException> {
                emailService.createFullEmail(dummyFullEmailServerDto, dummyEmail.id.value)
            }

            verifyOrder {
                emailRepositoryMock.getByAddressAndUser(dummyFullEmailServerDto.emailAddress, dummyEmail.id.value)
            }
        }

        @Test
        fun `create when a site already exists`() {
            every {
                emailRepositoryMock.getByAddressAndUser(
                    dummyFullEmailServerDto.emailAddress, dummyUser.id.value
                )
            } returns null
            every {
                emailRepositoryMock.createAndGetId(
                    dummyFullEmailServerDto.emailAddress, dummyUser.id.value
                )
            } returns dummyEmailId
            every {
                siteServiceMock.createFullSite(
                    dummyFullEmailServerDto.sites[0], dummyEmailId
                )
            } throws DataConflictException()
            mockTransaction()

            val emailService = EmailServiceImpl(
                siteServiceMock,
                emailRepositoryMock,
                userRepositoryMock,
                sessionRepositoryMock,
                emailMapperMock
            )
            assertThrows<DataConflictException> {
                emailService.createFullEmail(dummyFullEmailServerDto, dummyUser.id.value)
            }

            verifyOrder {
                emailRepositoryMock.getByAddressAndUser(dummyFullEmailServerDto.emailAddress, dummyUser.id.value)
                emailRepositoryMock.createAndGetId(dummyFullEmailServerDto.emailAddress, dummyUser.id.value)
                siteServiceMock.createFullSite(dummyFullEmailServerDto.sites[0], dummyEmailId)
            }
        }

        @Test
        fun `create when nothing already exists`() {
            every {
                emailRepositoryMock.getByAddressAndUser(
                    dummyFullEmailServerDto.emailAddress, dummyUser.id.value
                )
            } returns null
            every {
                emailRepositoryMock.createAndGetId(
                    dummyFullEmailServerDto.emailAddress, dummyUser.id.value
                )
            } returns dummyEmailId
            dummyFullEmailServerDto.sites.forEachIndexed { index, site ->
                every {
                    siteServiceMock.createFullSite(
                        site, dummyEmailId
                    )
                } returns dummySiteIDBDtoList[index]
            }
            mockTransaction()

            val emailService = EmailServiceImpl(
                siteServiceMock,
                emailRepositoryMock,
                userRepositoryMock,
                sessionRepositoryMock,
                emailMapperMock
            )
            val result = emailService.createFullEmail(dummyFullEmailServerDto, dummyUser.id.value)

            assertEquals(dummyFullEmailServerDto.emailAddress, result.emailAddress)
            assertEquals(dummyEmailId.toString(), result.id)
            assertEquals(dummySiteIDBDtoList, result.sites)
            verifyOrder {
                emailRepositoryMock.getByAddressAndUser(dummyFullEmailServerDto.emailAddress, dummyUser.id.value)
                emailRepositoryMock.createAndGetId(dummyFullEmailServerDto.emailAddress, dummyUser.id.value)
                for (site in dummyFullEmailServerDto.sites) {
                    siteServiceMock.createFullSite(site, dummyEmailId)
                }
            }
        }

    }

}
