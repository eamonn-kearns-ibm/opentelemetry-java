# OtelDockerImageNameSubstitutor

This class arises from the need to import Docker Images as part of the OpenTelemetry build process, and the need to map all dockerhub and ghcr images to a single namespace under IBM control.

## Building
`./gradlew build` will build the class. The system variables `TAAS_ARTIFACTORY_USERNAME`, `TAAS_ARTIFACTORY_PASSWORD`, and `TAAS_DEPENDENCIES_URL` must either be found in the environment or defined in a local.properties file. local.properties has an entry in .gitignore to prevent secrets being published to a repository.


## Namespace
com.ibm.instana.otel.testcontainer

## Source path
File paths follow Maven/Gradle convention. So the class file can be found in [lib/src/main/java/com/ibm/instana/otel/testcontainer/OtelImageNameSubstitutor.java](lib/src/main/java/com/ibm/instana/otel/testcontainer/OtelImageNameSubstitutor.java) and the Unit Tests can be found in [lib/src/test/java/com/ibm/instana/otel/testcontainer/OtelImageNameSubstitutorTest.java](lib/src/test/java/com/ibm/instana/otel/testcontainer/OtelImageNameSubstitutorTest.java)

## Environment variable

It requires an environment variable INTERNAL_REGISTRY to have been set which it binds to a `final` class property `internalRegistry`. Setting this to an empty string will cause an IllegalStateException to be thrown. If this field has a trailing / an IllegalStateException will be thrown. The value defaults to `us.icr.io/instana-tracer-otel`


## Behaviour

This class leverages TestContainer's DockerImageName class to rewrite the coordinates of any images passed through it as part of the testcontainer lifecycle. The class will flatten the image, that is: the apply method removes the repository part of the Image's coordinate and returns a new coordinate in the form {internalRegistry}/{repository}:{version}. If no version is provided, the underlying DockerImageName class will provide latest, so the following behaviours will happen. As a projection of requirements, sha is removed. Since the sha may well be an index sha (which we know it is in the case of open telemetry's collector contrib, from investigating it) we cannot guarantee that it will match. As such, a design decision was made that we drop the sha.

This code, extracted from the runSubstitutorAgainstBatch Unit Test, shows a fairly comprehensive set of rewrite behaviours

```java
class Substitution {
    public String original;
    public String replacement;

    public Substitution(String original, String replacement) {
        this.original = original.trim();
        this.replacement = replacement.trim();
    }
}

List<Substitution> imagesToTest = Arrays.asList(
    new Substitution(
        "a", 
        "icr.io/instana/a:latest"
    ),
    new Substitution(
        "b:1.76", 
        "icr.io/instana/b:1.76"
    ),
    new Substitution(
        "package/c", 
        "icr.io/instana/package/c:latest"
    ),
    new Substitution(
        "package/c:1.74", 
        "icr.io/instana/package/c:1.74"
    ),
    new Substitution(
        "someHost.url/package/d", 
        "icr.io/instana/package/d:latest"
    ),
    new Substitution(
        "someHost.url/package/d:7.2", 
        "icr.io/instana/package/d:7.2"
    ),
    new Substitution(
        "someHost.url:5000/package/e", 
        "icr.io/instana/package/e:latest"
    ),
    new Substitution(
        "someHost.url:5000/package/e:1.5", 
        "icr.io/instana/package/e:1.5"
    ),
    new Substitution(
        "otel/opentelemetry-collector-contrib:0.147.0@sha256:e7c92c715f28ff142f3bcaccd4fc5603cf4c71276ef09954a38eb4038500a5a5", 
        "icr.io/instana/otel/opentelemetry-collector-contrib:0.147.0"
    ),
    new Substitution(
        "somehost.url/otel/opentelemetry-collector-contrib:0.147.0@sha256:e7c92c715f28ff142f3bcaccd4fc5603cf4c71276ef09954a38eb4038500a5a5", 
        "icr.io/instana/otel/opentelemetry-collector-contrib:0.147.0"
    ),
    new Substitution(
        "somehost.url:1293/otel/opentelemetry-collector-contrib:0.147.0@sha256:e7c92c715f28ff142f3bcaccd4fc5603cf4c71276ef09954a38eb4038500a5a5", 
        "icr.io/instana/otel/opentelemetry-collector-contrib:0.147.0"
    )
);
```
