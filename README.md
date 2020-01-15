# Kwadrant

## A Gradle project build optimization tool

#### **How to build locally**

- Add a `local.properties` file in the root directory of this project
- Specify a directory path in the target project and add it as `deploy.target.dir` property in the `local.properties` file
- Run the `deploy` task
- Add `apply plugin: 'de.joyn.kwadrant'` in the target project top level `build.gradle`
- Add `classpath files("./libs/kwadrant-$kwadrant_version.jar")` (current version is `0.1`) in the target project top level `build.gradle` under dependencies

#### **Tasks**

- Run `./gradlew kwUnusedLibs` to find potentially unused dependencies in your modules
- Run `./gradlew kwDuplicatedLibs` to find library dependency candidates which could be replaced in a top module by using the `api` directive
- Run `./gradlew kwDuplicatedParents` to find module dependency candidates which could be replaced in a top module by using the `api` directive`
- Run `./gradlew kwMisalignedDeps` to find dependencies used in several modules with different version numbers

