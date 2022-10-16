package com.lucasmdjl.passwordgenerator.server.integrationtest

import com.lucasmdjl.passwordgenerator.common.dto.client.EmailClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.common.routes.EmailRoute
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.tables.Emails
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.and
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EmailTest : TestParent() {

    @Nested
    inner class Create {

        @Nested
        inner class Challenged {

            @Test
            fun `no cookie`() = testApplication {
                val client = createAndConfigureClientWithoutCookie()
                val response = client.post(EmailRoute.New()) {
                    setBody(EmailServerDto("email002"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `bad cookie`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f9999"))
                val response = client.post(EmailRoute.New()) {
                    setBody(EmailServerDto("email002"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

        }

        @Nested
        inner class Validated {

            @Test
            fun `no last user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))
                assertThrows<Exception> {
                    client.post(EmailRoute.New()) {
                        setBody(EmailServerDto("emailXXX"))
                        contentType(ContentType.Application.Json)
                    }
                }
            }

            @Test
            fun `non-existing email`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                val emailsBefore = testTransaction {
                    Email.find { Emails.userId eq 6 }.count()
                }
                val response = client.post(EmailRoute.New()) {
                    setBody(EmailServerDto("emailXXX"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<EmailClientDto?>()
                assertNotNull(responseBody)
                assertEquals("emailXXX", responseBody.emailAddress)
                assertEquals(mutableListOf(), responseBody.siteList)
                testTransaction {
                    assertNotEmpty(Email.find { Emails.userId eq 6 and (Emails.emailAddress eq "emailXXX") })
                    val user = User.findById(6)
                    assertNotNull(user)
                    assertNotNull(user.lastEmail)
                    assertEquals("emailXXX", user.lastEmail!!.emailAddress)
                    assertEquals(6, user.lastEmail!!.user.id.value)
                    assertEquals(emailsBefore + 1, Email.find { Emails.userId eq 6 }.count())
                }
            }

            @Test
            fun `email from other user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                val emailsBefore = testTransaction {
                    Email.find { Emails.userId eq 6 }.count()
                }
                val response = client.post(EmailRoute.New()) {
                    setBody(EmailServerDto("email009"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<EmailClientDto?>()
                assertNotNull(responseBody)
                assertEquals("email009", responseBody.emailAddress)
                assertEquals(mutableListOf(), responseBody.siteList)
                testTransaction {
                    assertNotEmpty(Email.find { Emails.userId eq 6 and (Emails.emailAddress eq "email009") })
                    val user = User.findById(6)
                    assertNotNull(user)
                    assertNotNull(user.lastEmail)
                    assertEquals("email009", user.lastEmail!!.emailAddress)
                    assertEquals(6, user.lastEmail!!.user.id.value)
                    assertEquals(emailsBefore + 1, Email.find { Emails.userId eq 6 }.count())
                }
            }

            @Test
            fun `email from last user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                val emailsBefore = testTransaction {
                    Email.find { Emails.userId eq 6 }.count()
                }
                val response = client.post(EmailRoute.New()) {
                    setBody(EmailServerDto("email003"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<EmailClientDto?>()
                assertNull(responseBody)
                testTransaction {
                    val user = User.findById(6)
                    assertNotNull(user)
                    assertNull(user.lastEmail)
                    assertEquals(emailsBefore, Email.find { Emails.userId eq 6 }.count())
                }
            }
        }

    }

    @Nested
    inner class Find {

        @Nested
        inner class Challenged {

            @Test
            fun `no cookie`() = testApplication {
                val client = createAndConfigureClientWithoutCookie()
                val response = client.get(EmailRoute.Find("email002"))
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `bad cookie`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f9999"))
                val response = client.get(EmailRoute.Find("email002"))
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

        }

        @Nested
        inner class Validated {

            @Test
            fun `no last user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))
                assertThrows<Exception> {
                    client.get(EmailRoute.Find("email002"))
                }
            }

            @Test
            fun `non-existing email`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                val response = client.get(EmailRoute.Find("emailXYZ"))
                assertEquals(HttpStatusCode.OK, response.status)
                assertNull(response.body<EmailClientDto?>())
                testTransaction {
                    val user = User.findById(6)
                    assertNotNull(user)
                    assertNull(user.lastEmail)
                }
            }

            @Test
            fun `email from other user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                val response = client.get(EmailRoute.Find("email011"))
                assertEquals(HttpStatusCode.OK, response.status)
                assertNull(response.body<EmailClientDto?>())
                testTransaction {
                    val user = User.findById(6)
                    assertNotNull(user)
                    assertNull(user.lastEmail)
                }
            }

            @Test
            fun `email from last user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                val response = client.get(EmailRoute.Find("email003"))
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<EmailClientDto?>()
                assertNotNull(responseBody)
                assertEquals("email003", responseBody.emailAddress)
                assertEquals(mutableListOf("site001", "site007"), responseBody.siteList)
                testTransaction {
                    val user = User.findById(6)
                    assertNotNull(user)
                    assertNotNull(user.lastEmail)
                    assertEquals("email003", user.lastEmail!!.emailAddress)
                    assertEquals(6, user.lastEmail!!.user.id.value)
                }
            }
        }

    }

    @Nested
    inner class Delete {

        @Nested
        inner class Challenged {

            @Test
            fun `no cookie`() = testApplication {
                val client = createAndConfigureClientWithoutCookie()
                val response = client.delete(EmailRoute.Delete("email002"))
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `bad cookie`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f9999"))
                val response = client.delete(EmailRoute.Delete("email002"))
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

        }

        @Nested
        inner class Validated {

            @Test
            fun `no last user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))
                assertThrows<Exception> {
                    client.delete(EmailRoute.Delete("email002"))
                }
            }

            @Test
            fun `non-existing email`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                val emailsBefore = testTransaction {
                    Email.find { Emails.userId eq 6 }.count()
                }
                val response = client.delete(EmailRoute.Delete("emailXYZ"))
                assertEquals(HttpStatusCode.OK, response.status)
                assertNull(response.body<Boolean?>())
                testTransaction {
                    val user = User.findById(6)
                    assertNotNull(user)
                    assertNull(user.lastEmail)
                    assertEquals(emailsBefore, Email.find { Emails.userId eq 6 }.count())
                }
            }

            @Test
            fun `email from other user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                val emailsBefore = testTransaction {
                    Email.find { Emails.userId eq 6 }.count()
                }
                val response = client.delete(EmailRoute.Delete("email011"))
                assertEquals(HttpStatusCode.OK, response.status)
                assertNull(response.body<Boolean?>())
                testTransaction {
                    val user = User.findById(6)
                    assertNotNull(user)
                    assertNull(user.lastEmail)
                    assertEquals(emailsBefore, Email.find { Emails.userId eq 6 }.count())
                    assertNotNull(Email.findById(13))
                }
            }

            @Test
            fun `email from last user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                val emailsBefore = testTransaction {
                    Email.find { Emails.userId eq 6 }.count()
                }
                val response = client.delete(EmailRoute.Delete("email003"))
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<Unit?>()
                assertNotNull(responseBody)
                testTransaction {
                    val user = User.findById(6)
                    assertNotNull(user)
                    assertNull(user.lastEmail)
                    assertEquals(emailsBefore - 1, Email.find { Emails.userId eq 6 }.count())
                    assertNull(Email.findById(10))
                }
            }
        }

    }

}
