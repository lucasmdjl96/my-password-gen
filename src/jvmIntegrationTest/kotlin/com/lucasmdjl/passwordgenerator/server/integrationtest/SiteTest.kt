package com.lucasmdjl.passwordgenerator.server.integrationtest

import com.lucasmdjl.passwordgenerator.common.dto.client.SiteClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.Site
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.tables.Sites
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SiteTest : TestParent() {

    @Nested
    inner class Create {

        @Nested
        inner class Challenged {

            @Test
            fun `no cookie`() = testApplication {
                val client = createAndConfigureClientWithoutCookie()
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto("site002"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `bad cookie`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f9999"))
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto("site002"))
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
                val sitesBefore = testTransaction {
                    Sites.selectAll().count()
                }
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto("siteXXX"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Sites.selectAll().count())
                    assertEmpty(Site.find { Sites.siteName eq "siteXXX" })
                }
            }

            @Test
            fun `no last email`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                val sitesBefore = testTransaction {
                    Sites.selectAll().count()
                }
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto("siteXXX"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Sites.selectAll().count())
                    assertEmpty(Site.find { Sites.siteName eq "siteXXX" })
                }
            }

            @Test
            fun `non-existing site`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                testTransaction {
                    User.findById(6)!!.lastEmail = Email.findById(10)
                }
                val sitesBefore = testTransaction {
                    Site.find { Sites.emailId eq 10 }.count()
                }
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto("siteXXX"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<SiteClientDto>()
                assertNotNull(responseBody)
                assertEquals("siteXXX", responseBody.siteName)
                testTransaction {
                    assertNotEmpty(Site.find { Sites.siteName eq "siteXXX" and (Sites.emailId eq 10) })
                    assertEquals(sitesBefore + 1, Site.find { Sites.emailId eq 10 }.count())
                }
            }

            @Test
            fun `site from other email`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                testTransaction {
                    User.findById(6)!!.lastEmail = Email.findById(10)
                }
                val sitesBefore = testTransaction {
                    Site.find { Sites.emailId eq 10 }.count()
                }
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto("site006"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<SiteClientDto>()
                assertNotNull(responseBody)
                assertEquals("site006", responseBody.siteName)
                testTransaction {
                    assertNotEmpty(Site.find { Sites.siteName eq "site006" and (Sites.emailId eq 10) })
                    assertEquals(sitesBefore + 1, Site.find { Sites.emailId eq 10 }.count())
                }
            }

            @Test
            fun `site from last email`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                testTransaction {
                    User.findById(6)!!.lastEmail = Email.findById(10)
                }
                val sitesBefore = testTransaction {
                    Site.find { Sites.emailId eq 10 }.count()
                }
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto("site007"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Conflict, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Site.find { Sites.emailId eq 10 }.count())
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
                val response = client.get(SiteRoute.Find("site002"))
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `bad cookie`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f9999"))
                val response = client.get(SiteRoute.Find("site002"))
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

        }

        @Nested
        inner class Validated {

            @Test
            fun `no last user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))
                val response = client.get(SiteRoute.Find("site002"))
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
            }

            @Test
            fun `no last email`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                val response = client.get(SiteRoute.Find("site002"))
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
            }

            @Test
            fun `non-existing site`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                testTransaction {
                    User.findById(6)!!.lastEmail = Email.findById(10)
                }
                val response = client.get(SiteRoute.Find("siteXXX"))
                assertEquals(HttpStatusCode.NotFound, response.status)
            }

            @Test
            fun `site from other user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                testTransaction {
                    User.findById(6)!!.lastEmail = Email.findById(10)
                }
                val response = client.get(SiteRoute.Find("site006"))
                assertEquals(HttpStatusCode.NotFound, response.status)
            }

            @Test
            fun `site from last user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                testTransaction {
                    User.findById(6)!!.lastEmail = Email.findById(10)
                }
                val response = client.get(SiteRoute.Find("site007"))
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<SiteClientDto>()
                assertNotNull(responseBody)
                assertEquals("site007", responseBody.siteName)
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
                val response = client.delete(SiteRoute.Delete("site002"))
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `bad cookie`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f9999"))
                val response = client.delete(SiteRoute.Delete("site002"))
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

        }

        @Nested
        inner class Validated {

            @Test
            fun `no last user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001"))
                val sitesBefore = testTransaction {
                    Sites.selectAll().count()
                }
                val response = client.delete(SiteRoute.Delete("site002"))
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Sites.selectAll().count())
                    assertEmpty(Site.find { Sites.siteName eq "siteXXX" })
                }
            }

            @Test
            fun `no last email`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                val sitesBefore = testTransaction {
                    Sites.selectAll().count()
                }
                val response = client.delete(SiteRoute.Delete("site002"))
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Sites.selectAll().count())
                    assertEmpty(Site.find { Sites.siteName eq "siteXXX" })
                }
            }

            @Test
            fun `non-existing site`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                testTransaction {
                    User.findById(6)!!.lastEmail = Email.findById(10)
                }
                val sitesBefore = testTransaction {
                    Site.find { Sites.emailId eq 10 }.count()
                }
                val response = client.delete(SiteRoute.Delete("siteXXX"))
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Site.find { Sites.emailId eq 10 }.count())
                }
            }

            @Test
            fun `site from other user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                testTransaction {
                    User.findById(6)!!.lastEmail = Email.findById(10)
                }
                val sitesBefore = testTransaction {
                    Site.find { Sites.emailId eq 10 }.count()
                }
                val response = client.delete(SiteRoute.Delete("site006"))
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Site.find { Sites.emailId eq 10 }.count())
                    assertNotNull(Email.findById(9))
                }
            }

            @Test
            fun `site from last user`() = testApplication {
                val client = createAndConfigureClientWithCookie(UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0002"))
                testTransaction {
                    User.findById(6)!!.lastEmail = Email.findById(10)
                }
                val sitesBefore = testTransaction {
                    Site.find { Sites.emailId eq 10 }.count()
                }
                val response = client.delete(SiteRoute.Delete("site007"))
                assertEquals(HttpStatusCode.OK, response.status)
                testTransaction {
                    assertEquals(sitesBefore - 1, Site.find { Sites.emailId eq 10 }.count())
                    assertNull(Site.findById(19))
                }
            }

        }

    }

}
