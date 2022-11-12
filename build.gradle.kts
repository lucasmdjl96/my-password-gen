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
        compilations.all {
            kotlinOptions.jvmTarget = "11"
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

                // Create a test task to run the tests produced by this compilation:
                tasks.register<Test>("integrationTest") {
                    // Run the tests with the classpath containing the compile dependencies (including 'main'),
                    // runtime dependencies, and the outputs of this compilation:
                    classpath = compileDependencyFiles + runtimeDependencyFiles + output.allOutputs

                    // Run only the tests from this compilation's outputs:
                    testClassesDirs = output.classesDirs
                }
            }
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
        commandLine("bash", "version.sh")
    else
        commandLine("cmd", "/c", "Powershell", "-File", "version.ps1")
    dependsOn(tasks.named<Copy>("jvmProcessResources"))
}
