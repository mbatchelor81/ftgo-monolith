import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

/**
 * Legacy integration-test source-set plugin.
 *
 * Kept for backward compatibility with the monolith modules that still
 * use {@code apply plugin: IntegrationTestsPlugin}. New microservice
 * modules should prefer {@code id 'ftgo.testing-conventions'} which
 * provides the same integration-test source set plus JUnit 5 configuration.
 */
class IntegrationTestsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.sourceSets {
            integrationTest {
                java {
                    compileClasspath += main.output + test.output
                    runtimeClasspath += main.output + test.output
                    srcDir project.file('src/integration-test/java')
                }
                resources.srcDir project.file('src/integration-test/resources')
            }
        }

        project.configurations {
            integrationTestImplementation.extendsFrom project.configurations.testImplementation
            integrationTestRuntimeOnly.extendsFrom project.configurations.testRuntimeOnly
        }

        project.tasks.register("integrationTest", Test) {
            testClassesDirs = project.sourceSets.integrationTest.output.classesDirs
            classpath = project.sourceSets.integrationTest.runtimeClasspath
        }

        project.tasks.withType(Test).configureEach {
            reports.html.outputLocation.set(project.file("${project.reporting.baseDir}/${name}"))
        }
    }
}
