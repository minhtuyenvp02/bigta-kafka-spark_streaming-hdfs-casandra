package org.example.crawler;

import com.datastax.oss.driver.shaded.codehaus.jackson.type.TypeReference;
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.ticks.StockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class StockCrawler {
    private static final String[] STOCK_TYPES = {"HOSE", "HNX", "UPCOM"};
    private static final String DATA_FOLDER = "./data/";
    Logger log = LoggerFactory.getLogger("StockCrawler");
    KafkaProducer<String, JsonNode> kafkaProducer;
    String topicName;

    public StockCrawler(KafkaProducer<String, JsonNode> kafkaProducer, String topicName) {
        this.kafkaProducer = kafkaProducer;
        this.topicName = topicName;
    }
//    public static void main(String[] args) throws IOException {
////        crawlDataAtFirstTime();
//        scheduleDataCrawling();
//    }

    public void crawlDataAtFirstTime() throws IOException {
        for (String stockType : STOCK_TYPES) {
           crawlData(stockType.toLowerCase(),kafkaProducer, topicName );
        }
    }
//
    public void scheduleDataCrawling() {
        long delay = 5 * 60 * 1000; // 5 minutes

        Runnable task = () -> {
            for (String stockType : STOCK_TYPES) {
                try {
                    crawlData(stockType.toLowerCase(),kafkaProducer, topicName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Thread thread = new Thread(() -> {
            while (true) {
                task.run();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void crawlData(String stockType, KafkaProducer<String, JsonNode> kafkaProducer,String topic) throws IOException {
        StockDataFetcher stockDataFetcher = new StockDataFetcher();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String currentTime = dateFormat.format(new Date());
        List<IndustryType> typeList = Constant.getListIndustryType();
//        System.out.println(typeList.toString());

        if ((currentTime.compareTo("09:00") >= 0 && currentTime.compareTo("12:30") <= 0)
                || (currentTime.compareTo("14:00") >= 0 && currentTime.compareTo("16:30") <= 0)) {
            System.out.println("Crawling data at " + currentTime);
            String crawledTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            List<String> stockCodes = stockDataFetcher.getStockCode(stockType);
//            System.out.println(1);
            String stringStockCodes = String.join(",", stockCodes);

//            System.out.println(stringStockCodes.toString());
            JsonNode stockDatas = stockDataFetcher.getListStockData(stringStockCodes);
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode newArrayNode = mapper.createArrayNode();
            ArrayNode arrayField = (ArrayNode) stockDatas;
//            System.out.println(arrayField.toString());
//            System.out.println(typeList.toString());
            arrayField.forEach(node -> {
//                System.out.println(node.toString());
                typeList.forEach(typeStock ->{
                    if(typeStock.getListStockCode().contains(node.get("sym").toString())){
//                        JsonNode arrayObject = node.deepCopy();
                        ((ObjectNode) node).put("industry", typeStock.getName());
                        ((ObjectNode) node).put("crawledTime", crawledTime);
                        try {
                            kafkaProducer.send(new ProducerRecord<>(topic,"value", node));
                        } catch (Exception exception){
                            log.warn("problem when send - ignoring", exception);
                        }
                    }
                });
            });

//            System.out.println(newArrayNode.toString());
//            System.out.println(stockDatas.toString());
//            return newArrayNode;

//            }
//            List<StockData> datas = updateStockData(stockDatas);
//            updateDataInJsonFile(stockType, datas);
            updateDataInJsonFile(stockType, stockDatas);
        }
    }


    private List<StockData> updateStockData(JsonNode stockDatas) {
        List<StockData> updatedData = new ArrayList<>();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<StockCrawler.StockData>>() {}.getType();
        return gson.fromJson(String.valueOf(stockDatas), listType);
    }

    public void updateDataInJsonFile(String stockType, JsonNode datas) {
        String DATA_FOLDER = "/Users/minhtuyen02/MTuyen/bigdata_project/data_folder";
        String filePath = DATA_FOLDER + stockType + ".json";
        File file = new File(filePath);

        if (!file.exists()) {
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("[]");
                writer.close();
                System.out.println("File created with initial data");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Path path = Paths.get(filePath);
                byte[] jsonData = Files.readAllBytes(path);
                String jsonContent = new String(jsonData);
                List<StockData> fileData = new ArrayList<>();

                List<StockData> data = null;
                if (!jsonContent.isEmpty()) {
                    data = updateStockData(datas);
                }

                fileData.addAll(data);
                FileWriter writer = new FileWriter(file);
                writer.write(new Gson().toJson(fileData));
                writer.close();
                System.out.println("Data written to file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class StockData {
        private String id;
        private String sym;
        private String c;
        private String f;
        private String g1;
        private String g2;
        private String g3;
        private String g4;
        private String g5;
        private String g6;
        private String lastPrice;
        private String lastVolume;
        private String lot;
        private String ot;
        private String avePrice;
        private String highPrice;
        private String lowPrice;
        private String fBVol;
        private String fSVolume;
        private String fRoom;
        private String fBValue;
        private String fSValue;
        private String industry;
        private String crawledTime;

        public StockData(String id, String sym, String c, String f, String g1, String g2, String g3, String g4, String g5, String g6, String lastPrice, String lastVolume, String lot, String ot, String avePrice, String highPrice, String lowPrice, String fBVol, String fSVolume, String fRoom, String fBValue, String fSValue, String industry, String crawledTime) {
            this.id = id;
            this.sym = sym;
            this.c = c;
            this.f = f;
            this.g1 = g1;
            this.g2 = g2;
            this.g3 = g3;
            this.g4 = g4;
            this.g5 = g5;
            this.g6 = g6;
            this.lastPrice = lastPrice;
            this.lastVolume = lastVolume;
            this.lot = lot;
            this.ot = ot;
            this.avePrice = avePrice;
            this.highPrice = highPrice;
            this.lowPrice = lowPrice;
            this.fBVol = fBVol;
            this.fSVolume = fSVolume;
            this.fRoom = fRoom;
            this.fBValue = fBValue;
            this.fSValue = fSValue;
            this.industry = industry;
            this.crawledTime = crawledTime;
        }
    }
}