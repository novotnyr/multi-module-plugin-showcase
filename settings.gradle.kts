rootProject.name = "mincssrel"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("org.jetbrains.intellij.platform.settings") version "2.13.1"
}

include("shared")
include("css")