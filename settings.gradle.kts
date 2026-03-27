rootProject.name = "mincssrel"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include("shared")
include("css")