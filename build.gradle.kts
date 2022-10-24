val serializationVersion: String by project
val ktorVersion: String by project
val logbackVersion: String by project
val kotlinLoggingVersion: String by project
val kotlinWrappersVersion: String by project
val kotlinxHtmlVersion: String by project
val koinVersion: String by project

val exposedVersion: String by project
val postgresVersion: String by project

plugins {
    kotlin("multiplatform") version "1.7.10"
    application
    kotlin("plugin.serialization") version "1.7.10"
    id("io.ktor.plugin") version "2.1.2"
}

group = "com.lucasmdjl"
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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-resources:$ktorVersion")
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
                implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-server-sessions:$ktorVersion")
                implementation("io.ktor:ktor-server-auth:$ktorVersion")
                implementation("io.ktor:ktor-server-compression:$ktorVersion")
                implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-server-config-yaml:$ktorVersion")
                implementation("io.ktor:ktor-server-resources:$ktorVersion")
                implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
                implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

                implementation("ch.qos.logback:logback-classic:$logbackVersion")
                implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

                implementation("com.zaxxer:HikariCP:5.0.1")
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.postgresql:postgresql:$postgresVersion")

                implementation("io.insert-koin:koin-ktor:$koinVersion")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-junit5:1.7.10")
                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
                implementation("io.mockk:mockk:1.9.3")
                implementation("io.ktor:ktor-client-resources:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

                implementation("org.testcontainers:testcontainers:1.17.5")
                implementation("org.testcontainers:junit-jupiter:1.17.5")
                implementation("org.testcontainers:postgresql:1.17.5")
            }
        }
        val jvmIntegrationTest by getting {
            dependsOn(commonMain)
            dependencies {
                // Compile against the main compilation's compile classpath and outputs:
                implementation("org.jetbrains.kotlin:kotlin-test-junit5:1.7.10")
                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
                implementation("io.ktor:ktor-client-resources:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

                implementation("org.testcontainers:testcontainers:1.17.5")
                implementation("org.testcontainers:junit-jupiter:1.17.5")
                implementation("org.testcontainers:postgresql:1.17.5")
                /* ... */
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-resources:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                implementation(project.dependencies.enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:$kotlinWrappersVersion"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$kotlinWrappersVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:$kotlinWrappersVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:$kotlinWrappersVersion")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation("io.mockk:mockk:1.9.3")
                implementation(npm("react-test-renderer","18.2.0"))
                implementation(npm("@testing-library/react", "13.4.0"))
            }
        }
    }
}

application {
    mainClass.set("com.lucasmdjl.passwordgenerator.server.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}
