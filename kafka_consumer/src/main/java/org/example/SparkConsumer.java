package org.example;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.internal.config.R;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

public class SparkConsumer {
    public static void main(String[] args) throws TimeoutException, StreamingQueryException {
        Logger.getLogger("org.apache").setLevel(Level.WARN);
        Logger.getLogger("org.apache.spark.storage").setLevel(Level.ERROR);
//        System.setProperty("hadoop.home.dir", "c:/hadoop");
        SparkSession session = SparkSession.builder()
                .master("local[*]")
                .appName("ProcessStockData")
                .config("spark.cassandra.connection.host", "localhost")
                .config("spark.cassandra.connection.port", "9042")
                .getOrCreate();
        session.sparkContext().setLogLevel("WARN");
//        session.conf().set("spark.sql.shuffle.partitions", "10");
        Dataset<Row> df = session.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:8097, localhost:8098, localhost:8099")
                .option("subscribe", "firstdemo")
                .option("startingOffsets", "earliest")
                .option("includeHeaders", "true")
                .load();
        df.printSchema();
        Schema schema = new Schema();
        StructType scm = schema.getSchema();
        Dataset<Row> stocks = df.selectExpr("CAST(value AS STRING)")
                .select(functions.from_json(functions.col("value"), scm).as("data"))
                .select("data.*");
        StreamingQuery query = stocks.writeStream()
                .format("org.apache.spark.sql.cassandra")
                .outputMode("append")
//                .trigger(Trigger.ProcessingTime("10 seconds"))
                .foreachBatch((Dataset<Row> writeDF, Long batchId) -> {
                    writeToCassandra(writeDF, session);
                })
                .start();
        query.awaitTermination();
    }
    private static void writeToCassandra(Dataset<Row> writeDF, SparkSession spark) {
        writeDF.write()
                .format("org.apache.spark.sql.cassandra")
                .mode("append")
                .option("table", "stock_test")
                .option("keyspace", "spark_stock")
                .save();
    }

        public static Dataset<Row> parseDataFromKafkaMessage(Dataset<Row> sdf, StructType schema) {
            if (!sdf.isStreaming()) {
                throw new IllegalArgumentException("DataFrame doesn't receive streaming data");
            }

            Column col = split(sdf.col("data.*"), ",");

            for (int idx = 0; idx < schema.length(); idx++) {
                StructField field = schema.apply(idx);
                sdf = sdf.withColumn(field.name(), col.getItem(idx).cast(field.dataType()));
            }

            Column[] columns = new Column[schema.length()];
            for (int idx = 0; idx < schema.length(); idx++) {
                StructField field = schema.apply(idx);
                columns[idx] = sdf.col(field.name());
            }

            return sdf.select(columns);
        }
}
