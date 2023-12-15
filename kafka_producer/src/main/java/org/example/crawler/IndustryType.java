package org.example.crawler;

import java.util.List;

public class IndustryType {
    private final String name;
    private final String code;
    private final List<String> listStockCode;

    public IndustryType(Builder builder) {
        this.name = builder.name;
        this.code = builder.code;
        this.listStockCode = builder.listStockCode;
    }

    @Override
    public String toString() {
        return "IndustryType{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", listStockCode=" + listStockCode +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public List<String> getListStockCode() {
        return listStockCode;
    }

    public static class Builder {
        private String name;
        private String code;
        private List<String> listStockCode;

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public Builder setListCodeStock(List<String> listStockCode) {
            this.listStockCode = listStockCode;
            return this;
        }

        public IndustryType build() {
            return new IndustryType(this);
        }
    }
}
