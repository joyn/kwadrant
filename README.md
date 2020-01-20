# Kwadrant

## Gradle Project Multi Module Optimization Tool

#### **How to build and deploy locally**

1. Add a `local.properties` file in the root directory of this project
2. Specify a directory path in the target project and add it as `deploy.target.dir` property in the `local.properties` file
3. Run the `deploy` task
4. Add `apply plugin: 'de.joyn.kwadrant'` in the target project top level `build.gradle`
5. Add `classpath files("${deploy_target_dir_path}/kwadrant-${kwadrant_version}.jar")` (current version is `0.1`) in the target project top level `build.gradle` under dependencies
6. Run the `deploy` task which builds the project and copies the resulting `jar` into the target path in the target project

#### **Tasks**

- Run `./gradlew kwUnusedLibs` to find potentially unused dependencies in your modules
- Run `./gradlew kwDuplicatedLibs` to find library dependency candidates which could be replaced in a top module by using the `api` directive
- Run `./gradlew kwDuplicatedParents` to find module dependency candidates which could be replaced in a top module by using the `api` directive`
- Run `./gradlew kwMisalignedDeps` to find dependencies used in several modules with different version numbers

