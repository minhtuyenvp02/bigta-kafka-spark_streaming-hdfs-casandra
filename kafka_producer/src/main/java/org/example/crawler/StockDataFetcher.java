package org.example.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StockDataFetcher {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final List<IndustryType> industryTypes = Constant.getListIndustryType();
    public static List<String> getStockCode(String stockType) throws IOException {
        String url_ = "https://bgapidatafeed.vps.com.vn/getlistckindex/" + stockType;
        List<String> responseList = new ArrayList<>();

        try {
            // Tạo URL từ chuỗi URL của API
            URL url = new URL(url_);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Lấy phản hồi từ API
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuffer response = new StringBuffer();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Chuyển đổi phản hồi thành danh sách chuỗi
//                System.out.println(response.toString());
                String[] responseArray = response.substring(2, response.length()-2).split("\",\"");
                for (String str : responseArray) {
                    responseList.add(str);
                }
            } else {
                System.out.println("Lỗi: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println(responseList.toString());
        return responseList;
    }

    public static List<String> getIndustry(String code) {
        String url = "https://histdatafeed.vps.com.vn/industry/symbols/" + code;
        OkHttpClient client = new OkHttpClient();

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            String responseData = Objects.requireNonNull(response.body()).string();
            responseData=responseData.substring(9,responseData.length()-16);
//            System.out.println(responseData);

            List<String> filteredData = new ArrayList<>();
            for (String item : responseData.split(",")) {
                if (!item.equals("null") && item.length() > 2) {
                    filteredData.add(item);
                }
            }
//            System.out.println(filteredData);

            return filteredData;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JsonNode getListStockData(String listStock) throws IOException {
        String url = "https://bgapidatafeed.vps.com.vn/getliststockdata/" + listStock;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        try (InputStream inputStream = connection.getInputStream()) {
//            System.out.println(objectMapper.readTree(inputStream));
            return objectMapper.readTree(inputStream);
        }
    }
}
