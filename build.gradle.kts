@file:Suppress("DSL_SCOPE_VIOLATION")
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

plugins {
    application
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.pluginSerialization)
    alias(libs.plugins.ktor)
}

group = "com.mypasswordgen"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
        compilations {
            val main by getting
            val integrationTest by compilations.creating {
                defaultSourceSet {
                    dependencies {
                        // Compile against the main compilation's compile classpath and outputs:
                        implementation(main.compileDependencyFiles + main.output.classesDirs)
                    }
                }
            }
        }
        testRuns.create("integrationTest") {
            executionTask.configure {
                useJUnitPlatform()
            }
            setExecutionSourceFrom(compilations.getByName("integrationTest"))
        }
        withJava()
    }
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.bundles.ktor.common)
                implementation(libs.libraries.serialization)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.bundles.ktor.server)
                implementation(libs.libraries.logback)
                implementation(libs.libraries.kotlinLogging)
                implementation(libs.libraries.hikari)
                implementation(libs.bundles.exposed)
                implementation(libs.libraries.postgres)
                implementation(libs.libraries.koin)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.ktor.server.test)
                implementation(libs.bundles.testcontainers)
                implementation(libs.libraries.kotlin.junit)
                implementation(libs.libraries.mockk)
            }
        }
        val jvmIntegrationTest by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.bundles.ktor.server.test)
                implementation(libs.bundles.testcontainers)
                implementation(libs.libraries.kotlin.junit)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.bundles.ktor.client)
                implementation(project.dependencies.enforcedPlatform(libs.libraries.wrappers))
                implementation(libs.bundles.wrappers)
            }
        }
        val jsTest by getting {
            dependencies {
                /*implementation("io.mockk:mockk:$mockkVersion")
                implementation(npm("react-test-renderer","18.2.0"))
                implementation(npm("@testing-library/react", "13.4.0"))*/
            }
        }
    }
}

application {
    mainClass.set("com.mypasswordgen.server.ServerKt")
}

tasks.named<Copy>("jvmIntegrationTestProcessResources") {
    val jvmProcessResources = tasks.named<Copy>("jvmProcessResources")
    dependsOn(jvmProcessResources)
    finalizedBy(tasks.named("jvmIntegrationTestCopyResources"))
}

tasks.register<Copy>("jvmIntegrationTestCopyResources") {
    from(tasks.named<Copy>("jvmProcessResources")) {
        exclude("application.conf")
    }
    into(tasks.named<Copy>("jvmIntegrationTestProcessResources").get().destinationDir)
}

tasks.named<Copy>("jvmProcessResources") {
    dependsOn(tasks.named<Copy>("copyJs"))
    finalizedBy(tasks.named<Exec>("addCacheVersion"))
}

tasks.register<Copy>("copyJs") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    val jvmProcessResources = tasks.named<Copy>("jvmProcessResources")
    from(jsBrowserDistribution) {
        include("**/*.js",  "**/*.js.map")
    }
    into(jvmProcessResources.get().destinationDir.resolve("static/js"))
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}

tasks.register<Exec>("addCacheVersion") {
    workingDir(projectDir)
    if (!System.getProperty("os.name").toLowerCaseAsciiOnly().contains("windows"))
        commandLine("bash", "scripts/addCacheVersion.sh")
    else
        commandLine("cmd", "/c", "Powershell", "-File", "scripts\\addCacheVersion.ps1")
    dependsOn(tasks.named<Copy>("jvmProcessResources"))
}

tasks.register<Exec>("deploy") {
    dependsOn(tasks.named("buildFatJar"))
    workingDir(projectDir)
    environment("DEPLOY_VERSION", version)
    commandLine("bash", "scripts/deploy.sh")
}

tasks.named("buildFatJar") {
    dependsOn(tasks.named("check"))
}
