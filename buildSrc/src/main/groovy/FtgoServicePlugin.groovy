import org.gradle.api.Plugin
import org.gradle.api.Project

class FtgoServicePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.apply(plugin: 'org.springframework.boot')
    	project.apply(plugin: "io.spring.dependency-management")

        // Pin the BOM to the legacy Spring Boot version so that
        // transitive dependencies resolve against 2.0.3, not the
        // plugin version (2.7.x, needed for Gradle 8 compatibility).
        project.dependencyManagement {
            imports {
                mavenBom "org.springframework.boot:spring-boot-dependencies:${project.property('springBootVersion')}"
            }
        }
    }
}
