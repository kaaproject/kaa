package org.kaaproject.kaa.server.common.verifier;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKaaUserVerifier<T extends SpecificRecordBase> implements UserVerifier {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractKaaUserVerifier.class);

    @Override
    public void init(UserVerifierContext context) throws UserVerifierLifecycleException{
        LOG.info("Initializing user verifier with {}", context);
        AvroByteArrayConverter<T> converter = new AvroByteArrayConverter<>(getConfigurationClass());
        try {
            T configuration = converter.fromByteArray(context.getVerifierDto().getRawConfiguration());
            LOG.info("Initializing user verifier {} with {}", getClassName(), configuration);
            init(context, configuration);
        } catch (IOException e) {
            LOG.error(MessageFormat.format("Failed to initialize user verifier {0}", getClassName()), e);
            throw new UserVerifierLifecycleException(e);
        }
    }
    
    public abstract void init(UserVerifierContext context, T configuration) throws UserVerifierLifecycleException;
    
    /**
     * Gets the configuration class.
     *
     * @return the configuration class
     */
    public abstract Class<T> getConfigurationClass();
    
    private String getClassName() {
        return this.getClass().getName();
    }
}
