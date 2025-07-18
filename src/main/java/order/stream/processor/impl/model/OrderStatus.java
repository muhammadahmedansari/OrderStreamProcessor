package order.stream.processor.impl.model;

public enum OrderStatus {

    CREATED("created"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}