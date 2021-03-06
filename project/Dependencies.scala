import sbt._
import sbt.Keys._
import play.Play.autoImport._

object Dependencies {

  val resolverSettings = {
    // Use in-house Maven repo instead of Maven central if env var is set
    sys.env.get("INHOUSE_MAVEN_REPO").fold[Seq[sbt.Def.Setting[_]]] {
      Seq(
        resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
        resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
      )
    } { inhouse =>
      Seq(
        // Speed up resolution using local Maven cache
        resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository",
        resolvers += "Inhouse" at inhouse,
        externalResolvers := Resolver.withDefaultResolvers(resolvers.value, mavenCentral = false)
      )
    }
  }

  val thePlayVersion = play.core.PlayVersion.current
  val slf4jVersion = "1.7.7"
  val hystrixVersion = "1.3.19"
  val httpClientVersion = "4.3.6"
  val scalikejdbcVersion = "2.2.1"
  val swaggerVersion = "1.3.12"
  val jacksonVersion = "2.4.4"

  // Logging
  val logbackClassic      = "ch.qos.logback"            % "logback-classic"               % "1.1.2"
  val slf4jApi            = "org.slf4j"                 % "slf4j-api"                     % slf4jVersion
  val jclOverSlf4j        = "org.slf4j"                 % "jcl-over-slf4j"                % slf4jVersion
  val log4jOverSlf4j      = "org.slf4j"                 % "log4j-over-slf4j"              % slf4jVersion
  val julToSlf4j          = "org.slf4j"                 % "jul-to-slf4j"                  % slf4jVersion
  val ravenLogback        = "net.kencochrane.raven"     % "raven-logback"                 % "5.0.1"   % Runtime
  val janino              = "org.codehaus.janino"       % "janino"                        % "2.7.7"
  val ltsvLogger          = "com.beachape"              %% "ltsv-logger"                  % "0.0.8"

  // Hystrix
  val hystrixCore         = "com.netflix.hystrix"       % "hystrix-core"                  % hystrixVersion
  val hystrixStream       = "com.netflix.hystrix"       % "hystrix-metrics-event-stream"  % hystrixVersion
  val rxJavaScala         = "com.netflix.rxjava"        % "rxjava-scala"                  % "0.20.3" // matches version used in hystrix-core

  // HTTP clients
  val asyncHttpClient     = "com.ning"                  % "async-http-client"             % "1.9.3"
  val httpClient          = "org.apache.httpcomponents" % "httpclient"                    % httpClientVersion
  val httpClientCache     = "org.apache.httpcomponents" % "httpclient-cache"              % httpClientVersion
  val metricsHttpClient   = "io.dropwizard.metrics"     % "metrics-httpclient"            % "3.1.0"

  // DB
  val postgres            = "org.postgresql"            % "postgresql"                    % "9.3-1102-jdbc41"   % Runtime
  val skinnyOrm           = "org.skinny-framework"      %% "skinny-orm"                   % "1.3.8"
  val scalikeJdbc         = "org.scalikejdbc"           %% "scalikejdbc"                  % scalikejdbcVersion
  val scalikeJdbcPlay     = "org.scalikejdbc"           %% "scalikejdbc-play-plugin"      % "2.3.4"
  val dbcp2               = "org.apache.commons"        % "commons-dbcp2"                 % "2.0.1"

  // Memcached
  val shade               = "com.bionicspirit"          %% "shade"                        % "1.6.0"
  val spyMemcached        = "net.spy"                   % "spymemcached"                  % "2.11.5"

  // Play plugins
  val playFlyway          = "com.github.tototoshi"      %% "play-flyway"                  % "1.2.0"
  val scaldiPlay          = "org.scaldi"                %% "scaldi-play"                  % "0.4.1"
  val metricsPlay         = "com.kenshoo"               %% "metrics-play"                 % "2.3.0_0.1.7"
  val providedPlay        = "com.typesafe.play"         %% "play"                         % thePlayVersion      % Provided

  // Swagger
  val swaggerPlay         = "com.wordnik"               %% "swagger-play2"                % swaggerVersion
  val swaggerAnnotations  = "com.wordnik"               % "swagger-annotations"           % swaggerVersion

