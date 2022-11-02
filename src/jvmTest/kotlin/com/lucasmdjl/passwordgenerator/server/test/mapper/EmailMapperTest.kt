package com.lucasmdjl.passwordgenerator.server.test.mapper

import com.lucasmdjl.passwordgenerator.common.dto.client.EmailClientDto
import com.lucasmdjl.passwordgenerator.server.mapper.impl.EmailMapperImpl
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.Site
import io.mockk.*
import org.jetbrains.exposed.dao.load
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

    @BeforeAll
    override fun initMocks() {
        emailMock = mockk()
        siteListMock = listOf(mockk(), mockk())
    }

    @BeforeEach
    override fun initDummies() {
        dummyEmailId = "email123"
        dummySiteNameList = listOf("site1", "site2")
        dummySiteIdsList = mutableListOf("id1", "id2")
        dummyEmailClientDto = EmailClientDto(dummyEmailId, listOf())
    }

    @Nested
    inner class EmailToEmailClientDto {

        @Test
        fun `with no sites`() {
            mockTransaction()
            mockkStatic("org.jetbrains.exposed.dao.ReferencesKt")
            every { emailMock.load(any()) } returns emailMock
            every { emailMock.id.value.toString() } returns dummyEmailId
            every { emailMock.sites } returns emptySized()
            val emailMapper = EmailMapperImpl()
            val emailDto = emailMapper.emailToEmailClientDto(emailMock)
            assertEquals(dummyEmailId, emailDto.id)
            assertTrue(emailDto.siteIdList.isEmpty())
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                emailMock.load(Email::sites)
            }
        }

        @Test
        fun `with sites`() {
            mockTransaction()
            mockkStatic("org.jetbrains.exposed.dao.ReferencesKt")
            every { emailMock.load(any()) } returns emailMock
            every { emailMock.id.value.toString() } returns dummyEmailId
            every { emailMock.sites } returns SizedCollection(siteListMock)
            siteListMock.forEachIndexed { index, siteMock ->
                every { siteMock.id.value.toString() } returns dummySiteIdsList[index]
            }
            val emailMapper = EmailMapperImpl()
            val emailDto = emailMapper.emailToEmailClientDto(emailMock)
            assertEquals(dummyEmailId, emailDto.id)
            assertEquals(dummySiteIdsList, emailDto.siteIdList)
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                emailMock.load(Email::sites)
            }
        }

        @Test
        fun `with receiver`() {
            val emailMapper = EmailMapperImpl()
            val emailMapperSpy = spyk(emailMapper)
            every { emailMapperSpy.emailToEmailClientDto(emailMock) } returns dummyEmailClientDto
            with(emailMapperSpy) {
                emailMock.toEmailClientDto()
            }
            verifySequence {
                with(emailMapperSpy) {
                    emailMock.toEmailClientDto()
                }
                emailMapperSpy.emailToEmailClientDto(emailMock)
            }
        }

    }

}
