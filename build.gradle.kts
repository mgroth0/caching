modtype = LIB

dependencies {
  implementation(projects.k.kjlib.lang)
  implementation(projects.k.kjlib)
  implementation(projects.k.async)
  api(libs.kotlinx.serialization.json)
  implementation(projects.k.reflect)
}

plugins {
  kotlin("plugin.serialization")
}