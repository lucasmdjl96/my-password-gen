package com.mypasswordgen.server.test.mapper

import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.mapper.impl.SessionMapperImpl
import com.mypasswordgen.server.model.Session
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verifySequence
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class SessionMapperTest : MapperTestParent() {

    private lateinit var sessionMock: Session
    private lateinit var dummySessionId: UUID
    private lateinit var dummySessionDto: SessionDto

    @BeforeAll
    override fun initMocks() {
        sessionMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionId = UUID.fromString("f2e0b5b3-cc9f-4c9e-8715-df4f51a342bf")
        dummySessionDto = SessionDto(dummySessionId)
    }

    @Nested
    inner class SessionToSessionDto {

        @Test
        fun `with argument`() {
            every { sessionMock.id.value } returns dummySessionId
            val sessionMapper = SessionMapperImpl()
            val sessionDto = sessionMapper.sessionToSessionDto(sessionMock)
            assertEquals(dummySessionId, sessionDto.sessionId)
        }

        @Test
        fun `with receiver`() {
            val sessionMapper = SessionMapperImpl()
            val sessionMapperSpy = spyk(sessionMapper)
            every { sessionMapperSpy.sessionToSessionDto(sessionMock) } returns dummySessionDto
            with(sessionMapperSpy) {
                sessionMock.toSessionDto()
            }
            verifySequence {
                with(sessionMapperSpy) {
                    sessionMock.toSessionDto()
                }
                sessionMapperSpy.sessionToSessionDto(sessionMock)
            }
        }

    }


}
