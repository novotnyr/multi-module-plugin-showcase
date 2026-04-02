Plugin Model v2 is now available as an experimental way to structure, package and build plugins. How to create a fresh modular plugin based on this model? Let's find out!

We will use a template built on IntelliJ Platform Gradle Plugin, adapt it and create two plugin modules: one core module and another one that optionally depends on a bundled CSS plugin.

## Getting started

Use the [IntelliJ Platform Plugin Template](https://github.com/new?template_name=intellij-platform-plugin-template&template_owner=JetBrains) as a starting point for a fresh plugin Git repository.

After opening the repository in IntelliJ IDEA, remove all Kotlin files, resource bundles, erase all extensions in the `plugin.xml` plugin descriptor, and delete all XML files in test data. The final plugin will have a completely different directory layout, anyways.

The `plugin.xml` will boil down to the bare-bones structure:
```xml
<idea-plugin>
    <id>org.jetbrains.plugins.template</id>
    <name>IntelliJ Platform Plugin Template</name>
    <vendor>JetBrains</vendor>
</idea-plugin>
```

Now, change the project coordinates.

In `settings.gradle.kts`, modify the Gradle project name:
```properties
rootProject.name = "mincssrel"
```

Change `gradle.properties`: 
```properties
pluginGroup = com.github.novotnyr.mincssrel
pluginName = Mini Modular CSS Showcase
pluginVersion = 1.0.0
```

And finally, in `plugin.xml`, change the plugin ID.
```xml
<id>com.github.novotnyr.mincssrel</id>
```

## Creating a first content module

In Plugin Model v2, a plugin consists of multiple _content modules_. In Gradle, such plugin is a [multi-project build](https://docs.gradle.org/current/userguide/multi_project_builds_intermediate.html), where each content module maps to a Gradle subproject.

The primary content module will be named `shared`. It will contain the plugin core functionality and shared resources. It is the foundation upon which other content modules can be built, allowing for modular and maintainable plugin development.

In IntelliJ IDEA, add a new Gradle module with `mincssrel` as a parent.

Make sure that `settings.gradle.kts` contains the following directive:

```kotlin
include("shared")
```

Any Gradle build script for content module needs to declare the `org.jetbrains.intellij.platform.module` Gradle plugin.
Since the plugin template uses [Gradle version catalog](https://docs.gradle.org/current/userguide/version_catalogs.html), declare the necessary catalog item.
In `gradle/libs.versions.toml`, add the following lines:
```
intelliJPlatformModule = { id = "org.jetbrains.intellij.platform.module" }
```

The version for this catalog item will be shared with the the `intelliJPlatform ` catalog item.

The Gradle build script for the `shared` module, `shared/build.gradle.kts` should look like this:

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

The content module uses the Kotlin Gradle plugin along with the IntelliJ PLatform Module Gradle plugin.
Furthermore, it declares a dependency on the IntelliJ IDEA SDK, as most probably it will need to access the basic APIs in this IDE. Such IDE will be downloaded from the corresponding repository declared in the `intellijPlatform ` [dependency extension](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html).

Finally, add the dependency between the plugin descriptor module and the `shared` content module.

In `build.gradle.kts`, add the following lines:
```kotlin
pluginModule(implementation(project(":shared")))
```

## Content module content

To showcase the shared functionality, create a new action.

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

## Content module descriptors

Each content module needs its own descriptor that must follow a naming convention. 

It is prefixed with the name of the Gradle project from the `rootProject.name` in the `settings.gradle.kts` file. Then, the content module name is appended to the file name, with `.xml` suffix.

As a consequence, create
a file `shared/src/main/resources/mincssrel.shared.xml` and declare the action in it:
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

Having a content module ready, declare it in the main plugin descriptor `plugin.xml`:

```
<content>
    <module name="mincssrel.shared" 
            loading="required" />
</content>
```

The name must follow the naming convention described above. 
Additionally, as the shared functionality content module is a prerequisite for the plugin to work, the `loading` attribute must be set to `required`.

## Packaging and running

The plugin can be packaged and run in the IDE. 
Use the `buildPlugin` Gradle task to build the plugin.

The IntelliJ Platform plugin will automatically handle the correct packaging of content modules. Each content module needs to be packaged separately, into a JAR with a name following the convention, and put into the `modules` directory.

```
mincssrel-1.0.0.zip
|-mincssrel/
  |-lib/
    |-mincssrel-1.0.0.jar
    |-modules/
      |-mincssrel.shared.jar
```

Run the plugin in the IDE via the `runIde` Gradle task.
Invoke the Search Everywhere action and search for the `Invoke Shared Mincssrel Action`.

This is an action invoked from the required content module.

## Creating an optional content module

In Plugin Model v2, an optional functionality belongs to a separate content module which will be loaded only when all the necessary dependencies are available.

Let's create a second content module, `mincssrel.css` that will depend on a bundled CSS plugin. 

Again, add a new Gradle module with `mincssrel` as a parent and make sure that the `settings.gradle.kts` includes this new Gradle module:

```kotlin
include("css")
```

The build script for the `css` module (`css/build.gradle.kts`) will be initially identical to the build script for the `shared` module.

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

In the spirit of the shared content module, create a new descriptor file `css/src/main/resources/mincssrel.css.xml`.

```
<idea-plugin>
    <dependencies>
        <module name="mincssrel.shared" />
        <plugin id="com.intellij.css"/>
    </dependencies>
</idea-plugin>
```

The `css` content declares two dependencies, by using the Plugin Module v2 syntax in the `<dependencies>` element. 

1. The `css` content module requires a `mincssrel.shared` content module funcionality. The dependency on another content module is expressed by the `<module>` element.
2. The `css` content module requires the CSS plugin provided by JetBrains. The dependency on a plugin is expressed by the `<plugin>` element.

All such dependencies are mandatory.

Now in turn, declare this content module in the plugin descriptor `plugin.xml`:

In `src/main/resources/META-INF/plugin.xml`, add the following lines:
```
<module name="mincssrel.css" loading="optional" />
```

If any of the `mincssrel.css` content module dependencies are not available, the content module will not be loaded. 
However, by declaring this module as optional, the plugin will still work even if the `com.intellij.css` plugin is not installed.

As an example, the `com.intellij.css` plugin is not available in IntelliJ IDEA 2025.3 without a subscription.
However, the situation changes in IntelliJ IDEA 2026.1, where this plugin is available even without a subscription, causing the `mincssrel.css` content module to be loaded.

## Content module build script

To be consistent with the plugin template conventions, add the following line to the `gradle.properties`.

```properties
cssPlatformBundledPlugins = com.intellij.css
```

Since the `css` content module depends on the `com.intellij.css` plugin and on the `shared` module, align the dependencies in the Gradle build script with the dependencies in the content module descriptor.

In `css/build.gradle.kts`, add the following lines:
```kotlin
implementation(project(":shared"))
intellijPlatform {
    bundledPlugins(providers.gradleProperty("cssPlatformBundledPlugins").map { it.split(',') })
}
```

As a last step, declare the Gradle subproject in `build.gradle.kts`:
```kotlin
pluginModule(implementation(project(":css")))
```

## Showcasing a CSS content module

To showcase the optional functionality, add a minimalistic action to the `css` content module.

Create a bare-bones `css/src/main/kotlin/com/github/novotnyr/mincssrel/css/CssAction.kt`.

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

Declare this action in the `mincssrel.css.xml` descriptor.

```xml
<actions>
    <action id="com.github.novotnyr.mincssrel.css.CssAction"
            class="com.github.novotnyr.mincssrel.css.CssAction"
            text="Invoke CSS Action"
    />
</actions>
```

Now, if you run the plugin in the IntelliJ IDEA 2026.1 that bundles the `com.intellij.css` and use the _Search Everywhere_ action, see that the *Invoke CSS Action* becomes be available.

%TODO showcase PSI

# Streamlining the setup

The Gradle build script setup can be streamlined, leading to more opinionated and simple code.

1. Declare the repositories in the `settings.gradle.kts` file.
2. Apply Kotlin and IntelliJ Platform Module plugin in a central place. 
3. Declare Kotlin JVM toolchain and IntelliJ Platform dependencies in a central place as well.

## Declare repositories in Gradle Settings

In `settings.gradle.kts`, add the following lines:
```
id("org.jetbrains.intellij.platform.settings") version "2.13.1"
```
Immediately, we need to adjust the version catalog, as the _Settings_ plugin with a specific version is in conflict with catalog items.

In `gradle/libs.versions.toml`, remove the version from `intelliJPlatform`, as this is now specified in the Gradle Settings.

```
intelliJPlatform = { id = "org.jetbrains.intellij.platform" }
```

Let's centralize the repositories declaration into `settings.gradle.kts`.

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

The `repositories` block is now no longer needed in any of the three `build.gradle.kts` files.

## Consolidating Gradle plugin declaration

Since every content module build script declares a dependency on Kotlin Gradle Plugin and IntelliJ Platform Module Gradle plugin, we can consolidate the declaration in the main build script.

In `build.gradle.kts`, add the following lines:
```kotlin
subprojects {
    plugins.apply("org.jetbrains.kotlin.jvm")
    plugins.apply("org.jetbrains.intellij.platform.module")
}
```
Due to the technical limitations of Gradle, we cannot use the version catalog here. 
Instead, we use the explicit Gradle plugin identifiers.

%TODO reload

Now, the `plugins` block can be removed from both build scripts in two content modules.

## Consolidating Kotlin toolchain and dependencies

As a final stage of consolidation, reuse the dependencies declaration along with Kotlin JVM toolchain. They are repeated in plugin build script, and two content modules build scripts.

In the main `build.gradle.kts`, remove the `kotlin` block. Additionally, remove the `intellijIdea`, `bundledPlugins`, `plugins` and `bundledModules` declaration from `intellijPlatform` block. From now on, all dependencies are declared in the corresponding content modules.

Instead of these, add an `allproject` block and declare dependencies and IntelliJ IDEA dependency consistently for all three Gradle modules.

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

And that's it! 
After these optimizations, we end up with an empty build script for the `shared` module. We can even remove it, boiling project down to three files:

- `settings.gradle.kts`
- `build.gradle.kts`
- `css/build.gradle.kts`.

Even the build script for the `css` module is now reduced to simple dependency declaration.

```kotlin
dependencies {
    implementation(project(":shared"))
    intellijPlatform {
        bundledPlugins(providers.gradleProperty("cssPlatformBundledPlugins").map { it.split(',') })
    }
}
```

# Packaging the two-content-module plugin

The final plugin artifact now properly packages both modules in the correct places.

```
mincssrel-1.0.0.zip
|-mincssrel/
  |-lib/
    |-mincssrel-1.0.0.jar
    |-modules/
      |-mincssrel.shared.jar
      |-mincssrel.css.jar
```

% TODO devkit autocomplete
% TODO reload