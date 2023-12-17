package org.example;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
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
                .getOrCreate();
        session.sparkContext().setLogLevel("WARN");
//        session.conf().set("spark.sql.shuffle.partitions", "10");
        Dataset<Row> df = session.readStream().format("kafka")
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
                .outputMode("update")
                .format("console")
                .start();
        query.awaitTermination();
    }
}
