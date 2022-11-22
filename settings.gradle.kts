/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

rootProject.name = "my-password-gen"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin" aliasOf "1.7.10") { vRef ->
                plugins(vRef) {
                    +("kotlinMultiplatform" aliasOf "org.jetbrains.kotlin.multiplatform")
                    +("pluginSerialization" aliasOf "org.jetbrains.kotlin.plugin.serialization")
                }
                libraries(vRef) {
                    +("kotlin-junit" aliasOf "org.jetbrains.kotlin:kotlin-test-junit5")
                }
            }
            version("ktor" aliasOf "2.1.2") { vRef ->
                plugins(vRef) {
                    +("ktor" aliasOf "io.ktor.plugin")
                }
                libraries(vRef) {
                    +("ktor-serialization" aliasOf "io.ktor:ktor-serialization-kotlinx-json")
                    +("ktor-resources" aliasOf "io.ktor:ktor-resources")

                    +("ktor-server-contentNegotiation" aliasOf "io.ktor:ktor-server-content-negotiation")
                    +("ktor-server-sessions" aliasOf "io.ktor:ktor-server-sessions")
                    +("ktor-server-auth" aliasOf "io.ktor:ktor-server-auth")
                    +("ktor-server-compression" aliasOf "io.ktor:ktor-server-compression")
                    +("ktor-server-coreJvm" aliasOf "io.ktor:ktor-server-core-jvm")
                    +("ktor-server-netty" aliasOf "io.ktor:ktor-server-netty")
                    +("ktor-server-resources" aliasOf "io.ktor:ktor-server-resources")
                    +("ktor-server-callLogging" aliasOf "io.ktor:ktor-server-call-logging")
                    +("ktor-server-statusPages" aliasOf "io.ktor:ktor-server-status-pages")

                    +("ktor-client-core" aliasOf "io.ktor:ktor-client-core")
                    +("ktor-client-js" aliasOf "io.ktor:ktor-client-js")
                    +("ktor-client-contentNegotiation" aliasOf "io.ktor:ktor-client-content-negotiation")
                    +("ktor-client-resources" aliasOf "io.ktor:ktor-client-resources")
                    +("ktor-client-logging" aliasOf "io.ktor:ktor-client-logging")

                    +("ktor-server-testHost" aliasOf "io.ktor:ktor-server-test-host")
                }
            }
            version("exposed" aliasOf "0.39.2") { vRef ->
                libraries(vRef) {
                    +("exposed-core" aliasOf "org.jetbrains.exposed:exposed-core")
                    +("exposed-dao" aliasOf "org.jetbrains.exposed:exposed-dao")
                    +("exposed-jdbc" aliasOf "org.jetbrains.exposed:exposed-jdbc")
                    +("exposed-datetime" aliasOf "org.jetbrains.exposed:exposed-kotlin-datetime")
                }
            }
            version("testcontainers" aliasOf "1.17.5") { vRef ->
                libraries(vRef) {
                    +("testcontainers-core" aliasOf "org.testcontainers:testcontainers")
                    +("testcontainers-junit" aliasOf "org.testcontainers:junit-jupiter")
                    +("testcontainers-postgres" aliasOf "org.testcontainers:postgresql")
                }
            }
            libraries {
                +("serialization" aliasOf "org.jetbrains.kotlinx:kotlinx-serialization-core:0.7.2")
                +("wrappers" aliasOf "org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.390")
                +("logback" aliasOf "ch.qos.logback:logback-classic:1.2.11")
                +("kotlinLogging" aliasOf "io.github.microutils:kotlin-logging-jvm:2.0.11")
                +("hikari" aliasOf "com.zaxxer:HikariCP:5.0.1")
                +("mockk" aliasOf "io.mockk:mockk:1.9.3")
                +("postgres" aliasOf "org.postgresql:postgresql:42.5.0")
                +("koin" aliasOf "io.insert-koin:koin-ktor:3.2.2")
            }
            noVersionLibraries {
                +("wrappers-react" aliasOf "org.jetbrains.kotlin-wrappers:kotlin-react")
                +("wrappers-reactDom" aliasOf "org.jetbrains.kotlin-wrappers:kotlin-react-dom")
                +("wrappers-emotion" aliasOf "org.jetbrains.kotlin-wrappers:kotlin-emotion")
            }
            bundle("ktor-server") {
                +"ktor-server-contentNegotiation"
                +"ktor-server-sessions"
                +"ktor-server-auth"
                +"ktor-server-compression"
                +"ktor-server-coreJvm"
                +"ktor-server-netty"
                +"ktor-server-resources"
                +"ktor-server-callLogging"
                +"ktor-server-statusPages"
            }
            bundle("ktor-client") {
                +"ktor-client-core"
                +"ktor-client-js"
                +"ktor-client-contentNegotiation"
                +"ktor-client-resources"
                +"ktor-client-logging"
            }
            bundle("ktor-server-test") {
                +"ktor-client-resources"
                +"ktor-client-contentNegotiation"
                +"ktor-server-testHost"
            }
            bundle("ktor-common") {
                +"ktor-serialization"
                +"ktor-resources"
            }
            bundle("wrappers") {
                +"wrappers-react"
                +"wrappers-reactDom"
                +"wrappers-emotion"
            }
            bundle("exposed") {
                +"exposed-core"
                +"exposed-dao"
                +"exposed-jdbc"
                +"exposed-datetime"
            }
            bundle("testcontainers") {
                +"testcontainers-core"
                +"testcontainers-junit"
                +"testcontainers-postgres"
            }
        }
    }
}

