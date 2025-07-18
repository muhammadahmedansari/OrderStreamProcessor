package order.stream.processor.impl.model;

public enum DeliveryStatus {

    DELIVERED("delivered"),
    CANCELLED("cancelled");

    private final String value;

    DeliveryStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}