  // Jackson
  val jacksonCore         = "com.fasterxml.jackson.core"   % "jackson-core"               % jacksonVersion
  val jacksonScala        = "com.fasterxml.jackson.module" %% "jackson-module-scala"      % jacksonVersion
  val jacksonDatabind     = "com.fasterxml.jackson.core"   % "jackson-databind"           % jacksonVersion

  // Test
  val playTest            = "com.typesafe.play"         %% "play-test"                    % thePlayVersion      % Test
  val scalatest           = "org.scalatest"             %% "scalatest"                    % "2.2.3"             % Test
  val scalatestPlay       = "org.scalatestplus"         %% "play"                         % "1.2.0"             % Test
  val scalacheck          = "org.scalacheck"            %% "scalacheck"                   % "1.12.1"            % Test
  val groovy              = "org.codehaus.groovy"       % "groovy"                        % "2.3.9"             % Test
  val scalikeJdbcTest     = "org.scalikejdbc"           %% "scalikejdbc-test"             % scalikejdbcVersion  % Test

  // Misc utils
  val commonsValidator    = "commons-validator"         % "commons-validator"             % "1.4.0"             % Runtime
  val jta                 = "javax.transaction"         % "jta"                           % "1.1"
  val scalaUri            = "com.netaporter"            %% "scala-uri"                    % "0.4.4"
  val findbugs            = "com.google.code.findbugs"  % "jsr305"                        % "3.0.0"

  val withoutExcluded = { (m: ModuleID) =>
    m.excludeAll(
      ExclusionRule(organization = "spy", name = "spymemcached"), // spymemcached's org changed from spy to net.spy
      ExclusionRule(organization = "org.slf4j", name = "slf4j-log4j12"),
      ExclusionRule(organization = "org.slf4j", name = "slf4j-jdk14"),
      ExclusionRule(organization = "org.slf4j", name = "slf4j-jcl"),
      ExclusionRule(organization = "org.slf4j", name = "slf4j-nop"),
      ExclusionRule(organization = "org.slf4j", name = "slf4j-simple"))}

  val rootDependencies = Seq(
    // Logging
    logbackClassic,
    slf4jApi,
    jclOverSlf4j,
    log4jOverSlf4j,
    julToSlf4j,
    ravenLogback,
    janino,
    ltsvLogger,

    //Hystrix
    hystrixCore,
    hystrixStream,
    rxJavaScala,

    // Apache HTTP client
    httpClient,
    httpClientCache,
    metricsHttpClient,

    // DB
    postgres,
    skinnyOrm,
    scalikeJdbc,
    scalikeJdbcPlay,
    dbcp2,

    // Memcached
    shade,
    spyMemcached,

    // Misc utils
    commonsValidator,
    jta,
    scalaUri,

    // Play plugins
    playFlyway,
    scaldiPlay,
    metricsPlay,
    swaggerPlay,

    // Test
    playTest,
    scalatest,
    scalatestPlay,
    scalacheck,
    groovy,
    scalikeJdbcTest
  ).map(withoutExcluded)

  val playScalatestDependencies = Seq(
    scalatest,
    scalatestPlay
  )

  val authPluginDependencies = Seq(
    providedPlay,
    ltsvLogger
  )

  val modelsDependencies = Seq(
    swaggerAnnotations intransitive(),
    jacksonCore intransitive(),
    jacksonScala intransitive()
  )

  val javaClientDependncies = Seq(
    findbugs intransitive(),
    slf4jApi,
    asyncHttpClient,
    jacksonCore,
    jacksonScala,
    jacksonDatabind,
    logbackClassic % Test,
    jclOverSlf4j intransitive(),
    log4jOverSlf4j intransitive(),
    julToSlf4j intransitive(),
    scalatest
  )

  // TODO when bumping to Play 2.4, depend on play-json instead of ws
  val playJsonFormatsDependencies = playScalatestDependencies :+ ws

  val scalaWsClientDependencies = playScalatestDependencies :+ ws
}