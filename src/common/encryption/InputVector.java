package common.encryption;

import java.io.Serializable;

/**
 * Klasa zawierająca wartości używane w komunikacji pomiędzy dwoma instancjami sieci neuronowych.
 * Posiada możliwość losowej inicjalizacji wartości binarnych. Zawiera wektor wejściowy dla sieci neuronowej, a także
 * przechowuje wartość wyjścia sieci neuronowej po przepuszczeniu wektora przez neurony sieci.
 */
public class InputVector implements Serializable {
    private int[] data;
    private int output;

    private InputVector(int length) {
        data = new int[length];

        for (int i = 0; i < length; i++) {
            data[i] = getRandomBit();
        }
    }

    public static InputVector generate(TreeParityMachine t) {
        int length = t.getInputVectorLength();
        return new InputVector(length);
    }


    private static int getRandomBit() {
        double randomNumber = Math.random() * 2;
        return (randomNumber > 1) ? 1 : -1;
    }

    public int[] getData() {
        return data;
    }

    public int getOutput() {
        return output;
    }

    public void setOutput(int output) {
        this.output = output;
    }
}
