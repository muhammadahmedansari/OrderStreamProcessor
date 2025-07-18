package order.stream.processor.impl;

public class MessageFilter {
    private final static String KEEP_ALIVE_MESSAGE = "\n";
    private final static String EMPTY_MESSAGE = "";

    public boolean ignore(String incomingMessage) {
        return incomingMessage.equals(KEEP_ALIVE_MESSAGE) || incomingMessage.equals(EMPTY_MESSAGE);
    }
}