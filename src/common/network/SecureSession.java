package common.network;

import common.encryption.InputVector;
import common.encryption.LearningParameters;
import common.encryption.LearningRule;
import common.encryption.TreeParityMachine;
import common.util.Log;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;

public class SecureSession extends Session {
    private boolean synchronizing;
    private LearningRule learningRule;
    private int testKeyInterval;
    private int renegotiateAfter;
    private TreeParityMachine treeParityMachine;
    private byte[] key;
    private LearningParameters lp;

    public SecureSession(Socket socket, LinkedBlockingQueue<ClientMessage> messages) throws IOException {
        super(socket, messages);
    }

    @Override
    protected void read(ClientMessage message) throws InterruptedException {
        switch (message.getClientMessageMode()) {
            case INITIALIZE_SESSION:
                super.read(message);
                break;
            case INITIALIZE_KEY_NEGOTIATION:
                onInitializeKeyNegotiationReceived(message);
                write(message);
                break;
            case KEY_NEGOTIATION_REQUEST:
                onKeyNegotiationRequestReceived(message);
                write(message);
                break;
            case KEY_NEGOTIATION_RESPONSE:
                onKeyNegotiationResponseReceived(message);
                write(message);
                break;
            case TEST_KEY_REQUEST:
                //trigger view update
                super.read(new ClientMessage(ClientMessageMode.TEST_KEY_REQUEST, null, null));
                onTestKeyRequestReceived(message);
                write(message);
                break;
            case TEST_KEY_RESPONSE:
                onTestKeyResponse(message);
                write(message);
                break;
            case FINALIZE_KEY_NEGOTIATION:
                onFinalizeKeyNegotiation(message);
                break;
            case AVAILABLE_USERS:
                super.read(message);
                break;
            default:
                if (key != null) decryptMessage(message, key);
                super.read(message);
                break;
        }
    }

    private void decryptMessage(ClientMessage message, byte[] key) {
        try {
            message.decryptAddressee(key);
            message.decryptPayload(key);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            Log.print("Decryption error: " + e.getMessage());
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
            case KEY_NEGOTIATION_REQUEST:
                sendKeyNegotiationRequest(message);
                break;
            case KEY_NEGOTIATION_RESPONSE:
                sendKeyNegotiationResponse(message);
                break;
            case TEST_KEY_REQUEST:
                sendTestKeyRequest(message);
                break;
            case TEST_KEY_RESPONSE:
                sendTestKeyResponse(message);
                break;
            case FINALIZE_KEY_NEGOTIATION:
                onFinalizeKeyNegotiation(message);
                break;
            case AVAILABLE_USERS:
                break;
            default:
                if (key != null) encryptMessage(message, key);
                break;
        }

        super.write(message);
    }

    private void encryptMessage(ClientMessage message, byte[] key) {
        try {
            message.encryptAddressee(key);
            message.encryptPayload(key);
        }
        catch (IllegalBlockSizeException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            Log.print("Encryption error: " + e.getMessage());
        }
        catch (BadPaddingException ignored) {}
    }

    private void sendInitializeKeyNegotiationRequest(ClientMessage message) {
        initializeKeyNegotiation(message);
    }

    private void onInitializeKeyNegotiationReceived(ClientMessage message) {
        initializeKeyNegotiation(message);

        message.setClientMessageMode(ClientMessageMode.KEY_NEGOTIATION_REQUEST);
    }

    private void initializeKeyNegotiation(ClientMessage message) {
        Log.print("Session %s key negotiation initialized", getSessionId());
        lp = (LearningParameters) message.getPayload();

        learningRule = lp.getLearningRule();
        testKeyInterval = lp.getTestKeyInterval();
        renegotiateAfter = lp.getRenegotiateAfter();

        treeParityMachine = new TreeParityMachine(
                lp.getK(),
                lp.getN(),
                lp.getL()
        );

        synchronizing = true;
    }

