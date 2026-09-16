# Learned Scorer V1 closure

Closure executed with `/usr/lib/jvm/java-8-openjdk-arm64` and Gradle 4.4.1.
The project exposes no `reobf` task; `tasks` was checked before running the
supported `test jar benchmarkFoundation` flow.

* Java tests: 295, failures 0, errors 0, skipped 0
* Gradle build tasks: PASS (`test`, `jar`, `benchmarkFoundation`)
* Final JAR: `director/build/libs/SobrenaturalDirector-0.12.0-alpha.jar`
* JAR size: 577978 bytes
* JAR SHA-256: `7ce917d1bb50b670c100c9c324e5bec20aaafa9b45feb859b640b27fcf534e3f`
* Class major version: 52 (Java 8)
* Benchmark classes in production JAR: none
* Python files in production JAR: none

The build was updated to exclude the offline benchmark package from the
production artifact. Benchmark classes remain available to Java tests and the
`benchmarkFoundation` JavaExec task. No production decision behavior was
changed.
