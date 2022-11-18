package com.mypasswordgen.server.test.controller

import com.mypasswordgen.common.routes.SessionRoute
import com.mypasswordgen.server.controller.impl.SessionControllerImpl
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.plugins.NotAuthenticatedException
import com.mypasswordgen.server.service.SessionService
import com.mypasswordgen.server.tables.Sessions
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.id.EntityID
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SessionControllerTest : ControllerTestParent() {

    private lateinit var sessionServiceMock: SessionService
    private lateinit var callMock: ApplicationCall

    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummySession: Session

    @BeforeAll
    override fun initMocks() {
        sessionServiceMock = mockk()
        callMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionDto = SessionDto(UUID.fromString("ddc94894-ac04-4730-9951-b141b30ed430"))
        dummySession = Session(EntityID(UUID.fromString("4481df83-b8b6-49e8-81db-ea0db3db2620"), Sessions))
    }

    @Nested
    inner class Update {

        @Test
        fun `update existing session`() = runBlocking {
            mockCall(callMock, dummySessionDto)
            every { callMock.sessions.set(any(), any()) } just Runs
            every { sessionServiceMock.assignNew(dummySessionDto) } returns dummySessionDto

            val sessionController = SessionControllerImpl(sessionServiceMock)

            sessionController.put(callMock, SessionRoute.Update())

            coVerify {
                callMock.sessions.get<SessionDto>()
                sessionServiceMock.assignNew(dummySessionDto)
                callMock.respond(HttpStatusCode.OK)
            }
        }

        @Test
        fun `update no session`() = runBlocking {
            mockCall(callMock, dummySessionDto)
            every { callMock.sessions.get(any()) } returns null
            every { callMock.sessions.set(any(), any()) } just Runs
            every { sessionServiceMock.assignNew(null) } returns dummySessionDto

            val sessionController = SessionControllerImpl(sessionServiceMock)

            sessionController.put(callMock, SessionRoute.Update())

            coVerify {
                callMock.sessions.get<SessionDto>()
                sessionServiceMock.assignNew(null)
                callMock.respond(HttpStatusCode.OK)
            }
        }

    }

    @Nested
    inner class Validate {

        @Test
        fun `validate with existing session`() {
            every { sessionServiceMock.find(dummySessionDto) } returns dummySession

            val sessionController = SessionControllerImpl(sessionServiceMock)

            val result = sessionController.validate(callMock, dummySessionDto)

            verify {
                sessionServiceMock.find(dummySessionDto)
            }
            assertEquals(dummySessionDto, result)
        }

        @Test
        fun `validate with non-existing session`() {
            every { sessionServiceMock.find(dummySessionDto) } returns null

            val sessionController = SessionControllerImpl(sessionServiceMock)

            val result = sessionController.validate(callMock, dummySessionDto)

            verify {
                sessionServiceMock.find(dummySessionDto)
            }
            assertNull(result)
        }

    }

    @Nested
    inner class Challenge {

        @Test
        fun challenge() {
            runBlocking {
                mockCall(callMock, dummySessionDto)

                val sessionController = SessionControllerImpl(sessionServiceMock)

                assertThrows<NotAuthenticatedException> {
                    sessionController.challenge(callMock, dummySessionDto)
                }
            }
        }

    }


}
