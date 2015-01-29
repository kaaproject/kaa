package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import java.util.HashMap;
import java.util.Map;

import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.verifier.UserVerifier;
import org.kaaproject.kaa.server.common.verifier.UserVerifierContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification.UserVerificationRequestMessage;
import org.kaaproject.kaa.server.operations.service.user.EndpointUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationUserVerifierActorMessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationUserVerifierActorMessageProcessor.class);

    private final EndpointUserService endpointUserService;

    private final String applicationId;

    /** The log appenders */
    private Map<Integer, UserVerifier> userVerifiers;

    ApplicationUserVerifierActorMessageProcessor(EndpointUserService endpointUserService, String applicationId) {
        this.applicationId = applicationId;
        this.endpointUserService = endpointUserService;
        initUserVerifiers();
    }

    private void initUserVerifiers() {
        this.userVerifiers = new HashMap<Integer, UserVerifier>();
        for (UserVerifierDto dto : endpointUserService.findUserVerifiers(applicationId)) {
            try {
                LOG.trace("Initializing user verifier for {}", dto);
                userVerifiers.put(dto.getVerifierId(), createUserVerifier(dto));
            } catch (Exception e) {
                LOG.error("Failed to create user verifier", e);
            }
        }
    }

    private UserVerifier createUserVerifier(UserVerifierDto verifierDto) throws ReflectiveOperationException {
        if (verifierDto == null) {
            throw new IllegalArgumentException("verifier dto can't be null");
        }
        try {
            @SuppressWarnings("unchecked")
            Class<UserVerifier> verifierClass = (Class<UserVerifier>) Class.forName(verifierDto.getClassName());
            UserVerifier userVerifier = verifierClass.newInstance();
            userVerifier.init(new UserVerifierContext(verifierDto));
            userVerifier.start();
            return userVerifier;
        } catch (ClassNotFoundException e) {
            LOG.error("Unable to find custom verifier class {}", verifierDto.getClassName());
            throw e;
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Unable to instantiate custom verifier from class {}", verifierDto.getClassName());
            throw e;
        }
    }
    
    public void verifyUser(UserVerificationRequestMessage message) {
        // TODO Auto-generated method stub
    }

    public void processNotification(Notification notification) {
        // TODO Auto-generated method stub
    }

    void preStart() {
    };

    void postStop() {
        for (UserVerifier verifier : userVerifiers.values()) {
            verifier.stop();
        }
    }

}
