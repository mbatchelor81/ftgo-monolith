plugins {
    `groovy-gradle-plugin`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

// Plugin versions must match those in gradle/libs.versions.toml.
// The version catalog is available in the convention plugin scripts but not
// in this build script (it is a separate included build).
dependencies {
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.2.5")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.5")
    implementation("com.google.cloud.tools:jib-gradle-plugin:3.4.2")
}
