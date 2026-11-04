package com.example.appreto.models;

public class Mobile {
    private String title;
    private String category;
    private double price;

    public Mobile(String title, String category, double price) {
        this.title = title;
        this.category = category;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }
}
