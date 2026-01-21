package com.idear.fimpe.remoterecharge.domain;

import java.time.LocalDateTime;

public class RemoteRechargeTransaction {

   private String cardId;
   private LocalDateTime registerDateTime;
   private Integer actionNumberAppliedToProduct;
   private Float amount;
   private String productId;
   private String rechargeType;
   private String samId;//Solo para confirmaciones

    public RemoteRechargeTransaction(String cardId, LocalDateTime registerDateTime, Integer actionNumberAppliedToProduct,
                                     Float amount, String productId, String rechargeType) {
        this.cardId = cardId;
        this.registerDateTime = registerDateTime;
        this.actionNumberAppliedToProduct = actionNumberAppliedToProduct;
        this.amount = amount;
        this.productId = productId;
        this.rechargeType = rechargeType;
    }

    public RemoteRechargeTransaction(String cardId, LocalDateTime registerDateTime, Integer actionNumberAppliedToProduct,
                                     Float amount, String productId, String rechargeType, String samId) {
        this.cardId = cardId;
        this.registerDateTime = registerDateTime;
        this.actionNumberAppliedToProduct = actionNumberAppliedToProduct;
        this.amount = amount;
        this.productId = productId;
        this.rechargeType = rechargeType;
        this.samId = samId;
    }

    public String getCardId() {
        return cardId;
    }

    public LocalDateTime getRegisterDateTime() {
        return registerDateTime;
    }

    public Integer getActionNumberAppliedToProduct() {
        return actionNumberAppliedToProduct;
    }

    public Float getAmount() {
        return amount;
    }

    public String getProductId() {
        return productId;
    }

    public String getRechargeType() {
        return rechargeType;
    }

    public String getSamId() {
        return samId;
    }
}
