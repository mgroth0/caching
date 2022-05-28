dependencies {
  implementation(projects.kj.kjlib.lang)
  implementation(projects.kj.kjlib)
  implementation(projects.kj.async)
  api(libs.kotlinx.serialization.json)
}

plugins {
  kotlin("plugin.serialization")
}