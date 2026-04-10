import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Legacy monolith service plugin.
 *
 * Applies only the dependency-management plugin with an explicit
 * Spring Boot 2.x BOM import so that legacy modules (Java 8) are
 * not affected by the Spring Boot 3.x Gradle plugin on the
 * buildSrc classpath.
 */
class FtgoServicePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.apply(plugin: "io.spring.dependency-management")

        // Import the Spring Boot 2.x BOM for legacy modules.
        // The version comes from gradle.properties (springBootVersion=2.0.3.RELEASE).
        def bootVersion = project.findProperty('springBootVersion') ?: '2.0.3.RELEASE'
        project.dependencyManagement {
            imports {
                mavenBom "org.springframework.boot:spring-boot-dependencies:${bootVersion}"
            }
        }
    }
}
