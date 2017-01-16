package common.encryption;

import java.io.Serializable;

public class InputVector implements Serializable {
    private int[] data;

    private InputVector() {}

    public static InputVector generate(int K, int N) {
        InputVector inputVector = new InputVector();

        inputVector.data = new int[K * N];

        for (int i = 0; i < K * N; i++) {
            inputVector.data[i] = getRandomBit();
        }

        return inputVector;
    }


    private static int getRandomBit() {
        double randomNumber = Math.random() * 2;
        return (randomNumber > 1) ? 1 : -1;
    }

    public int[] getData() {
        return data;
    }
}
