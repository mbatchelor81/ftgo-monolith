import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Legacy FtgoServicePlugin.
 *
 * Previously applied `org.springframework.boot` (2.0.3) and
 * `io.spring.dependency-management` (1.0.3), neither of which is
 * compatible with Gradle 8+. Retained as an empty marker so existing
 * `apply plugin: FtgoServicePlugin` calls in legacy `ftgo-*` build
 * scripts keep parsing during the microservices migration.
 *
 * NEW services must apply `ftgo.spring-boot-conventions` instead.
 */
class FtgoServicePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // No-op. Java plugin + source compatibility are applied by the
        // root `build.gradle` legacy-compat block for every `ftgo-*` module.
    }
}
