package org.example.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

        if ((currentTime.compareTo("01:00") >= 0 && currentTime.compareTo("14:30") <= 0)
                || (currentTime.compareTo("14:00") >= 0 && currentTime.compareTo("23:30") <= 0)) {
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
                        JsonNode arrayObject = node.deepCopy();
                        ((ObjectNode) arrayObject).put("industry", typeStock.getName());
                        ((ObjectNode) arrayObject).put("crawledTime", crawledTime);
                        try {
                            kafkaProducer.send(new ProducerRecord<>(topic,"value", arrayObject));
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
//            JsonNode customStockData = ;
//            System.out.println(stockDatas.toString());
//            List<StockData> stockDatas = stockDataFetcher.getListStockData(stringStockCodes);
//            List<StockData> datas = updateStockData(stockDatas);
//            updateDataInJsonFile(stockType, datas);
        }
    }


    private List<StockData> updateStockData(List<StockData> stockDatas) {
        List<StockData> updatedData = new ArrayList<>();

        // Update stock data according to the JavaScript code

        return updatedData;
    }

    private void updateDataInJsonFile(String stockType, List<StockData> datas) {
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

                if (!jsonContent.isEmpty()) {
                    fileData = Arrays.asList(new Gson().fromJson(jsonContent, StockData[].class));
                }

                fileData.addAll(datas);
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
    }
}