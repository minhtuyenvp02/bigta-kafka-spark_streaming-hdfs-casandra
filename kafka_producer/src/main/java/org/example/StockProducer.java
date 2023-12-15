package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.example.crawler.StockCrawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
public class StockProducer {
    private static Logger LOGGER = LoggerFactory.getLogger("StockProducer");
    private static final long PROGRESS_REPORTING_INTERVAL = 5;
    private static final Logger log = LoggerFactory.getLogger("StockProducer");
    public static void main(String[] args) {
//        if (args.length != 3) {
//            throw new IllegalArgumentException("" +
//                    "you need to supply " +
//                    "[1]: number that is maxRequestsPerSecond, " +
//                    "[2]: string topic name" +
//                    "[3]: kafka bootstrap.servers");
//        }
        final int maxRequestsPerSecond = 100;
        final String topicName = "firstdemo" ;
        final RateLimiter rateLimiter = RateLimiter.create(maxRequestsPerSecond);
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:8097,localhost:8098,localhost:8099");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        props.put(ProducerConfig.LINGER_MS_CONFIG, "20");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024));
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
//
        final KafkaProducer<String, JsonNode> producer = new KafkaProducer<>(props);
//        StockCrawler
        AtomicLong errorCount = new AtomicLong();


        // Create a counter to track the number of records we've successfully
        // created so far.
        final AtomicLong successCount = new AtomicLong();

        // This callback will be invoked whenever a send completes. It reports any
        // errors (and bumps the error-count) and signals the latch as described above.
        Callback postSender = (recordMetadata, e) -> {
            if (e != null) {
                log.error("Error adding to topic", e);
                errorCount.incrementAndGet();
            } else {
                successCount.incrementAndGet();
            }
        };
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
                () -> log.info("Successfully created {} Kafka records", successCount.get()),
                2, PROGRESS_REPORTING_INTERVAL, TimeUnit.SECONDS);
        StockCrawler stockCrawler = new StockCrawler(producer, topicName);
        stockCrawler.scheduleDataCrawling();
    }
}

