package com.example.stock_watch;


import java.io.Serializable;

public class Stock implements Serializable {

    private String symbol;
    private String companyName;
    private double price;
    private double changedPrice;
    private double changedPercentage;

    public Stock(String symbol, String companyName, double price, double changedPrice, double changedPercentage) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.price = price;
        this.changedPrice = changedPrice;
        this.changedPercentage = changedPercentage;
    }

    public Stock(String symbol, String companyName){
        this.symbol = symbol;
        this.companyName = companyName;
        this.price = 0.0;
        this.changedPrice = 0.0;
        this.changedPercentage = 0.0;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public double getPrice() {
        return price;
    }

    public double getChangedPrice() {
        return changedPrice;
    }

    public double getChangedPercentage() {
        return changedPercentage;
    }
}
