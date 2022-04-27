import org.gradle.internal.os.OperatingSystem

// WARNING: to update some dependencies, you will still have to change
// versions in more than one place. Take care!

plugins {
    java
    `maven-publish`
    // From https://docs.freefair.io/gradle-plugins/6.4.2/reference
    // This plugin simplifies the use of Lombok in Gradle by performing the following steps:
    //    * Lombok is added to the annotationProcessor and compileOnly configurations of each source set
    //    * For each source set a delombok task is created.
    //    * The javadoc task will be configured to read the delombok-ed sources instead of the actual sources.
    //    * lombok-mapstruct-binding is added to each source-set when 'org.mapstruct:mapstruct-processor' is found.
    //    * The compile tasks for each source set will consider the lombok.config(s) in their up-to-date check.
    id("io.freefair.lombok") version "6.4.2"

    val springBootVersion = "2.4.5"
    id("org.springframework.boot") version springBootVersion

    // READ https://github.com/node-gradle/gradle-node-plugin/blob/master/docs/usage.md
    // for how to execute npm commands properly
    id ("com.github.node-gradle.node") version "3.2.1"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://mvn.mchv.eu/repository/mchv/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    // The telegram api lib dependencies
    val tdlightVersion = "2.8.1.2"
    val tdlightNativesVersion = "4.0.242"
    // Jar lib itself
    implementation("it.tdlight:tdlight-java:$tdlightVersion")
    // By-os native dependencies
    val os = OperatingSystem.current()
    when {
        os.isWindows -> implementation("it.tdlight:tdlight-natives-windows-amd64:$tdlightNativesVersion")
        os.isLinux   -> implementation("it.tdlight:tdlight-natives-linux-amd64:$tdlightNativesVersion")
        os.isMacOsX  -> implementation("it.tdlight:tdlight-natives-osx-amd64:$tdlightNativesVersion")
        // Sorry, aarch64 mac users, you will have to compile & provide the .so urselves
        // (from https://github.com/tdlight-team/tdlight)
    }

    // Lombok
    val lombokVersion = "1.18.20"
    implementation("org.projectlombok:lombok:$lombokVersion")

    // JQuery for frontend AJAX
    implementation("org.webjars:jquery:3.6.0")

    // Spring (boot)
    val springBootVersion = "2.4.5"
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-security:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:$springBootVersion")

    // Spring/thymeleaf extensions
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.0.4.RELEASE")
    implementation("org.springframework.security:spring-security-taglibs:5.2.0.RELEASE")

    // Misc
    runtimeOnly("org.springframework.boot:spring-boot-devtools:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
}

// Build config...

group = "space"
version = "0.0.1-SNAPSHOT"
description = "SeaSearch"
java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

node {
    download.set(true)
    version.set("16.14.0")
    nodeProjectDir.set(file("${project.projectDir}/src/main/js"))
}

tasks {
    register<Copy>("cpVueToBootApp") {
        from("src/main/js/dist")
        destinationDir = file("src/main/resources/vue")
    }

    withType<JavaCompile>() {
        options.encoding = "UTF-8"
    }

    build {
        finalizedBy("npm_run_build")
    }

    named("npm_run_build") {
        finalizedBy("cpVueToBootApp")
    }
}
