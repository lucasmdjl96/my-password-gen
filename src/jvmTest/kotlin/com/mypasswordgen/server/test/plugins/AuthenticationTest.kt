/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.plugins

import com.mypasswordgen.server.controller.impl.SessionControllerImpl
import com.mypasswordgen.server.dto.SessionDto
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class AuthenticationTest : AuthenticationTestParent() {

    private lateinit var dummySessionDto: SessionDto

    @BeforeAll
    override fun initMocks() {
        mockkConstructor(SessionControllerImpl::class)
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionDto = SessionDto(UUID.fromString("cc199960-4a8b-48f8-8272-0ae265b459ad"))
    }

    @Test
    fun `access with good cookie`() = testApplication {
        every { anyConstructed<SessionControllerImpl>().validate(any(), any()) } returns dummySessionDto
        val response = createAndConfigureClientWithCookie().get("/test-authentication")
        assertEquals(HttpStatusCode.OK, response.status)
        verify { anyConstructed<SessionControllerImpl>().validate(any(), any()) }
    }

    @Test
    fun `access with bad cookie`() = testApplication {
        every { anyConstructed<SessionControllerImpl>().validate(any(), any()) } returns null
        coEvery { anyConstructed<SessionControllerImpl>().challenge(any(), any()) } just Runs
        createAndConfigureClientWithCookie().get("/test-authentication")
        coVerifyOrder {
            anyConstructed<SessionControllerImpl>().validate(any(), any())
            anyConstructed<SessionControllerImpl>().challenge(any(), any())
        }
    }

    @Test
    fun `access without cookie`() = testApplication {
        coEvery { anyConstructed<SessionControllerImpl>().challenge(any(), any()) } just Runs
        createAndConfigureClientWithoutCookie().get("/test-authentication")
        verify(exactly = 0) {
            anyConstructed<SessionControllerImpl>().validate(any(), any())
        }
        coVerify {
            anyConstructed<SessionControllerImpl>().challenge(any(), any())
        }
    }


}
