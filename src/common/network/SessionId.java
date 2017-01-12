package common.network;

import java.io.Serializable;
import java.util.UUID;

public class SessionId implements Serializable {
    private String uniqueId = UUID.randomUUID().toString();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionId sessionId = (SessionId) o;

        return uniqueId.equals(sessionId.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }
}
