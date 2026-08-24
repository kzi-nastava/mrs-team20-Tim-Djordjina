package com.example.team20_tim_djordjina.model;

public class PricingConfig {

    private double standardPrice;
    private double luxuryPrice;
    private double vanPrice;
    private double pricePerKm;

    public PricingConfig() {}

    public PricingConfig(double standardPrice, double luxuryPrice,
                         double vanPrice, double pricePerKm) {
        this.standardPrice = standardPrice;
        this.luxuryPrice = luxuryPrice;
        this.vanPrice = vanPrice;
        this.pricePerKm = pricePerKm;
    }

    public double getStandardPrice() {
        return standardPrice;
    }

    public double getLuxuryPrice() {
        return luxuryPrice;
    }

    public double getVanPrice() {
        return vanPrice;
    }

    public double getPricePerKm() {
        return pricePerKm;
    }
}
