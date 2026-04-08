import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle convention plugin for FTGO microservices.
 *
 * Applies common configuration shared across all microservice modules
 * under the services/ directory. This includes:
 * - Spring Boot and dependency management plugins
 * - Integration test source set configuration
 * - Standard dependency versions
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
 *   apply plugin: FtgoMicroservicePlugin
 */
class FtgoMicroservicePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.apply(plugin: 'org.springframework.boot')
        project.apply(plugin: 'io.spring.dependency-management')
        project.apply(plugin: IntegrationTestsPlugin)

        // Disable bootJar by default so empty scaffold modules don't fail
        // with "Main class name has not been configured". Services must
        // re-enable bootJar once they have a @SpringBootApplication class.
        project.tasks.getByName('bootJar') { enabled = false }
        project.tasks.getByName('jar') { enabled = true }
    }
}
