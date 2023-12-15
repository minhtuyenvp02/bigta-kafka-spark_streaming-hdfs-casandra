package org.example.crawler;

import java.util.ArrayList;
import java.util.List;

public class Constant {
    private static List<IndustryType> listIndustryType = new ArrayList<>();


    public static final List<IndustryType> getListIndustryType() {
       String [] listName = {"Dầu khí", "Bán lẻ", "Hoá chất", "Truyền thông", "Tài nguyên cơ bản", "Du lịch và giải trí", "Xây dựng và vật liệu", "Viễn thông","Hàng và dịch vụ công nghệp", "Điện, nước và xăng dầu khí đốt", "Ngân hàng", "Ô tô và phụ tùng", "Thực phẩm và đồ uống","Bảo hiểm", "Hàng cá nhân và gia dụng", "Bất động sản", "Y tế", "Dịch vụ và tài chính", "Công nghệ thông tin"};
       String [] code = {"0500", "5300", "1300", "5500", "1700", "5700", "2300", "6500", "2700", "7500", "8300", "3300", "3500", "8500", "3700","8600", "4500", "8700","9500"};


       List<IndustryType> typeList = new ArrayList<>();
        for (int i = 0; i < 18 ; i++) {
//            System.out.println(StockDataFetcher.getIndustry(code[i]));
            typeList.add(IndustryType.Builder
                    .newInstance()
                            .setCode(code[i])
                            .setName(listName[i])
                            .setListCodeStock(StockDataFetcher.getIndustry(code[i]))
                            .build());

        }
        return typeList;
    }
}
