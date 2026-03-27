plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatformModule)
}

dependencies {
    implementation(project(":shared"))
    intellijPlatform {
        //bundledPlugins(providers.gradleProperty("cssPlatformBundledPlugins").map { it.split(',') })
    }
}

