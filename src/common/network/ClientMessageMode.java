package common.network;

/**
 * Typ enumerowany pozwalający wysłać wiele typów wiadomości do serwera
 */
public enum ClientMessageMode {
    MESSAGE,
    AVAILABLE_USERS,
    INITIALIZE_KEY_NEGOTIATION,
    KEY_NEGOTIATION_REQUEST,
    KEY_NEGOTIATION_RESPONSE,
    TEST_KEY,
    FINALIZE_KEY_NEGOTIATION,
    INITIALIZE_SESSION,
    LOGIN,
    REGISTER
}
