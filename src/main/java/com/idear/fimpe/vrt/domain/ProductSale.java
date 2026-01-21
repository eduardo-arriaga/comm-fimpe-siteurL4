package com.idear.fimpe.vrt.domain;

import com.idear.fimpe.enums.Product;

import java.time.LocalDate;
import java.time.LocalTime;

public class ProductSale {
    private Product productId;
    private LocalDate startProductValidity;
    private LocalDate endProductValidity;
    private LocalTime startProductValidityDuringDay;
    private LocalTime endProductValidityDuringDay;

    public ProductSale(Product productId, LocalDate startProductValidity, LocalDate endProductValidity,
                       LocalTime startProductValidityDuringDay, LocalTime endProductValidityDuringDay) {
        this.productId = productId;
        this.startProductValidity = startProductValidity;
        this.endProductValidity = endProductValidity;
        this.startProductValidityDuringDay = startProductValidityDuringDay;
        this.endProductValidityDuringDay = endProductValidityDuringDay;
    }

    public Product getProductId() {
        return productId;
    }

    public LocalDate getStartProductValidity() {
        return startProductValidity;
    }

    public LocalDate getEndProductValidity() {
        return endProductValidity;
    }

    public LocalTime getStartProductValidityDuringDay() {
        return startProductValidityDuringDay;
    }

    public LocalTime getEndProductValidityDuringDay() {
        return endProductValidityDuringDay;
    }

    @Override
    public String toString() {
        return "ProductSale{" +
                "productId=" + productId +
                ", startProductValidity=" + startProductValidity +
                ", endProductValidity=" + endProductValidity +
                ", startProductValidityDuringDay=" + startProductValidityDuringDay +
                ", endProductValidityDuringDay=" + endProductValidityDuringDay +
                '}';
    }
}
