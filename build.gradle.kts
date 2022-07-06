apis(
  libs.kotlinx.serialization.json
)

implementations {
  kjlibLang
  stream
  file
  async
  reflect
}


plugins {
  kotlin("plugin.serialization")
}