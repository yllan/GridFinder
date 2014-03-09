name := """GridFinder"""

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
)

resolvers += "spray repo" at "http://repo.spray.io"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

Seq(
  scalaSource in Compile <<= baseDirectory / "src",
  sourceDirectory in Compile <<= baseDirectory / "src",
  scalaSource in Test <<= baseDirectory / "test",
  sourceDirectory in Test <<= baseDirectory / "test"
)