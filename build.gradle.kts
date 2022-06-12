modtype = LIB

dependencies {
  implementation(projects.kj.kjlib.kjlibLang)
  implementation(projects.kj.kjlib)
  implementation(projects.kj.async)
  api(libs.kotlinx.serialization.json)
  implementation(projects.kj.reflect)
}

plugins {
  kotlin("plugin.serialization")
}