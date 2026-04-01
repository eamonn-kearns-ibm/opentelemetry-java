package com.ibm.instana.otel.testcontainer;

import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.ImageNameSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * This class is used to ensure that images are pulled from the internal registry when building in the CI/CD pipeline.
 * 
 * If there is no version specified, the substitutor will default to using the "latest" tag.
 * This is an inherited behaviour from the underlying DockerImageName class.
 *
 * 
 * The substitutor must be configured with the following environment variable<br/>
 * INTERNAL_REGISTRY<br/>
 * This is the internal registry that will replace the image name's registry value
 */
public class OtelDockerImageNameSubstitutor extends ImageNameSubstitutor {
    /**
     * Logger for this class. Used to log the original and substituted image names when substitution occurs.
     */
    final Logger logger = LoggerFactory.getLogger(OtelDockerImageNameSubstitutor.class);
    /**
     * The internal registry that will replace the image name's registry value. This is set from the INTERNAL_REGISTRY environment variable.
     */
    protected final String internalRegistry = Optional.ofNullable(System.getenv("INTERNAL_REGISTRY")).orElse("us.icr.io/instana-tracer-otel").trim();

    {
        logger.warn("OtelDockerImageNameSubstitutor initialized with internal registry: {}", internalRegistry); 
    }

    /**
     * Getter for internal registry. Needed to expose this for the test suite
     * @return The internal registry for the image.
     */
    public String getInternalRegistry(){
        return this.internalRegistry;
    }

    /**
     * A method to substitute the image name with the internal registry. This, by design, does not use the newer fluent API of the DockerImageName class because that preserves the SHA256 digest in the substituted image name, which is not what we want. We want to ensure that the substituted image name always uses the version tag, even if the original image name uses a SHA256 digest.
     * @param name The Docker image name to be substituted.
     * @return The substituted Docker image name in the format {internalRegistry}/{repository}:{version}. <b>No SHA256 digest is included in the substituted image name, even if it was present in the original image name.</b>
     * @throws IllegalStateException If internalRegistry environtment variable is not set or is an empty string.
     * @throws IllegalStateException If internalRegistry ends with a /
     */
    @Override
    public DockerImageName apply(DockerImageName name){
        if(internalRegistry.isEmpty()){
            throw new IllegalStateException("INTERNAL_REGISTRY environment variable is not set");
        }
        else if(internalRegistry.endsWith("/")){
            throw new IllegalStateException("INTERNAL_REGISTRY environment variable must not end with /");
        }

        StringBuilder sb = new StringBuilder()
            .append(internalRegistry)
            .append("/")
            .append(name.getRepository())
            .append(":")
            .append(name.getVersionPart());

        String newName = sb.toString();

        logger.warn("Substituting image name: {} with {}", name.asCanonicalNameString(), newName);

        return DockerImageName.parse(newName);
    }

    /**
     * Required method by the ImageNameSubstitutor interface. 
     * This method returns a description of the substitutor, 
     * which includes the internal registry that is being used for substitution.
     */
    @Override
    protected String getDescription()
    {
        return "OtelImageNameSubstitutor with internal registry: "+ internalRegistry;
    }
}