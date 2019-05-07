lazy val `kleisli-in-30-minutes` = (project in file("."))
  .settings(		
		organization := "ru.dokwork",
		scalaVersion := "2.12.8",
		scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Ypartial-unification"),
		libraryDependencies ++= Seq(
			"org.typelevel" %% "cats-core" % "1.5.0",
			// tests:
			"org.scalatest" %% "scalatest" % "3.0.0" % "test"
		)
 )
	.settings(
    coverageMinimum := 90,
    coverageFailOnMinimum := true
  )

