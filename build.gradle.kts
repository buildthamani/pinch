import com.android.build.gradle.internal.utils.immutableListBuilder
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ktlint)
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    ktlint {
        enableExperimentalRules.set(true)
        additionalEditorconfig.set(
            mapOf(
                "ktlint_standard_package-naming" to "disabled",
            ),
        )
        reporters {
            reporter(ReporterType.JSON)
        }
        filter {
            val files =
                immutableListBuilder {
                    add("/generated/")
                    add("/build/")
                }
            exclude {
                val path =
                    projectDir
                        .toURI()
                        .relativize(it.file.toURI())
                        .path
                files.any { path.contains(it) }
            }
        }
    }
}
