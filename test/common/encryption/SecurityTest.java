package common.encryption;

import common.network.ClientMessage;
import common.network.ClientMessageMode;
import common.network.SessionId;
import common.util.User;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class SecurityTest {
    private Random random = new Random();
    private final int numberOfIterations = 1000;

    @Test
    public void encryptAndDecryptBytesTest() throws Exception {
        int K = 4;
        int N = 8;
        int L = 4;

        TreeParityMachine treeParityMachine = new TreeParityMachine(K, N, L);
        byte[] key = treeParityMachine.generateKey();


        for (int i = 0; i < numberOfIterations; i++) {
            byte[] expected = new byte[random.nextInt(numberOfIterations) + numberOfIterations];
            random.nextBytes(expected);

            byte[] encryptedData = Security.encrypt(expected, key);
            byte[] decryptedData = Security.decrypt(encryptedData, key);

            assertArrayEquals(expected, decryptedData);
        }
    }

    @Test
    public void encryptAndDecryptObjectTest() throws Exception {
        int K = 4;
        int N = 8;
        int L = 4;

        TreeParityMachine treeParityMachine = new TreeParityMachine(K, N, L);
        byte[] key = treeParityMachine.generateKey();


        for (int i = 0; i < numberOfIterations; i++) {
            SessionId expected = new SessionId();

            byte[] encryptedData = Security.encryptObject(expected, key);
            Object decryptedData = Security.decryptObject(encryptedData, key);

            SessionId actual = (SessionId) decryptedData;

            assertEquals(expected, actual);
        }
    }

    @Test
    public void encryptAndDecryptClientMessageTest() throws Exception {
        int K = 4;
        int N = 8;
        int L = 4;

        TreeParityMachine treeParityMachine = new TreeParityMachine(K, N, L);
        byte[] key = treeParityMachine.generateKey();


        for (int i = 0; i < numberOfIterations; i++) {
            ArrayList<User> expected = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                expected.add(new User("TEST" + i));
            }

            ClientMessage clientMessage = new ClientMessage (
                    ClientMessageMode.TEST_KEY,
                    null,
                    expected
            );

            clientMessage.encryptPayload(key);
            clientMessage.decryptPayload(key);

            ArrayList<User> actual = (ArrayList<User>) clientMessage.getPayload();

            for (int j = 0; j < i; j++) {
                assertEquals(expected.get(j), actual.get(j));
            }
        }
    }
}