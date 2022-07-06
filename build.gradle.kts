println("eval caching 1: ")

modtype = LIB

apis(
  libs.kotlinx.serialization.json
)

implementations(
  projects.k.kjlib.lang,
  projects.k.stream,
  ":k:file".jvm(),
  projects.k.async,
  projects.k.reflect
)


plugins {
  kotlin("plugin.serialization")
}