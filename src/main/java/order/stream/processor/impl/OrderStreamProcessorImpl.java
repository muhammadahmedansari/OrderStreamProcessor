package order.stream.processor.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import order.stream.processor.api.OrderStreamProcessor;
import order.stream.processor.impl.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class OrderStreamProcessorImpl implements OrderStreamProcessor {
    private final int maxOrders;
    private final Duration maxTime;
    private final List<OrderStatus> validOrderStatus;
    private final ObjectMapper objectMapper;
    private final OrderReader orderReader;
    private final MessageFilter messageFilter;

    public OrderStreamProcessorImpl(int maxOrders, Duration maxTime, ObjectMapper objectMapper, List<OrderStatus> validOrderStatus, OrderReader orderReader, MessageFilter messageFilter) {
        this.maxOrders = maxOrders;
        this.maxTime = maxTime;
        this.validOrderStatus = validOrderStatus;
        this.objectMapper = objectMapper;
        this.orderReader = orderReader;
        this.messageFilter = messageFilter;
    }

    @Override
    public void process(InputStream source, OutputStream sink) {
        HashMap<Delivery, List<Order>> deliveryWithOrders = new HashMap<>();
        String data = "";
        try {
            data = orderReader.readOrders(maxTime, maxOrders, source);
        } catch (InterruptedException | ExecutionException ignored) {
        }

        List<String> orderList = Arrays.stream(data.split("\n")).toList();
        for (String o : orderList) {
            if (messageFilter.ignore(o)) {
                continue;
            }
            Order order = null;
            try {
                order = objectMapper.readValue(o, Order.class);
            } catch (JsonProcessingException ignored) {
            }

            if (order != null && validOrderStatus.contains(order.orderStatus())) {
                deliveryWithOrders.putIfAbsent(order.delivery(), new ArrayList<>());
                deliveryWithOrders.get(order.delivery()).add(order);
            }
        }

        List<SinkDelivery> sinkDeliveries = new ArrayList<>();
        for (Delivery delivery : deliveryWithOrders.keySet()) {
            List<Order> orders = deliveryWithOrders.get(delivery);
            List<SinkOrder> sinkOrders = ordersToSinkOrders(orders);

            if (!sinkOrders.isEmpty()) {
                sinkDeliveries.add(new SinkDelivery(delivery.deliveryId(), delivery.deliveryTime(),
                        getDeliveryStatus(orders), sinkOrders, sumOfDeliveredOrders(orders)));
            }
        }

        sinkDeliveries.sort(Comparator.comparing(SinkDelivery::deliveryTime).thenComparing(SinkDelivery::deliveryId));

        try {
            objectMapper.writeValue(sink, sinkDeliveries);
        } catch (IOException ignored) {
        }
    }

    private int sumOfDeliveredOrders(List<Order> orders) {
        return sumOfOrders(orders, OrderStatus.DELIVERED);
    }

    private int sumOfOrders(List<Order> orders, OrderStatus orderStatus) {
        return orders.stream()
                .filter(order -> orderStatus.equals(order.orderStatus()))
                .mapToInt(Order::amount)
                .sum();
    }

    private DeliveryStatus getDeliveryStatus(List<Order> orders) {
        return orders
                .stream()
                .anyMatch(order -> order.orderStatus().equals(OrderStatus.DELIVERED)) ? DeliveryStatus.DELIVERED : DeliveryStatus.CANCELLED;
    }

    private List<SinkOrder> ordersToSinkOrders(List<Order> orders) {
        return orders
                .stream()
                .map(order -> new SinkOrder(order.orderId(), order.amount()))
                .sorted(Comparator.comparing(SinkOrder::orderId).reversed())
                .collect(Collectors.toList());
    }
}