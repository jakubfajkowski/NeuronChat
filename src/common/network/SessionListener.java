package common.network;

import common.network.Session;

public interface SessionListener {
    void onSessionDisposed(Session session);
}
