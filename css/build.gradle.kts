plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatformModule)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}
