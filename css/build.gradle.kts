dependencies {
    implementation(project(":shared"))
    intellijPlatform {
        bundledPlugins(providers.gradleProperty("cssPlatformBundledPlugins").map { it.split(',') })
    }
}

