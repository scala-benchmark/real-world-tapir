import sbt._

object Dependencies {
  val tapirVersion  = "1.10.7"
  val doobieVersion = "1.0.0-RC5"
  // Couldn't update to 10.11.0, as I was getting errors in compile time
  val flywayVersion = "10.9.0"

  val tapir = Seq(
    "ch.qos.logback"               % "logback-classic"          % "1.4.11",
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-sttp-client"        % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % tapirVersion,
    "org.http4s"                  %% "http4s-ember-server"      % "0.23.27",
  )
  val compileDeps = tapir ++ Seq(
    "com.github.jwt-scala"       %% "jwt-circe"                   % "10.0.1",
    "com.github.pureconfig"      %% "pureconfig-core"             % "0.17.6",
    "com.typesafe.scala-logging" %% "scala-logging"               % "3.9.5",
    "org.flywaydb"                % "flyway-core"                 % flywayVersion,
    "org.flywaydb"                % "flyway-database-postgresql"  % flywayVersion,
    "org.tpolecat"               %% "doobie-core"                 % doobieVersion,
    "org.tpolecat"               %% "doobie-hikari"               % doobieVersion,
    "org.tpolecat"               %% "doobie-postgres"             % doobieVersion,
    // CWE dependencies
    "com.github.pathikrit"       %% "better-files"                % "3.9.2",
    "org.scalikejdbc"            %% "scalikejdbc"                 % "4.3.2",
    "com.lihaoyi"                %% "scalatags"                   % "0.13.1",
    "org.scala-lang"              % "scala-compiler"              % "2.13.14",
    "org.scala-lang"              % "scala-reflect"               % "2.13.14",
    ("com.typesafe.akka"          %% "akka-actor-typed"            % "2.8.8").cross(CrossVersion.for3Use2_13),
    ("com.typesafe.akka"          %% "akka-serialization-jackson"  % "2.8.8").cross(CrossVersion.for3Use2_13),
    "com.unboundid"                % "unboundid-ldapsdk"           % "7.0.1",
    "org.mvel"                     % "mvel2"                       % "2.5.2.Final",
    "org.springframework"          % "spring-expression"           % "6.1.14",
  )

  val testDeps = Seq(
    "com.dimafeng"  %% "testcontainers-scala-postgresql" % "0.41.3"    % Test,
    "com.dimafeng"  %% "testcontainers-scala-munit"      % "0.41.3"    % Test,
    "org.scalameta" %% "munit"                           % "1.0.0-M10" % Test,
    "org.typelevel" %% "munit-cats-effect"               % "2.0.0-M3"  % Test,
  )
}
