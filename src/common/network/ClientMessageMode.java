package common.network;

public enum ClientMessageMode {
    MESSAGE,
    AVAILABLE_USERS,
    INITIALIZE_KEY_NEGOTIATION,
    KEY_NEGOTIATION_REQUEST,
    KEY_NEGOTIATION_RESPONSE,
    TEST_KEY_REQUEST,
    TEST_KEY_RESPONSE,
    FINALIZE_KEY_NEGOTIATION,
    FAIL_KEY_NEGOTIATION,
    INITIALIZE_SESSION,
    LOGIN,
    REGISTER
}
