package com.mypasswordgen.server.test.plugins.routing

import com.mypasswordgen.common.dto.server.EmailServerDto
import com.mypasswordgen.common.routes.EmailRoute
import com.mypasswordgen.server.controller.impl.EmailControllerImpl
import com.mypasswordgen.server.controller.impl.SessionControllerImpl
import com.mypasswordgen.server.dto.SessionDto
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class EmailRoutesTest : RoutesTestParent() {

    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummyEmailServerDto: EmailServerDto

    @BeforeAll
    override fun initMocks() {
        mockkConstructor(EmailControllerImpl::class)
        mockkConstructor(SessionControllerImpl::class)
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionDto = SessionDto(UUID.fromString("7639a835-b7a4-4d57-ad28-d23e6bbd1d97"))
        dummyEmailServerDto = EmailServerDto("email@email.com")
    }

    @Nested
    inner class Get {

        @Test
        fun `get old email route`() = testApplication {
            every { anyConstructed<SessionControllerImpl>().validate(any(), any()) } returns dummySessionDto
            coEvery { anyConstructed<EmailControllerImpl>().get(any(), any<EmailRoute.Find>()) } just Runs
            createAndConfigureClient().get(EmailRoute.Find("email@email.com"))
            coVerifyOrder {
                anyConstructed<SessionControllerImpl>().validate(any(), any())
                anyConstructed<EmailControllerImpl>().get(any(), any<EmailRoute.Find>())
            }
        }

    }

    @Nested
    inner class Post {

        @Test
        fun `post new email route`() = testApplication {
            every { anyConstructed<SessionControllerImpl>().validate(any(), any()) } returns dummySessionDto
            coEvery { anyConstructed<EmailControllerImpl>().post(any(), any<EmailRoute.New>()) } just Runs
            createAndConfigureClient().post(EmailRoute.New()) {
                contentType(ContentType.Application.Json)
                setBody(dummyEmailServerDto)
            }
            coVerifyOrder {
                anyConstructed<SessionControllerImpl>().validate(any(), any())
                anyConstructed<EmailControllerImpl>().post(any(), any<EmailRoute.New>())
            }
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete old email route`() = testApplication {
            every { anyConstructed<SessionControllerImpl>().validate(any(), any()) } returns dummySessionDto
            coEvery { anyConstructed<EmailControllerImpl>().delete(any(), any<EmailRoute.Delete>()) } just Runs
            createAndConfigureClient().delete(EmailRoute.Delete("email@email.com"))
            coVerifyOrder {
                anyConstructed<SessionControllerImpl>().validate(any(), any())
                anyConstructed<EmailControllerImpl>().delete(any(), any<EmailRoute.Delete>())
            }
        }

    }

}