inline fun VersionCatalogBuilder.version(vRef: Alias<String>, versionBuilder: VersionCatalogBuilder.(String) -> Unit) {
    version(vRef.alias, vRef.of)
    this.versionBuilder(vRef.alias)
}

inline fun VersionCatalogBuilder.libraries(librariesBuilder: CoolMap<String>.() -> Unit) {
    val libraryMap = CoolMap<String>().apply(librariesBuilder).toMap()
    for (entry in libraryMap) {
        library("libraries-${entry.key}", entry.value)
    }
}

inline fun VersionCatalogBuilder.libraries(vRef: String, librariesBuilder: CoolMap<String>.() -> Unit) {
    val libraryMap = CoolMap<String>().apply(librariesBuilder).toMap()
    for (entry in libraryMap) {
        val alias = entry.key
        val (group, artifact) = entry.value.split(":")
        library("libraries-$alias", group, artifact).versionRef(vRef)
    }
}

inline fun VersionCatalogBuilder.noVersionLibraries(librariesBuilder: CoolMap<String>.() -> Unit) {
    val libraryMap = CoolMap<String>().apply(librariesBuilder).toMap()
    for (entry in libraryMap) {
        val alias = entry.key
        val (group, artifact) = entry.value.split(":")
        library("libraries-$alias", group, artifact).withoutVersion()
    }
}

inline fun VersionCatalogBuilder.plugins(vRef: String, pluginsBuilder: CoolMap<String>.() -> Unit) {
    val pluginMap = CoolMap<String>().apply(pluginsBuilder).toMap()
    for (entry in pluginMap) {
        plugin(entry.key, entry.value).versionRef(vRef)
    }
}

inline fun VersionCatalogBuilder.bundle(vRef: String, bundlesBuilder: CoolBundle.() -> Unit) {
    val bundleList = CoolBundle().apply(bundlesBuilder).toList()
    bundle(vRef, bundleList)
}

class CoolMap<V>(private val map: MutableMap<String, V> = mutableMapOf()) {
    fun toMap() = map.toMap()
    operator fun Alias<V>.unaryPlus() = map.put(alias, of)
}

class CoolBundle(private val list: MutableList<String> = mutableListOf()) {
    fun toList() = list.toList()
    operator fun String.unaryPlus() = list.add("libraries-$this")
}

class Alias<V>(val alias: String, val of: V)

infix fun <V> String.aliasOf(other: V) = Alias(this, other)
