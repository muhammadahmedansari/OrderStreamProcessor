package order.stream.processor.impl;

import com.google.auto.service.AutoService;
import order.stream.processor.api.OrderStreamProcessor;
import order.stream.processor.api.OrderStreamProcessorFactory;
import order.stream.processor.impl.model.OrderStatus;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Executors;

@AutoService(OrderStreamProcessorFactory.class)
public final class OrderStreamProcessorFactoryImpl implements OrderStreamProcessorFactory {
    @Override
    public OrderStreamProcessor createProcessor(int maxOrders, Duration maxTime) {
        final MessageFilter messageFilter = new MessageFilter();

        return new OrderStreamProcessorImpl(
                maxOrders,
                maxTime,
                ObjectMapperFactory.createObjectMapper(),
                Arrays.asList(OrderStatus.DELIVERED, OrderStatus.CANCELLED),
                new OrderReader(Executors.newScheduledThreadPool(2), messageFilter),
                messageFilter
        );
    }
}