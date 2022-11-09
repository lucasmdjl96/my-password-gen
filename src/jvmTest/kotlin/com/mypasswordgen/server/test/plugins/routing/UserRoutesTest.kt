package com.mypasswordgen.server.test.plugins.routing

import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.common.routes.UserRoute
import com.mypasswordgen.server.controller.impl.SessionControllerImpl
import com.mypasswordgen.server.controller.impl.UserControllerImpl
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

class UserRoutesTest : RoutesTestParent() {

    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummyUserServerDto: UserServerDto

    @BeforeAll
    override fun initMocks() {
        mockkConstructor(UserControllerImpl::class)
        mockkConstructor(SessionControllerImpl::class)
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionDto = SessionDto(UUID.fromString("7639a835-b7a4-4d57-ad28-d23e6bbd1d97"))
        dummyUserServerDto = UserServerDto("user123")
    }

    @Nested
    inner class Login {

        @Test
        fun `get old email route`() = testApplication {
            every { anyConstructed<SessionControllerImpl>().validate(any(), any()) } returns dummySessionDto
            coEvery { anyConstructed<UserControllerImpl>().post(any(), any<UserRoute.Login>()) } just Runs
            createAndConfigureClient().post(UserRoute.Login()) {
                contentType(ContentType.Application.Json)
                setBody(dummyUserServerDto)
            }
            coVerifyOrder {
                anyConstructed<SessionControllerImpl>().validate(any(), any())
                anyConstructed<UserControllerImpl>().post(any(), any<UserRoute.Login>())
            }
        }

    }

    @Nested
    inner class Register {

        @Test
        fun `post new email route`() = testApplication {
            every { anyConstructed<SessionControllerImpl>().validate(any(), any()) } returns dummySessionDto
            coEvery { anyConstructed<UserControllerImpl>().post(any(), any<UserRoute.Register>()) } just Runs
            createAndConfigureClient().post(UserRoute.Register()) {
                contentType(ContentType.Application.Json)
                setBody(dummyUserServerDto)
            }
            coVerifyOrder {
                anyConstructed<SessionControllerImpl>().validate(any(), any())
                anyConstructed<UserControllerImpl>().post(any(), any<UserRoute.Register>())
            }
        }

    }

    @Nested
    inner class Logout {

        @Test
        fun `post new email route`() = testApplication {
            every { anyConstructed<SessionControllerImpl>().validate(any(), any()) } returns dummySessionDto
            coEvery { anyConstructed<UserControllerImpl>().patch(any(), any<UserRoute.Logout>()) } just Runs
            createAndConfigureClient().patch(UserRoute.Logout()) {
                contentType(ContentType.Application.Json)
                setBody(dummyUserServerDto)
            }
            coVerifyOrder {
                anyConstructed<SessionControllerImpl>().validate(any(), any())
                anyConstructed<UserControllerImpl>().patch(any(), any<UserRoute.Logout>())
            }
        }

    }

}
