import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle convention plugin for FTGO microservices.
 *
 * Applies the new convention plugins that standardize build configuration
 * across all microservice modules under the services/ directory:
 * - ftgo.spring-boot-conventions (Java 17, Spring Boot 3.x, dependency management)
 * - ftgo.testing-conventions (JUnit 5, Rest-Assured, integration tests)
 * - ftgo.docker-conventions (Jib container image builds)
 * - ftgo.tracing-conventions (Actuator, Micrometer, Prometheus, distributed tracing)
 * - ftgo.quality-conventions (Checkstyle, SpotBugs, PMD static analysis)
 *
 * By default, bootJar is disabled and the standard jar task is enabled.
 * This allows empty scaffold modules to participate in ./gradlew build
 * without requiring a main class. Once a service has a Spring Boot
 * main class, re-enable bootJar in its build.gradle:
 *
 *   bootJar { enabled = true }
 *   jar { enabled = false }
 *
 * Usage in a service build.gradle:
 *   plugins {
 *       id 'ftgo.spring-boot-conventions'
 *       id 'ftgo.testing-conventions'
 *       id 'ftgo.docker-conventions'
 *   }
 *   // Or use the legacy shorthand:
 *   apply plugin: FtgoMicroservicePlugin
 */
class FtgoMicroservicePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply('ftgo.spring-boot-conventions')
        project.pluginManager.apply('ftgo.testing-conventions')
        project.pluginManager.apply('ftgo.docker-conventions')
        project.pluginManager.apply('ftgo.tracing-conventions')
        project.pluginManager.apply('ftgo.logging-conventions')
        project.pluginManager.apply('ftgo.quality-conventions')
    }
}
