package common.encryption;

/**
 * Typ enumerowany pozwalający zaaplikować 3 różne metody uaktualniania wartości wag neuronów
 */
public enum LearningRule {
    HEBBIAN,
    ANTI_HEBBIAN,
    RANDOM_WALK
}
