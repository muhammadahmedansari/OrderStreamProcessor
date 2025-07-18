package order.stream.processor.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OrderReader {

    private final ScheduledExecutorService executor;
    private final MessageFilter messageFilter;


    public OrderReader(ScheduledExecutorService executor, MessageFilter messageFilter) {
        this.executor = executor;
        this.messageFilter = messageFilter;
    }

    public String readOrders(Duration duration, int maxOrder, InputStream source) throws InterruptedException, ExecutionException {
        StringBuilder data = new StringBuilder();
        try {
            executor.submit(() -> read(data, source, maxOrder))
                    .get(duration.getSeconds(), TimeUnit.SECONDS);
        } catch (TimeoutException ignored) {
        }
        return data.toString();
    }

    private void read(StringBuilder data, InputStream source, int maxOrder) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(source));
        int totalOrdersProcessed = 0;
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                if (messageFilter.ignore(line)) {
                    continue;
                }
                data.append(line).append("\n");
                totalOrdersProcessed++;

                if (totalOrdersProcessed >= maxOrder) {
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }
}