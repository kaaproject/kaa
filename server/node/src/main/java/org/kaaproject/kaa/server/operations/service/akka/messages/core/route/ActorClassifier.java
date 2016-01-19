package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;

public class ActorClassifier {
    private final boolean globalActor;

    public ActorClassifier(boolean globalActor) {
        super();
        this.globalActor = globalActor;
    }

    public boolean isGlobalActor() {
        return globalActor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (globalActor ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ActorClassifier other = (ActorClassifier) obj;
        if (globalActor != other.globalActor)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ActorClassifier [globalActor=" + globalActor + "]";
    }

}
