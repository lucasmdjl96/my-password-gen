package com.lucasmdjl.passwordgenerator.server.test.repository

import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.repository.impl.SessionRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.tables.Sessions
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SessionRepositoryTest : RepositoryTestParent() {

    @Nested
    inner class Create {

        @Test
        fun `create a new session`() {
            val sessionRepository = SessionRepositoryImpl()
            testTransaction {
                val sessionsBefore = Sessions.selectAll()
                val beforeCount = sessionsBefore.count()
                val existingIds = sessionsBefore.map { it[Sessions.id].value.toString() }
                val session = sessionRepository.create()
                val afterCount = Sessions.selectAll().count()
                assertEquals(beforeCount + 1, afterCount)
                assertNotNull(session)
                assertNull(session.lastUser)
                assert(session.id.value.toString() !in existingIds)
            }
        }

    }

    @Nested
    inner class GetById {

        @Test
        fun `get by id when it exists`() {
            val sessionRepository = SessionRepositoryImpl()
            testTransaction {
                val session = sessionRepository.getById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))
                assertNotNull(session)
                assertEquals("757f2ad6-aa06-4403-aea3-d5e6cb9f0001", session.id.value.toString())
            }
        }

        @Test
        fun `get by id when it doesn't exist`() {
            val sessionRepository = SessionRepositoryImpl()
            testTransaction {
                val session = sessionRepository.getById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f2001"))
                assertNull(session)
            }
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete session`() {
            val sessionRepository = SessionRepositoryImpl()
            testTransaction {
                val before = Sessions.selectAll().count()
                val session = Session.findById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))!!
                sessionRepository.delete(session)
                val after = Sessions.selectAll().count()
                assertEquals(before - 1, after)
                assertNull(Session.findById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001")))
            }
        }

    }

    @Nested
    inner class SetLastUser {

        @Test
        fun `set last user from null to not null`() {
            val sessionRepository = SessionRepositoryImpl()
            testTransaction {
                val session = Session.findById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))!!
                val user = User.findById(2)!!
                sessionRepository.setLastUser(session, user)
                val newSession = Session.findById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))
                assertNotNull(newSession)
                assertNotNull(newSession.lastUser)
                assertEquals(user.id.value, newSession.lastUser!!.id.value)
            }
        }

        @Test
        fun `set last user from not null to null`() {
            val sessionRepository = SessionRepositoryImpl()
            testTransaction {
                val session = Session.findById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))!!
                sessionRepository.setLastUser(session, null)
                val newSession = Session.findById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                assertNotNull(newSession)
                assertNull(newSession.lastUser)
            }
        }

        @Test
        fun `set last user from not null to not null`() {
            val sessionRepository = SessionRepositoryImpl()
            testTransaction {
                val session = Session.findById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))!!
                val user = User.findById(5)!!
                sessionRepository.setLastUser(session, user)
                val newSession = Session.findById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                assertNotNull(newSession)
                assertNotNull(newSession.lastUser)
                assertEquals(user.id.value, newSession.lastUser!!.id.value)
            }
        }

        @Test
        fun `set last user from null to null`() {
            val sessionRepository = SessionRepositoryImpl()
            testTransaction {
                val session = Session.findById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))!!
                sessionRepository.setLastUser(session, null)
                val newSession = Session.findById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))
                assertNotNull(newSession)
                assertNull(newSession.lastUser)
            }
        }

    }

    @Nested
    inner class GetLastUser {

        @Test
        fun `get last user when null`() {
            val sessionRepository = SessionRepositoryImpl()
            testTransaction {
                val session = Session.findById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))!!
                val user = sessionRepository.getLastUser(session)
                assertNull(user)
            }
        }

        @Test
        fun `get last user when not null`() {
            val sessionRepository = SessionRepositoryImpl()
            testTransaction {
                val session = Session.findById(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))!!
                val user = sessionRepository.getLastUser(session)
                assertNotNull(user)
                assertEquals(6, user.id.value)
            }
        }

    }

}
