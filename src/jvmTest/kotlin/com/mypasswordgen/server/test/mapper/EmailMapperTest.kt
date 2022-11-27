/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.mapper

import com.mypasswordgen.common.dto.client.EmailClientDto
import com.mypasswordgen.common.dto.fullClient.FullEmailClientDto
import com.mypasswordgen.common.dto.fullClient.FullSiteClientDto
import com.mypasswordgen.server.mapper.SiteMapper
import com.mypasswordgen.server.mapper.impl.EmailMapperImpl
import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.Site
import io.mockk.*
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.emptySized
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmailMapperTest : MapperTestParent() {

    private lateinit var emailMock: Email
    private lateinit var dummyEmailId: String
    private lateinit var siteListMock: List<Site>
    private lateinit var dummySiteNameList: List<String>
    private lateinit var dummySiteIdsList: MutableList<String>
    private lateinit var dummyEmailClientDto: EmailClientDto
    private lateinit var siteMapperMock: SiteMapper
    private lateinit var dummyFullSiteClientList: List<FullSiteClientDto>
    private lateinit var dummyFullEmailClientDto: FullEmailClientDto

    @BeforeAll
    override fun initMocks() {
        emailMock = mockk()
        siteListMock = listOf(mockk(), mockk())
        siteMapperMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummyEmailId = "email123"
        dummySiteNameList = listOf("site1", "site2")
        dummySiteIdsList = mutableListOf("id1", "id2")
        dummyEmailClientDto = EmailClientDto(dummyEmailId, setOf())
        dummyFullSiteClientList = listOf(FullSiteClientDto("site1"), FullSiteClientDto("site2"))
        dummyFullEmailClientDto = FullEmailClientDto("email1")
    }

    @Nested
    inner class EmailToEmailClientDto {

        @Test
        fun `with no sites`() {
            mockTransaction()
            every { emailMock.id.value.toString() } returns dummyEmailId
            every { emailMock.sites } returns emptySized()
            val emailMapper = EmailMapperImpl(siteMapperMock)
            val emailDto = emailMapper.emailToEmailClientDto(emailMock)
            assertEquals(dummyEmailId, emailDto.id)
            assertTrue(emailDto.siteIdSet.isEmpty())
            verify {
                transaction(statement = any<Transaction.() -> Any>())
            }
        }

        @Test
        fun `with sites`() {
            mockTransaction()
            every { emailMock.id.value.toString() } returns dummyEmailId
            every { emailMock.sites } returns SizedCollection(siteListMock)
            siteListMock.forEachIndexed { index, siteMock ->
                every { siteMock.id.value.toString() } returns dummySiteIdsList[index]
            }
            val emailMapper = EmailMapperImpl(siteMapperMock)
            val emailDto = emailMapper.emailToEmailClientDto(emailMock)
            assertEquals(dummyEmailId, emailDto.id)
            assertEquals(dummySiteIdsList.toSet(), emailDto.siteIdSet)
            verify {
                transaction(statement = any<Transaction.() -> Any>())
            }
        }

        @Test
        fun `with receiver`() {
            val emailMapper = EmailMapperImpl(siteMapperMock)
            val emailMapperSpy = spyk(emailMapper)
            every { emailMapperSpy.emailToEmailClientDto(emailMock) } returns dummyEmailClientDto
            val result = with(emailMapperSpy) {
                emailMock.toEmailClientDto()
            }
            assertEquals(dummyEmailClientDto, result)
            verifySequence {
                with(emailMapperSpy) {
                    emailMock.toEmailClientDto()
                }
                emailMapperSpy.emailToEmailClientDto(emailMock)
            }
        }

    }

    @Nested
    inner class EmailToFullEmailClientDto {
        @Test
        fun `with argument`() {
            mockTransaction()
            every { emailMock.id.value.toString() } returns dummyEmailId
            every { emailMock.sites } returns SizedCollection(siteListMock)
            siteListMock.forEachIndexed { index, email ->
                every {
                    with(siteMapperMock) {
                        email.toFullSiteClientDto()
                    }
                } returns dummyFullSiteClientList[index]
            }

            val emailMapper = EmailMapperImpl(siteMapperMock)

            val result = emailMapper.emailToFullEmailClientDto(emailMock)

            assertEquals(dummyEmailId, result.id)
            assertEquals(2, result.sites.size)
            val siteList = result.sites.toList()
            for (i in 0..1) {
                assertEquals(dummyFullSiteClientList[i], siteList[i])
            }
            verifyOrder {
                emailMock.sites
                for (user in siteListMock) {
                    with(siteMapperMock) {
                        user.toFullSiteClientDto()
                    }
                }
            }
        }

        @Test
        fun `with receiver`() {
            val emailMapper = EmailMapperImpl(siteMapperMock)
            val sessionMapperSpy = spyk(emailMapper)
            every { sessionMapperSpy.emailToFullEmailClientDto(emailMock) } returns dummyFullEmailClientDto
            val result = with(sessionMapperSpy) {
                emailMock.toFullEmailClientDto()
            }

            assertEquals(dummyFullEmailClientDto, result)
            verifySequence {
                with(sessionMapperSpy) {
                    emailMock.toFullEmailClientDto()
                }
                sessionMapperSpy.emailToFullEmailClientDto(emailMock)
            }
        }
    }

}
