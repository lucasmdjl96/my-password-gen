/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.kotest

import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.common.dto.server.SiteServerDto
import com.mypasswordgen.common.routes.SiteRoute
import com.mypasswordgen.server.kotest.dto.*
import com.mypasswordgen.server.model.Site
import com.mypasswordgen.server.repository.crypto.encode
import com.mypasswordgen.server.tables.Sites
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.uuid
import io.kotest.property.checkAll
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.and

class SiteTest : FunSpec({
    val testTransaction = initDatabase()

    context("create site") {
        context("with no cookie") {
            should("respond with Unauthorized" and "not create any site") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        SiteServerDto.arb()
                    ) { fullDatabaseDto, siteServerDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(null)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.post(SiteRoute.New()) {
                            setBody(siteServerDto)
                            contentType(ContentType.Application.Json)
                        }
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with bad cookie") {
            should("respond with Unauthorized" and "not create any site") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        SiteServerDto.arb(),
                        Arb.uuid()
                    ) { fullDatabaseDto, siteServerDto, badSessionId ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.post(SiteRoute.New()) {
                            setBody(siteServerDto)
                            contentType(ContentType.Application.Json)
                        }
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with good cookie") {
            context("without last user") {
                should("respond with PreconditionFailed" and "not create any site") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            SiteServerDto.arb()
                        ) { fullDatabaseDto, siteServerDto ->
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val session = fullDatabaseDto.sessionSet.first()
                            val client = createAndConfigureClientWithCookie(session.id)
                            val databaseBefore = fullDatabaseDto.encode()
                            val response = client.post(SiteRoute.New()) {
                                setBody(siteServerDto)
                                contentType(ContentType.Application.Json)
                            }
                            response shouldHaveStatus HttpStatusCode.PreconditionFailed
                            testTransaction {
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
            context("with last user") {
                context("without last email") {
                    should("respond with PreconditionFailed" and "not create any site") {
                        testApplication {
                            checkAll(
                                FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                                SiteServerDto.arb()
                            ) { fullDatabaseDto, siteServerDto ->
                                val session = fullDatabaseDto.sessionSet.firstOrNull {
                                    it.userSet.isNotEmpty()
                                } ?: return@checkAll
                                val lastUser = session.userSet.first()
                                testTransaction {
                                    exec(fullDatabaseDto.insertStatement())
                                    exec(session.makeLastUserStatement(lastUser))
                                }
                                val client = createAndConfigureClientWithCookie(session.id)
                                val databaseBefore = fullDatabaseDto.encode()
                                val response = client.post(SiteRoute.New()) {
                                    setBody(siteServerDto)
                                    contentType(ContentType.Application.Json)
                                }
                                response shouldHaveStatus HttpStatusCode.PreconditionFailed
                                testTransaction {
                                    FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                    cleanUp()
                                }
                            }
                        }
                    }
                }
                context("with last email") {
                    context("with not a currently existing site from last email") {
                        should("respond with OK" and "create the site on last email") {
                            testApplication {
                                checkAll(
                                    FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                                    SiteServerDto.arb()
                                ) { fullDatabaseDto, siteServerDto ->
                                    val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                        fullSession.userSet.isNotEmpty() &&
                                                !fullSession.userSet.flatMap { it.emailSet }
                                                    .flatMap { it.siteSet }
                                                    .map { it.siteName }
                                                    .contains(siteServerDto.siteName)
                                    } ?: return@checkAll
                                    val lastUser = session.userSet.firstOrNull { fullUser ->
                                        fullUser.emailSet.isNotEmpty() &&
                                                !fullUser.emailSet.flatMap { it.siteSet }.map { it.siteName }
                                                    .contains(siteServerDto.siteName)
                                    } ?: return@checkAll
                                    val lastEmail = lastUser.emailSet.firstOrNull { fullEmail ->
                                        fullEmail.siteSet.isNotEmpty() &&
                                                !fullEmail.siteSet.map { it.siteName }
                                                    .contains(siteServerDto.siteName)
                                    } ?: return@checkAll
                                    testTransaction {
                                        exec(fullDatabaseDto.insertStatement())
                                        exec(session.makeLastUserStatement(lastUser))
                                        exec(lastUser.makeLastEmailStatement(lastEmail))
                                    }
                                    val sitesBefore = testTransaction {
                                        Site.find { Sites.emailId eq lastEmail.id }.count()
                                    }
                                    val client = createAndConfigureClientWithCookie(session.id)
                                    val response = client.post(SiteRoute.New()) {
                                        setBody(siteServerDto)
                                        contentType(ContentType.Application.Json)
                                    }
                                    response shouldHaveStatus HttpStatusCode.OK
                                    response shouldHaveStatus HttpStatusCode.OK
                                    val responseBody = response.body<SiteClientDto>()
                                    responseBody.shouldNotBeNull()
                                    testTransaction {
                                        Site.find { Sites.emailId eq lastEmail.id and (Sites.siteName eq siteServerDto.siteName.encode()) }
                                            .shouldNotBeEmpty()
                                        Site.find { Sites.emailId eq lastEmail.id }
                                            .count() shouldBe sitesBefore + 1
                                        for (fullSessionDto in fullDatabaseDto.sessionSet.filterNot { it.id == session.id }) {
                                            FullSessionDto.recoverFromDatabase(fullSessionDto.id) shouldBe fullSessionDto.encode()
                                        }
                                        for (fullUserDto in session.userSet.filterNot { it.id == lastUser.id }) {
                                            FullUserDto.recoverFromDatabase(fullUserDto.id) shouldBe fullUserDto.encode()
                                        }
                                        for (fullEmailDto in lastUser.emailSet.filterNot { it.id == lastEmail.id }) {
                                            FullEmailDto.recoverFromDatabase(fullEmailDto.id) shouldBe fullEmailDto.encode()
                                        }
                                        for (fullSiteDto in lastEmail.siteSet.filterNot { it.siteName == siteServerDto.siteName.encode() }) {
                                            FullSiteDto.recoverFromDatabase(fullSiteDto.id) shouldBe fullSiteDto.encode()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    context("with a currently existing site from last email") {
                        should("respond with Conflict" and "not create any site") {
                            testApplication {
                                checkAll(FullDatabaseDto.arb(1..maxSize, 0..maxSize)) { fullDatabaseDto ->
                                    val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                        fullSession.userSet.flatMap { it.emailSet }.flatMap { it.siteSet }
                                            .isNotEmpty()
                                    } ?: return@checkAll
                                    val lastUser = session.userSet.firstOrNull { fullUser ->
                                        fullUser.emailSet.flatMap { it.siteSet }.isNotEmpty()
                                    } ?: return@checkAll
                                    val lastEmail = lastUser.emailSet.firstOrNull { fullEmail ->
                                        fullEmail.siteSet.isNotEmpty()
                                    } ?: return@checkAll
                                    val site = lastEmail.siteSet.first()
                                    testTransaction {
                                        exec(fullDatabaseDto.insertStatement())
                                        exec(session.makeLastUserStatement(lastUser))
                                        exec(lastUser.makeLastEmailStatement(lastEmail))
                                    }
                                    val databaseBefore = fullDatabaseDto.encode()
                                    val client = createAndConfigureClientWithCookie(session.id)
                                    val response = client.post(SiteRoute.New()) {
                                        setBody(site.toSiteServerDto())
                                        contentType(ContentType.Application.Json)
                                    }
                                    response shouldHaveStatus HttpStatusCode.Conflict
                                    testTransaction {
                                        FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                        cleanUp()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    context("find site") {
        context("with no cookie") {
            should("respond with Unauthorized") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.string(minSize = 1)
                    ) { fullDatabaseDto, siteName ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(null)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.get(SiteRoute.Find(siteName))
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with bad cookie") {
            should("respond with Unauthorized") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.string(minSize = 1),
                        Arb.uuid()
                    ) { fullDatabaseDto, siteName, badSessionId ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.get(SiteRoute.Find(siteName))
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with good cookie") {
            context("without last user") {
                should("respond with PreconditionFailed") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            Arb.string(minSize = 1)
                        ) { fullDatabaseDto, siteName ->
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val session = fullDatabaseDto.sessionSet.first()
                            val databaseBefore = fullDatabaseDto.encode()
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.get(SiteRoute.Find(siteName))
                            response shouldHaveStatus HttpStatusCode.PreconditionFailed
                            testTransaction {
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
            context("with last user") {
                context("without last email") {
                    should("respond with PreconditionFailed") {
                        testApplication {
                            checkAll(
                                FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                                Arb.string(minSize = 1)
                            ) { fullDatabaseDto, siteName ->
                                val session = fullDatabaseDto.sessionSet.firstOrNull {
                                    it.userSet.isNotEmpty()
                                } ?: return@checkAll
                                val lastUser = session.userSet.first()
                                testTransaction {
                                    exec(fullDatabaseDto.insertStatement())
                                    exec(session.makeLastUserStatement(lastUser))
                                }
                                val databaseBefore = fullDatabaseDto.encode()
                                val client = createAndConfigureClientWithCookie(session.id)
                                val response = client.get(SiteRoute.Find(siteName))
                                response shouldHaveStatus HttpStatusCode.PreconditionFailed
                                testTransaction {
                                    FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                    cleanUp()
                                }
                            }
                        }
                    }
                }
                context("with last email") {
                    context("with not a currently existing site from last email") {
                        should("respond with NotFound") {
                            testApplication {
                                checkAll(
                                    FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                                    SiteServerDto.arb()
                                ) { fullDatabaseDto, siteServerDto ->
                                    val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                        fullSession.userSet.isNotEmpty() &&
                                                !fullSession.userSet.flatMap { it.emailSet }
                                                    .flatMap { it.siteSet }
                                                    .map { it.siteName }
                                                    .contains(siteServerDto.siteName)
                                    } ?: return@checkAll
                                    val lastUser = session.userSet.firstOrNull { fullUser ->
                                        fullUser.emailSet.isNotEmpty() &&
                                                !fullUser.emailSet.flatMap { it.siteSet }.map { it.siteName }
                                                    .contains(siteServerDto.siteName)
                                    } ?: return@checkAll
                                    val lastEmail = lastUser.emailSet.firstOrNull { fullEmail ->
                                        fullEmail.siteSet.isNotEmpty() &&
                                                !fullEmail.siteSet.map { it.siteName }
                                                    .contains(siteServerDto.siteName)
                                    } ?: return@checkAll
                                    testTransaction {
                                        exec(fullDatabaseDto.insertStatement())
                                        exec(session.makeLastUserStatement(lastUser))
                                        exec(lastUser.makeLastEmailStatement(lastEmail))
                                    }
                                    val databaseBefore = fullDatabaseDto.encode()
                                    val client = createAndConfigureClientWithCookie(session.id)
                                    val response = client.get(SiteRoute.Find(siteServerDto.siteName))
                                    response shouldHaveStatus HttpStatusCode.NotFound
                                    testTransaction {
                                        FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                        cleanUp()
                                    }
                                }
                            }
                        }
                    }
                    context("with a currently existing site from last email") {
                        should("respond with OK" and "respond with the corresponding site Id") {
                            testApplication {
                                checkAll(
                                    FullDatabaseDto.arb(1..maxSize, 0..maxSize)
                                ) { fullDatabaseDto ->
                                    val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                        fullSession.userSet.flatMap { it.emailSet }.flatMap { it.siteSet }
                                            .isNotEmpty()
                                    } ?: return@checkAll
                                    val lastUser = session.userSet.firstOrNull { fullUser ->
                                        fullUser.emailSet.flatMap { it.siteSet }.isNotEmpty()
                                    } ?: return@checkAll
                                    val lastEmail = lastUser.emailSet.firstOrNull { fullEmail ->
                                        fullEmail.siteSet.isNotEmpty()
                                    } ?: return@checkAll
                                    val site = lastEmail.siteSet.first()
                                    testTransaction {
                                        exec(fullDatabaseDto.insertStatement())
                                        exec(session.makeLastUserStatement(lastUser))
                                        exec(lastUser.makeLastEmailStatement(lastEmail))
                                    }
                                    val databaseBefore = fullDatabaseDto.encode()
                                    val client = createAndConfigureClientWithCookie(session.id)
                                    val response = client.get(SiteRoute.Find(site.siteName))
                                    response shouldHaveStatus HttpStatusCode.OK
                                    val responseBody = response.body<SiteClientDto>()
                                    responseBody.shouldNotBeNull()
                                    responseBody.id shouldBe site.id.toString()
                                    testTransaction {
                                        FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                        cleanUp()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    context("delete site") {
        context("with no cookie") {
            should("respond with Unauthorized" and "not delete any site") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.string(minSize = 1)
                    ) { fullDatabaseDto, siteName ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(null)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.delete(SiteRoute.Delete(siteName))
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with bad cookie") {
            should("respond with Unauthorized" and "not delete any site") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.string(minSize = 1),
                        Arb.uuid()
                    ) { fullDatabaseDto, siteName, badSessionId ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.delete(SiteRoute.Delete(siteName))
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with good cookie") {
            context("without last user") {
                should("respond with PreconditionFailed" and "not delete any site") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            Arb.string(minSize = 1)
                        ) { fullDatabaseDto, siteName ->
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val session = fullDatabaseDto.sessionSet.first()
                            val databaseBefore = fullDatabaseDto.encode()
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.delete(SiteRoute.Delete(siteName))
                            response shouldHaveStatus HttpStatusCode.PreconditionFailed
                            testTransaction {
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
            context("with last user") {
                context("without last email") {
                    should("respond with PreconditionFailed" and "not delete any site") {
                        testApplication {
                            checkAll(
                                FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                                Arb.string(minSize = 1)
                            ) { fullDatabaseDto, siteName ->
                                val session = fullDatabaseDto.sessionSet.firstOrNull {
                                    it.userSet.isNotEmpty()
                                } ?: return@checkAll
                                val lastUser = session.userSet.first()
                                testTransaction {
                                    exec(fullDatabaseDto.insertStatement())
                                    exec(session.makeLastUserStatement(lastUser))
                                }
                                val databaseBefore = fullDatabaseDto.encode()
                                val client = createAndConfigureClientWithCookie(session.id)
                                val response = client.delete(SiteRoute.Delete(siteName))
                                response shouldHaveStatus HttpStatusCode.PreconditionFailed
                                testTransaction {
                                    FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                    cleanUp()
                                }
                            }
                        }
                    }
                }
                context("with last email") {
                    context("with not a currently existing site from last email") {
                        should("respond with NotFound" and "not delete any site") {
                            testApplication {
                                checkAll(
                                    FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                                    SiteServerDto.arb()
                                ) { fullDatabaseDto, siteServerDto ->
                                    val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                        fullSession.userSet.isNotEmpty() &&
                                                !fullSession.userSet.flatMap { it.emailSet }
                                                    .flatMap { it.siteSet }
                                                    .map { it.siteName }
                                                    .contains(siteServerDto.siteName)
                                    } ?: return@checkAll
                                    val lastUser = session.userSet.firstOrNull { fullUser ->
                                        fullUser.emailSet.isNotEmpty() &&
                                                !fullUser.emailSet.flatMap { it.siteSet }.map { it.siteName }
                                                    .contains(siteServerDto.siteName)
                                    } ?: return@checkAll
                                    val lastEmail = lastUser.emailSet.firstOrNull { fullEmail ->
                                        fullEmail.siteSet.isNotEmpty() &&
                                                !fullEmail.siteSet.map { it.siteName }
                                                    .contains(siteServerDto.siteName)
                                    } ?: return@checkAll
                                    testTransaction {
                                        exec(fullDatabaseDto.insertStatement())
                                        exec(session.makeLastUserStatement(lastUser))
                                        exec(lastUser.makeLastEmailStatement(lastEmail))
                                    }
                                    val databaseBefore = fullDatabaseDto.encode()
                                    val client = createAndConfigureClientWithCookie(session.id)
                                    val response = client.delete(SiteRoute.Delete(siteServerDto.siteName))
                                    response shouldHaveStatus HttpStatusCode.NotFound
                                    testTransaction {
                                        FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                        cleanUp()
                                    }
                                }
                            }
                        }
                    }
                    context("with a currently existing site from last email") {
                        should("respond with OK" and "the site should be deleted" and "respond with the corresponding site id") {
                            testApplication {
                                checkAll(
                                    FullDatabaseDto.arb(1..maxSize, 0..maxSize)
                                ) { fullDatabaseDto ->
                                    val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                        fullSession.userSet.flatMap { it.emailSet }.flatMap { it.siteSet }
                                            .isNotEmpty()
                                    } ?: return@checkAll
                                    val lastUser = session.userSet.firstOrNull { fullUser ->
                                        fullUser.emailSet.flatMap { it.siteSet }.isNotEmpty()
                                    } ?: return@checkAll
                                    val lastEmail = lastUser.emailSet.firstOrNull { fullEmail ->
                                        fullEmail.siteSet.isNotEmpty()
                                    } ?: return@checkAll
                                    val site = lastEmail.siteSet.first()
                                    testTransaction {
                                        exec(fullDatabaseDto.insertStatement())
                                        exec(session.makeLastUserStatement(lastUser))
                                        exec(lastUser.makeLastEmailStatement(lastEmail))
                                    }
                                    val sitesBefore = testTransaction {
                                        Site.find { Sites.emailId eq lastEmail.id }.count()
                                    }
                                    val client = createAndConfigureClientWithCookie(session.id)
                                    val response = client.delete(SiteRoute.Delete(site.siteName))
                                    response shouldHaveStatus HttpStatusCode.OK
                                    val responseBody = response.body<SiteClientDto>()
                                    responseBody.shouldNotBeNull()
                                    responseBody.id shouldBe site.id.toString()
                                    testTransaction {
                                        Site.find { Sites.emailId eq lastEmail.id }
                                            .count() shouldBe sitesBefore - 1
                                        Site.findById(site.id).shouldBeNull()
                                        for (fullSessionDto in fullDatabaseDto.sessionSet.filterNot { it.id == session.id }) {
                                            FullSessionDto.recoverFromDatabase(fullSessionDto.id) shouldBe fullSessionDto.encode()
                                        }
                                        for (fullUserDto in session.userSet.filterNot { it.id == lastUser.id }) {
                                            FullUserDto.recoverFromDatabase(fullUserDto.id) shouldBe fullUserDto.encode()
                                        }
                                        for (fullEmailDto in lastUser.emailSet.filterNot { it.id == lastEmail.id }) {
                                            FullEmailDto.recoverFromDatabase(fullEmailDto.id) shouldBe fullEmailDto.encode()
                                        }
                                        for (fullSiteDto in lastEmail.siteSet.filterNot { it.id == site.id }) {
                                            FullSiteDto.recoverFromDatabase(fullSiteDto.id) shouldBe fullSiteDto.encode()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
})
