package common.encryption;

import java.io.Serializable;
/**
 * Klasa odpowiedzialna za ustawienie parametrów przesyłane w wiadomości inicjalizacyjnej, potrzebnych do ustalenia zgodności sieci
 */
public class LearningParameters implements Serializable {
    private LearningRule learningRule;
    private int k;
    private int n;
    private int l;
    private int testKeyInterval;
    private int renegotiateAfter;

    public LearningParameters(LearningRule learningRule, int k, int n, int l, int testKeyInterval, int renegotiateAfter) {
        this.learningRule = learningRule;
        this.k = k;
        this.n = n;
        this.l = l;
        this.testKeyInterval = testKeyInterval;
        this.renegotiateAfter = renegotiateAfter;
    }

    public LearningRule getLearningRule() {
        return learningRule;
    }

    public int getK() {
        return k;
    }

    public int getN() {
        return n;
    }

    public int getL() {
        return l;
    }

    public int getTestKeyInterval() {
        return testKeyInterval;
    }

    public int getRenegotiateAfter() {
        return renegotiateAfter;
    }
}
