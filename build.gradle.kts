modtype = LIB

apis(
  libs.kotlinx.serialization.json
)

implementations(
  projects.k.kjlib.lang,
  projects.k.stream,
  projects.k.file,
  projects.k.async,
  projects.k.reflect
)


plugins {
  kotlin("plugin.serialization")
}