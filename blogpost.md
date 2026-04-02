Plugin Model v2 is now available as an experimental way to structure, package, and build plugins.
How do you create a fresh modular plugin based on this model?
Let's find out.

## Getting started

Use the [IntelliJ Platform Plugin Template](https://github.com/new?template_name=intellij-platform-plugin-template&template_owner=JetBrains) as the starting point for a fresh plugin repository.
After you open the repository in IntelliJ IDEA, remove all Kotlin source files, resource bundles, and XML files from test data.
Since the final plugin uses a different layout, keeping the template boilerplate only adds noise.

In Plugin Model v2, the `plugin.xml` does not contain any actions, extensions, or listeners.
Reduce this file to the bare minimum.

```xml
<idea-plugin>
    <id>org.jetbrains.plugins.template</id>
    <name>IntelliJ Platform Plugin Template</name>
    <vendor>JetBrains</vendor>
</idea-plugin>
```

Update the project coordinates.
In `settings.gradle.kts`, set the Gradle root project name.

```kotlin
rootProject.name = "mincssrel"
```

Update `gradle.properties` with the plugin coordinates.

```properties
pluginGroup = com.github.novotnyr.mincssrel
pluginName = Mini Modular CSS Showcase
pluginVersion = 1.0.0
```

Finally, update the plugin ID in `plugin.xml`.

```xml
<id>com.github.novotnyr.mincssrel</id>
```


## Creating the first plugin content module

In Plugin Model v2, a plugin consists of multiple _plugin content modules_, and in Gradle terms this maps to a [multi-project build](https://docs.gradle.org/current/userguide/multi_project_builds_intermediate.html) where each content module is a subproject.
Create the first plugin content module as `shared`, which will host your core functionality and shared resources.

In IntelliJ IDEA, add a new Gradle module with `mincssrel` as the parent and make sure `settings.gradle.kts` includes it as a subproject.

```kotlin
include("shared")
```

Every content module build script must apply the `org.jetbrains.intellij.platform.module` Gradle plugin.
Because the template uses a [Gradle version catalog](https://docs.gradle.org/current/userguide/version_catalogs.html), add this catalog alias in `gradle/libs.versions.toml`.

```toml
intelliJPlatformModule = { id = "org.jetbrains.intellij.platform.module" }
```

This alias reuses the same plugin version as the existing `intelliJPlatform` catalog item.
Then create `shared/build.gradle.kts` with the following content.

```kotlin
plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatformModule)
}

dependencies {
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
```

This setup applies both Kotlin Gradle plugin and the IntelliJ Platform Module Gradle plugin, and it declares a dependency on the IntelliJ IDEA SDK APIs.

Now add a dependency from the parent plugin module to `shared`.
In `build.gradle.kts`, add the following line to the `dependencies` block.

```kotlin
pluginModule(implementation(project(":shared")))
```

## Content module functionality

To showcase the `shared` module, create a simple action.

```kotlin
package com.github.novotnyr.mincssrel

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages

class SharedAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showInfoMessage("An action from a shared content module was invoked", "Mincssrel")
    }
}
```

## Plugin content module descriptors

Each plugin content module needs its own descriptor file. 
Contrary to the `plugin.xml`, such a descriptor belongs to the root of the classpath. 
The name of the descriptor file follows a naming convention:

1. It should start with the value of `rootProject.name` from the `settings.gradle.kts`.
2. Then, separated with a dot (`.`), follows the content module name.
2. It has a `.xml` extension.

Following this convention, create `shared/src/main/resources/mincssrel.shared.xml` and declare the action there.

```xml
<idea-plugin>
    <actions>
        <action id="com.github.novotnyr.mincssrel.SharedAction"
                class="com.github.novotnyr.mincssrel.SharedAction"
                text="Invoke Shared Mincssrel Action"
        />
    </actions>
</idea-plugin>
```

## Content module declarations

The plugin content module must be declared in the `plugin.xml` descriptor.

```xml
<content>
    <module name="mincssrel.shared"
            loading="required" />
</content>
```

The module name follows the same naming convention. 
Since it provides necessary shared behavior, provide a `loading="required"` to indicate that it is required for the plugin to work.

## Packaging and running

Build the plugin with the `buildPlugin` Gradle task.
The IntelliJ Platform Gradle Plugin packages content modules automatically, with each module as a separate JAR under `lib/modules`.

```text
mincssrel-1.0.0.zip
|- mincssrel/
   |- lib/
      |- mincssrel-1.0.0.jar
      |- modules/
         |- mincssrel.shared.jar
```

Run the plugin in the IDE with the `runIde` Gradle task, open _Search Everywhere_, and execute `Invoke Shared Mincssrel Action`.
This confirms that the required content module is loaded.

## Creating an optional content module

In Plugin Model v2, optional functionality belongs in a separate content module loaded only when all required dependencies are available.
Create a second content module named `css`, which depends on the bundled CSS plugin developed by JetBrains.

Add a new Gradle module with `mincssrel` as the parent and ensure `settings.gradle.kts` includes it as a subproject.

```kotlin
include("css")
```

Start `css/build.gradle.kts` with the same content as `shared/build.gradle.kts`.

```kotlin
plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatformModule)
}

dependencies {
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
```

## Content module metadata

The second plugin content module needs its own descriptor file. 
Again, follow the naming convention and create the `css/src/main/resources/mincssrel.css.xml` file.

```xml
<idea-plugin>
    <dependencies>
        <module name="mincssrel.shared" />
        <plugin id="com.intellij.css"/>
    </dependencies>
</idea-plugin>
```

This descriptor uses Plugin Model v2 syntax in `<dependencies>` to declare both a content module dependency and a plugin dependency.
The `<module>` element declares the dependency on the `mincssrel.shared` plugin content module, and the `<plugin>` element declares the dependency on the `com.intellij.css` plugin.
Both dependencies declared in this descriptor are mandatory for loading this content module.

Now declare this content module in the main `plugin.xml` descriptor.
In `src/main/resources/META-INF/plugin.xml`, add this line.

```xml
<module name="mincssrel.css" loading="optional" />
```

If any dependency for `mincssrel.css` is missing, this module will not load, but because it is marked optional, the rest of the plugin still works.
As an example, the `com.intellij.css` plugin is not available in IntelliJ IDEA 2025.3 without a subscription.
However, the situation changes in IntelliJ IDEA 2026.1, where this plugin is available even without a subscription, causing the `mincssrel.css` content module to be loaded.

## Content module build script

To align with template conventions, add this line to `gradle.properties`.

```properties
cssPlatformBundledPlugins = com.intellij.css
```

Because the `css` module depends on both `shared` plugin content module and the `com.intellij.css` bundled plugin, mirror those dependencies in `css/build.gradle.kts`.

```kotlin
implementation(project(":shared"))
intellijPlatform {
    bundledPlugins(providers.gradleProperty("cssPlatformBundledPlugins").map { it.split(',') })
}
```

Finally, register the `css` subproject in `build.gradle.kts`.

```kotlin
pluginModule(implementation(project(":css")))
```

## Showcasing the CSS content module

To showcase optional functionality, add a minimal action to the `css` module.
Create `css/src/main/kotlin/com/github/novotnyr/mincssrel/css/CssAction.kt`.

```kotlin
package com.github.novotnyr.mincssrel.css

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages

class CssAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showInfoMessage("An action from a CSS content module was invoked", "Mincssrel")
    }
}
```

Declare this action in the `css` plugin content module descriptor, specifically, in the `mincssrel.css.xml`.

```xml
<actions>
    <action id="com.github.novotnyr.mincssrel.css.CssAction"
            class="com.github.novotnyr.mincssrel.css.CssAction"
            text="Invoke CSS Action"
    />
</actions>
```

Run the plugin in IntelliJ IDEA 2026.1, use _Search Everywhere_, and verify that *Invoke CSS Action* is available.

## Streamlining the setup

You can streamline the Gradle setup to make the build more opinionated and easier to maintain.
A practical consolidation strategy is to move repositories to `settings.gradle.kts`, apply shared plugins in a single place, and declare shared toolchain and IntelliJ dependencies in one place.

## Declare repositories in Gradle settings

In `settings.gradle.kts`, add this line.

```kotlin
id("org.jetbrains.intellij.platform.settings") version "2.13.1"
```

Then adjust the version catalog, because the Settings plugin version is now managed in Gradle settings.
In `gradle/libs.versions.toml`, remove the version from `intelliJPlatform`.

```toml
intelliJPlatform = { id = "org.jetbrains.intellij.platform" }
```

Now centralize repository declarations in `settings.gradle.kts`.

```kotlin
import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        mavenCentral()
        intellijPlatform {
            defaultRepositories()
        }
    }
}
```

After this change, you can remove `repositories` blocks from all three `build.gradle.kts` files.

## Consolidating Gradle plugin declaration

Because every content module applies the Kotlin and IntelliJ Platform Module plugins, centralize that configuration in the parent build script.
In the top-level `build.gradle.kts`, add the following block.

```kotlin
subprojects {
    plugins.apply("org.jetbrains.kotlin.jvm")
    plugins.apply("org.jetbrains.intellij.platform.module")
}
```

Use explicit plugin IDs here, because version-catalog aliases are not available in this context.
After this change, remove `plugins` blocks from both content module build scripts.

## Consolidating Kotlin toolchain and dependencies

As a final consolidation step, move the repeated toolchain and base IntelliJ dependency declarations to a shared block.
In the root `build.gradle.kts`, remove the root `kotlin` block.
In the root `build.gradle.kts`, remove `intellijIdea`, `bundledPlugins`, `plugins`, and `bundledModules` from the root `intellijPlatform` block.
Then add an `allprojects` block with shared declarations for all Gradle modules.

```kotlin
allprojects {
    kotlin {
        jvmToolchain(21)
    }
    dependencies {
        intellijPlatform {
            intellijIdea(providers.gradleProperty("platformVersion"))
        }
    }
}
```

After these optimizations, `shared/build.gradle.kts` becomes empty and can be removed.
At that point, the core build setup can be reduced to `settings.gradle.kts`, `build.gradle.kts`, and `css/build.gradle.kts`.
The `css` build script is then reduced to a just a dependency declaration.

```kotlin
dependencies {
    implementation(project(":shared"))
    intellijPlatform {
        bundledPlugins(providers.gradleProperty("cssPlatformBundledPlugins").map { it.split(',') })
    }
}
```

## Packaging the two-content-module plugin

The final plugin artifact packages both content modules in the correct location.

```text
mincssrel-1.0.0.zip
|- mincssrel/
   |- lib/
      |- mincssrel-1.0.0.jar
      |- modules/
         |- mincssrel.shared.jar
         |- mincssrel.css.jar
```

## Notes for expansion

Add a PSI-based example for the CSS module.
Add a section about DevKit support for `plugin.xml` completion in module descriptors.
Add a section about descriptor reload behavior during development.
