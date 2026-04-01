package com.ibm.instana.otel.testcontainer;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.testcontainers.utility.DockerImageName;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

public class OtelDockerImageNameSubstitutorTest {

    class Substitution {
        public String original;
        public String replacement;

        public Substitution(String original, String replacement) {
            this.original = original.trim();
            this.replacement = replacement.trim();
        }
    }

    @Test
    @SetEnvironmentVariable(key="INTERNAL_REGISTRY", value="")
    public void emptyRegistryThrowsException() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new OtelDockerImageNameSubstitutor().apply(DockerImageName.parse("test:latest"))
        );
    }

    @Test
    @SetEnvironmentVariable(key="INTERNAL_REGISTRY", value="icr.io/instana")
    public void getDescriptionReturnsDescriptionWithRegistryURL() {
        OtelDockerImageNameSubstitutor substitutor = new OtelDockerImageNameSubstitutor();
        String description = substitutor.getDescription();
        Assertions.assertTrue(description.contains("icr.io/instana"));
    }

    @Test
    @SetEnvironmentVariable(key="INTERNAL_REGISTRY", value="icr.io/instana ") //testing that whitespace is trimmed
    public void getDescriptionReturnsDescriptionWithRegistryURLTrimmed() {
        OtelDockerImageNameSubstitutor substitutor = new OtelDockerImageNameSubstitutor();
        String description = substitutor.getDescription();
        Assertions.assertTrue(description.contains("icr.io/instana"));
    }

    @Test
    @SetEnvironmentVariable(key="INTERNAL_REGISTRY", value="icr.io/instana")
    public void registryIsBeingSetFromEnvironment() {
        OtelDockerImageNameSubstitutor substitutor = new OtelDockerImageNameSubstitutor();
        Assertions.assertEquals("icr.io/instana", substitutor.getInternalRegistry());
    }


    @Test
    @SetEnvironmentVariable(key="INTERNAL_REGISTRY", value="icr.io/instana/")
    public void invalidRegistryURLThrowsException()
    {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new OtelDockerImageNameSubstitutor().apply(DockerImageName.parse("test:latest"))
        );
    }

    @Test
    @SetEnvironmentVariable(key="INTERNAL_REGISTRY", value="icr.io/instana") 
    public void substitutorReturnsSubstitutedImageName() {
        OtelDockerImageNameSubstitutor substitutor = new OtelDockerImageNameSubstitutor();
        
        DockerImageName name = DockerImageName.parse("test:latest");
        DockerImageName substituted = substitutor.apply(name);

        Assertions.assertEquals(substituted.toString(), "icr.io/instana/test:latest");
    }

    @Test
    @SetEnvironmentVariable(key="INTERNAL_REGISTRY", value="icr.io/instana") 
    public void runSubstitutorAgainstBatch() {
        OtelDockerImageNameSubstitutor substitutor = new OtelDockerImageNameSubstitutor();

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

        for(Substitution image:imagesToTest){
            DockerImageName name = DockerImageName.parse(image.original);
            DockerImageName substituted = substitutor.apply(name);
            
            Assertions.assertEquals( image.replacement, substituted.asCanonicalNameString());
        }
    }

    
}
