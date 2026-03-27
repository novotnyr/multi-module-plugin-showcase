plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatformModule)
}

dependencies {
    implementation(project(":shared"))
    intellijPlatform {
        bundledPlugins(providers.gradleProperty("cssPlatformBundledPlugins").map { it.split(',') })
    }
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
    }
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}
