package common.encryption;

import java.io.Serializable;

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
