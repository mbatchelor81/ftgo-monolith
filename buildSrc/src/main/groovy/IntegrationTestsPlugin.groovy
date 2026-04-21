import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

/**
 * Legacy IntegrationTestsPlugin — Gradle-8 compatible rewrite.
 *
 * Adds an `integrationTest` source set + task to any legacy module that
 * still applies it. The original implementation used APIs removed in
 * Gradle 5+ (`testClassesDir`, `reports.html.destination` assignment,
 * implicit `testCompile` configuration); this version expresses the same
 * contract via current Gradle APIs.
 *
 * NEW services must NOT apply this plugin. Use the standard Gradle
 * `JvmTestSuite` approach together with `ftgo.testing-conventions`.
 */
class IntegrationTestsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply('java')

        def sourceSets = project.extensions.getByType(org.gradle.api.tasks.SourceSetContainer)
        def integrationTest = sourceSets.create('integrationTest') {
            java.srcDir project.file('src/integration-test/java')
            resources.srcDir project.file('src/integration-test/resources')
            compileClasspath += sourceSets.main.output + sourceSets.test.output
            runtimeClasspath += sourceSets.main.output + sourceSets.test.output
        }

        project.configurations.named('integrationTestImplementation') {
            extendsFrom project.configurations.testImplementation
        }
        project.configurations.named('integrationTestRuntimeOnly') {
            extendsFrom project.configurations.testRuntimeOnly
        }

        project.tasks.register('integrationTest', Test) { task ->
            task.testClassesDirs = integrationTest.output.classesDirs
            task.classpath = integrationTest.runtimeClasspath
            task.shouldRunAfter project.tasks.named('test')
        }
    }
}
