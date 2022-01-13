import org.gradle.api.tasks.scala.ScalaCompile

plugins {
    scala
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.softwaremill.sttp.client3:core_2.13:3.3.18")
    implementation("com.softwaremill.sttp.client3:circe_2.13:3.3.18")
    implementation("io.circe:circe-core_2.13:0.15.0-M1")
    implementation("io.circe:circe-generic_2.13:0.15.0-M1")
    implementation("joda-time:joda-time:2.10.13")
}

val scalaCompile = tasks.withType<ScalaCompile> {
    targetCompatibility = "13"
}