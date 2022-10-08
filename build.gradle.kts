val serializationVersion: String by project
val ktorVersion: String by project
val logbackVersion: String by project
val kotlinLoggingVersion: String by project
val kotlinWrappersVersion: String by project
val kotlinxHtmlVersion : String by project

val exposedVersion: String by project
val postgresVersion: String by project

plugins {
    kotlin("multiplatform") version "1.7.10"
    application
    kotlin("plugin.serialization") version "1.7.10"
}

group = "com.lucasmdjl"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(LEGACY) {
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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
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
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-server-sessions:$ktorVersion")
                implementation("io.ktor:ktor-server-auth:$ktorVersion")
                implementation("io.ktor:ktor-server-http-redirect:$ktorVersion")
                implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-compression:$ktorVersion")
                implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-server-config-yaml:$ktorVersion")
                implementation("io.ktor:ktor-server-resources:$ktorVersion")
                implementation("io.ktor:ktor-server-call-logging:$ktorVersion")

                implementation("ch.qos.logback:logback-classic:$logbackVersion")
                implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

                implementation("com.zaxxer:HikariCP:5.0.1")
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.postgresql:postgresql:$postgresVersion")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-resources:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation(project.dependencies.enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:$kotlinWrappersVersion"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$kotlinWrappersVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:$kotlinWrappersVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:$kotlinWrappersVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:$kotlinWrappersVersion")

                implementation(npm("postcss", "8.4.16"))
                implementation(npm("postcss-loader", "7.0.1"))
                implementation(npm("autoprefixer", "10.4.12"))

            }
        }
        val jsTest by getting
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
