package common.network;

import common.encryption.InputVector;
import common.encryption.LearningRule;
import common.encryption.TreeParityMachine;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;

public class SecureSession extends Session {
    private LearningRule learningRule = LearningRule.RANDOM_WALK;
    private int testKeyFrequency = 500;
    private TreeParityMachine treeParityMachine;
    private byte[] key;

    public SecureSession(Socket socket, LinkedBlockingQueue<ClientMessage> messages) throws IOException {
        super(socket, messages);
    }

    @Override
    protected void read(ClientMessage message) throws InterruptedException {
        switch (message.getClientMessageMode()) {
            case INITIALIZE_SESSION:
                break;
            case INITIALIZE_KEY_NEGOTIATION:
                onInitializeKeyNegotiationReceived(message);
                break;
            case KEY_NEGOTIATION:
                onKeyNegotiationReceived(message);
                break;
            case TEST_KEY:
                onTestKeyReceived(message);
                break;
            case FINALIZE_KEY_NEGOTIATION:
                onFinalizeKeyNegotiation(message);
                break;
            default:
                decryptMessage(message);
                break;
        }
        super.read(message);
    }

    private void decryptMessage(ClientMessage message) {
        try {
            message.decryptPayload(key);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(ClientMessage message) {
        switch (message.getClientMessageMode()) {
            case INITIALIZE_SESSION:
                break;
            case INITIALIZE_KEY_NEGOTIATION:
                sendInitializeKeyNegotiationRequest(message);
                break;
            case KEY_NEGOTIATION:
                sendKeyNegotiationRequest(message);
                break;
            case TEST_KEY:
                sendTestKeyRequest(message);
                break;
            case FINALIZE_KEY_NEGOTIATION:
                onFinalizeKeyNegotiation(message);
                break;
            default:
                encryptMessage(message);
                break;
        }

        super.write(message);
    }

    private void encryptMessage(ClientMessage message) {
        try {
            message.encryptPayload(key);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void sendInitializeKeyNegotiationRequest(ClientMessage message) {
        initializeKeyNegotiation(message);
    }

    private void onInitializeKeyNegotiationReceived(ClientMessage message) {
        initializeKeyNegotiation(message);
        message.setClientMessageMode(ClientMessageMode.KEY_NEGOTIATION);
    }

    private void initializeKeyNegotiation(ClientMessage message) {
        int[] parameters = (int[]) message.getPayload();
        treeParityMachine = new TreeParityMachine(
                parameters[0],
                parameters[1],
                parameters[2]
        );
    }

    private void sendKeyNegotiationRequest(ClientMessage message) {
        InputVector inputVector = InputVector.generate(treeParityMachine);

        int output = treeParityMachine.computeOutput(inputVector);
        inputVector.setOutput(output);

        message.setPayload(inputVector);
    }

    private void onKeyNegotiationReceived(ClientMessage message) {
        InputVector inputVector = (InputVector) message.getPayload();

        int siblingOutput = inputVector.getOutput();
        int localOutput = treeParityMachine.computeOutput(inputVector);

        if (siblingOutput == localOutput)
            treeParityMachine.updateWeight(learningRule);

        if (treeParityMachine.getCounter() % testKeyFrequency == 0) {
            message.setClientMessageMode(ClientMessageMode.TEST_KEY);
        }
    }

    private void sendTestKeyRequest(ClientMessage message) {
        message.setPayload(getSessionId());
        encryptMessage(message);
    }

    private void onTestKeyReceived(ClientMessage message) {
        decryptMessage(message);
        SessionId receivedSessionId = (SessionId) message.getPayload();

        if (receivedSessionId.equals(getSessionId())) {
            message.setClientMessageMode(ClientMessageMode.FINALIZE_KEY_NEGOTIATION);
        }
        else {
            message.setClientMessageMode(ClientMessageMode.KEY_NEGOTIATION);
        }
    }

    private void onFinalizeKeyNegotiation(ClientMessage message) {
        message.setPayload(null);
        key = treeParityMachine.generateKey();
    }
}
