package common.encryption;

import common.util.Log;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.*;

public class TreeParityMachineTest {
    @Test
    public void synchronize() throws Exception {
        int K = 4;
        int N = 8;
        int L = 16;

        TreeParityMachine a = new TreeParityMachine(K, N, L);
        TreeParityMachine b = new TreeParityMachine(K, N, L);

        int counter = 0;
        do {
            InputVector inputVector = InputVector.generate(K, N);

            int tauA = a.computeOutput(inputVector);
            int tauB = b.computeOutput(inputVector);

            if (tauA == tauB) {
                a.updateWeight(LearningRule.RANDOM_WALK);
                b.updateWeight(LearningRule.RANDOM_WALK);
            }

            counter++;
            Log.print("MSE: " + TreeParityMachine.synchronizationStatus(a.getWeights(), b.getWeights()));
        } while (TreeParityMachine.meanSquaredError(a.getWeights(), b.getWeights()) != 0);

        Log.print("Synchronized in %d iterations.", counter);
        Log.print("Machine A key: " + DatatypeConverter.printHexBinary(a.generateKey()));
        Log.print("Machine B key: " + DatatypeConverter.printHexBinary(b.generateKey()));

        assertArrayEquals(a.generateKey(), b.generateKey());
    }
}