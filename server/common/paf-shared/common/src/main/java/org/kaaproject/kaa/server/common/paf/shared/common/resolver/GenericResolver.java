package org.kaaproject.kaa.server.common.paf.shared.common.resolver;

public interface GenericResolver extends SessionIdResolver, 
                                         SessionTypeResolver,
                                         ApplicationRouteResolver,
                                         ApplicationProfileRouteResolver,
                                         EndpointIdResolver {

}
