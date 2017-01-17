package common.network;

import common.network.Session;
/**
 * Klasa odpowiadająca za monitorowanie stanu sesji
 */
public interface SessionListener {
    void onSessionDisposed(Session session);
}
