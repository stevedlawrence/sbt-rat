import xerial.sbt.Sonatype.GitHubHosting
import ReleaseTransformations._

lazy val metadataSettings = Seq(
  name := "SBT Release Audit Tool",
  organization := "org.musigma",
  moduleName := "sbt-rat",
  licenses := Seq("Apache License, Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  developers := List(
    Developer("jvz", "Matt Sicker", "mattsicker@apache.org", url("https://musigma.blog/"))
  ),
  scmInfo := Some(ScmInfo(url("https://github.com/jvz/sbt-rat"), "scm:git:git@github.com:jvz/sbt-rat.git")),
  homepage := Some(url("https://github.com/jvz/sbt-rat"))
)

lazy val buildSettings = Seq(
  sbtPlugin := true,
  crossSbtVersions := Seq("0.13.17", "1.1.2"),
  libraryDependencies += "org.apache.rat" % "apache-rat-core" % "0.13",
  scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
  initialCommands in console := "import org.musigma.sbt.rat._"
)

lazy val signatureSettings = Seq(
  pgpSecretRing := {
    val old = pgpSecretRing.value
    val travis = file("travis/secring.gpg")
    if (travis.exists()) travis else old
  },
  usePgpKeyHex("BCC60587F2C9062CE016"),
  pgpPassphrase := sys.env.get("PGP_PASS").map(_.toCharArray)
)

lazy val releaseSettings = Seq(
  credentials ++= {
    for {
      user <- sys.env.get("SONATYPE_USER")
      pass <- sys.env.get("SONATYPE_PASS")
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)
  }.toList,
  publishMavenStyle := true,
  publishTo := sonatypePublishTo.value,
  sonatypeProjectHosting := Some(GitHubHosting("jvz", "sbt-rat", "Matt Sicker", "mattsicker@apache.org")),
  releaseProcess := Seq(
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    releaseStepCommandAndRemaining("^scripted"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("^publishSigned"),
    releaseStepCommand("sonatypeReleaseAll"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

lazy val packageSettings = for (task <- Seq(packageBin, packageSrc, packageDoc))
  yield mappings in (Compile, task) ++= Seq(
    (baseDirectory.value / "LICENSE", "META-INF/LICENSE"),
    (baseDirectory.value / "NOTICE", "META-INF/NOTICE")
  )

lazy val root = (project in file("."))
  .settings(metadataSettings: _*)
  .settings(buildSettings: _*)
  .settings(signatureSettings: _*)
  .settings(releaseSettings: _*)
  .settings(packageSettings: _*)
