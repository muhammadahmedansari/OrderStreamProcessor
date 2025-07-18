package order.stream.processor.impl.model;

import java.util.Date;
import java.util.List;

public record SinkDelivery(String deliveryId, Date deliveryTime, DeliveryStatus deliveryStatus,
                           List<SinkOrder> orders, Integer totalAmount) {
}