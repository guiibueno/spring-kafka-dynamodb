plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.bueno"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.kafka:spring-kafka")

	implementation("io.micrometer:micrometer-core")
	implementation("io.micrometer:micrometer-registry-prometheus:latest.release")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

	implementation("aws.sdk.kotlin:aws-core-jvm:1.3.108")
	implementation("software.amazon.awssdk:dynamodb-enhanced:2.29.47")
	implementation("software.amazon.awssdk:netty-nio-client:2.29.47")

	implementation("ch.qos.logback:logback-core:1.5.16")
	implementation("ch.qos.logback:logback-classic:1.5.16")
	implementation("net.logstash.logback:logstash-logback-encoder:8.0")

	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("io.mockk:mockk:1.13.13")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:localstack")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