    private void sendKeyNegotiationRequest(ClientMessage message) {
        InputVector inputVector = InputVector.generate(treeParityMachine);

        int output = treeParityMachine.computeOutput(inputVector);
        inputVector.setOutput(output);

        message.setPayload(inputVector);
    }

    private void onKeyNegotiationRequestReceived(ClientMessage message) {
        InputVector inputVector = (InputVector) message.getPayload();

        int siblingOutput = inputVector.getOutput();
        int localOutput = treeParityMachine.computeOutput(inputVector);

        if (siblingOutput == localOutput) {
            treeParityMachine.updateWeight(learningRule);
        }

        message.setClientMessageMode(ClientMessageMode.KEY_NEGOTIATION_RESPONSE);
    }

    private void sendKeyNegotiationResponse(ClientMessage message) {
        InputVector inputVector = (InputVector) message.getPayload();

        int localOutput = treeParityMachine.getOutput();
        inputVector.setOutput(localOutput);
        message.setPayload(inputVector);
    }

    private void onKeyNegotiationResponseReceived(ClientMessage message) {
        InputVector inputVector = (InputVector) message.getPayload();

        int siblingOutput = inputVector.getOutput();
        int localOutput = treeParityMachine.getOutput();

        if (siblingOutput == localOutput) {
            treeParityMachine.updateWeight(learningRule);
        }

        if (treeParityMachine.getCounter() >= renegotiateAfter) {
            Log.print("Session %s key negotiation reinitialized after %d iterations", getSessionId(), renegotiateAfter);
            message.setPayload(lp);
            message.setClientMessageMode(ClientMessageMode.INITIALIZE_KEY_NEGOTIATION);
        }
        else if (treeParityMachine.getCounter() % testKeyInterval == 0) {
            message.setClientMessageMode(ClientMessageMode.TEST_KEY_REQUEST);
        }
        else {
            message.setClientMessageMode(ClientMessageMode.KEY_NEGOTIATION_REQUEST);
        }
    }

    private void sendTestKeyRequest(ClientMessage message) {
        Log.print("Session %s sending test key request", getSessionId());
        message.setPayload(getSessionId());
        encryptMessage(message, treeParityMachine.generateKey());
    }

    private void onTestKeyRequestReceived(ClientMessage message) {
        boolean result = testKey(message);

        message.setPayload(result);
        message.setClientMessageMode(ClientMessageMode.TEST_KEY_RESPONSE);
    }

    private boolean testKey(ClientMessage message) {
        try {
            decryptMessage(message, treeParityMachine.generateKey());
            SessionId receivedSessionId = (SessionId) message.getPayload();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private void sendTestKeyResponse(ClientMessage message) {
        boolean result = (boolean) message.getPayload();

        if (result) {
            Log.print("Session %s key test succeeded on %d iteration",
                    getSessionId(), treeParityMachine.getCounter());
        }
        else {
            Log.print("Session %s key test failed on %d iteration",
                    getSessionId(), treeParityMachine.getCounter());
        }
    }

    private void onTestKeyResponse(ClientMessage message) {
        boolean result = (boolean) message.getPayload();

        if (result) {
        Log.print("Session %s key test succeeded on %d iteration",
                getSessionId(), treeParityMachine.getCounter());
        message.setClientMessageMode(ClientMessageMode.FINALIZE_KEY_NEGOTIATION);
        }
        else {
            Log.print("Session %s key test failed on %d iteration",
                    getSessionId(), treeParityMachine.getCounter());
            message.setClientMessageMode(ClientMessageMode.KEY_NEGOTIATION_REQUEST);
        }
    }

    private void onFinalizeKeyNegotiation(ClientMessage message) {
        Log.print("Session %s key negotiation finalized. Key is %s",
                getSessionId(), DatatypeConverter.printHexBinary(treeParityMachine.generateKey()));
        message.setPayload(null);
        key = treeParityMachine.generateKey();
    }

    public TreeParityMachine getTreeParityMachine() {
        return treeParityMachine;
    }
}
