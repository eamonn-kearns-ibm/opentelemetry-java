val taasArtifactoryUrl = System.getenv("TAAS_ARTIFACTORY_URL")
val googleArtifactoryUrl = System.getenv("GOOGLE_TAAS_ARTIFACTORY_URL")
val taasUsername = System.getenv("TAAS_ARTIFACTORY_USERNAME")
val taasPassword = System.getenv("TAAS_ARTIFACTORY_PASSWORD")

// a function to generate the maven repository configurations for the replacements for maven central
// and for google's maven repository, using the environment variables for the URLs and credentials
fun RepositoryHandler.configureArtifactories() {
  maven {
    url = uri(taasArtifactoryUrl)
    credentials {
      username = taasUsername
      password = taasPassword
    }
    // this is how we explicitly exclude the ones that we know come from google.
    content {
      excludeGroup("com.android.tools")
      excludeGroup("androidx.annotation")
    }
  }
  maven {
    url = uri(googleArtifactoryUrl)
    credentials {
      username = taasUsername
      password = taasPassword
    }
  }
}

gradle.projectsLoaded {
  allprojects {
    extra["apiBaseVersion"] = System.getenv("API_BASE_VERSION")

    repositories {
      mavenLocal()
      configureArtifactories()
    }

    buildscript {
      repositories {
        mavenLocal()
        configureArtifactories()
      }
    }

    plugins.withId("java") {
      dependencies {
        add("testImplementation", "com.ibm.instana.otel:otel-docker-image-name-substitutor:1.0.0")
        add("testRuntimeOnly", "com.ibm.instana.otel:otel-docker-image-name-substitutor:1.0.0")
      }
    }
  }
}
