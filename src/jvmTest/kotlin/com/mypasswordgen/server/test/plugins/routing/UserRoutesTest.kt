/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

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
            coEvery { anyConstructed<UserControllerImpl>().get(any(), any<UserRoute.Login>()) } just Runs
            createAndConfigureClient().get(UserRoute.Login(dummyUserServerDto.username))
            coVerifyOrder {
                anyConstructed<SessionControllerImpl>().validate(any(), any())
                anyConstructed<UserControllerImpl>().get(any(), any<UserRoute.Login>())
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
