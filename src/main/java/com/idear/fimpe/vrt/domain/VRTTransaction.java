package com.idear.fimpe.vrt.domain;

import com.idear.fimpe.enums.OperationType;
import com.idear.fimpe.enums.Product;

import java.time.LocalDateTime;

import static com.idear.fimpe.enums.Product.*;

public class VRTTransaction {

    //Common values
    private Long transactionId;
    private OperationType operationType;
    private String transactionFoil;
    private LocalDateTime transactionDate;
    private String serialCard;
    private Float transactionAmmount;
    private String samId;
    //Only for sales
    private String profile;//perfil que se vendio tarifa general 0
    private ProductSale productCreditId;
    private ProductSale productMoneyId;

    //Only for recharge
    private Product productId;
    private Float initialBalance;
    private Float finalBalance;
    private String samTransactionCounter;
    private Long cardTransactionCounter;
    private String rechargeType;

    //utilizado para las ventas
    public VRTTransaction(Long transactionId, OperationType operationType, LocalDateTime transactionDate, String serialCard,
                          Long cardTransactionCounter, Float transactionAmmount, String samId, String profile,
                          ProductSale productCreditId, ProductSale productMoneyId) {

        this.transactionId = transactionId;
        this.operationType = operationType;
        this.transactionDate = transactionDate;
        this.serialCard = serialCard;
        this.transactionAmmount = transactionAmmount;
        this.samId = samId;
        this.profile = profile;
        this.productCreditId = productCreditId;
        this.productMoneyId = productMoneyId;
        this.transactionFoil = generateFoil(serialCard, cardTransactionCounter);
    }

    //utilizado para las recargas
    public VRTTransaction(Long transactionId, OperationType operationType, LocalDateTime transactionDate, String serialCard,
                          Float transactionAmmount, String samId, String productId, Float initialBalance,
                          Float finalBalance, String samTransactionCounter, Long cardTransactionCounter, Integer rechargeType) {
        this.transactionId = transactionId;
        this.operationType = operationType;
        this.transactionDate = transactionDate;
        this.serialCard = serialCard;
        this.transactionAmmount = transactionAmmount;
        this.samId = samId;
        this.productId = setProduct(productId);
        this.initialBalance = initialBalance;
        this.finalBalance = finalBalance;
        this.samTransactionCounter = samTransactionCounter;
        this.cardTransactionCounter = cardTransactionCounter;
        this.rechargeType = getFIMPEFormatOperation(rechargeType);
        this.transactionFoil = generateFoil(serialCard, cardTransactionCounter);
    }

    //Utilizado para las recargas QR
    public VRTTransaction(Long transactionId, OperationType operationType, LocalDateTime transactionDate,
                          String serialCard, Float transactionAmmount) {
        this.transactionId = transactionId;
        this.operationType = operationType;
        this.transactionDate = transactionDate;
        this.serialCard = serialCard.substring(0, 20);
        transactionFoil = serialCard;
        this.transactionAmmount = transactionAmmount;
        initialBalance = 0.0F;
        finalBalance = 0.0F;
        productId = QR;
        samId = "0";
        samTransactionCounter = "0";
        cardTransactionCounter = transactionId;
        if(operationType.equals(OperationType.RECHARGE_QR_OK_VRT))
            rechargeType = "001";
        if(operationType.equals(OperationType.RECHARGE_QR_OK_COMMISSION_VRT)){
            rechargeType = "009";
            transactionFoil = transactionFoil + rechargeType;
        }
    }

    /**
     * Compara y asigna los identificadores del producto
     * @param product identificador del producto a comparar
     * @return Producto con los identificadores correspondientes
     */
    public Product setProduct(String product){
        if(product.equalsIgnoreCase(DEBIT.getBeaValue())){
            return DEBIT;
        }
        if(product.equalsIgnoreCase(CREDIT.getBeaValue())){
            return CREDIT;
        }
        if(product.equalsIgnoreCase(BPD1.getBeaValue())){
            return BPD1;
        }
        if(product.equalsIgnoreCase(BPD2.getBeaValue())){
            return BPD2;
        }
        return DEFAULT;
    }

    /**
     * Genera un numero de folio compuesto del idientificador de la tarjeta y el consecutivo de la aplicacion
     * El consecutivo es convertido a hexadecimal y concatenado al final del identificador
     * @param serialCard identificador de la tarjeta
     * @param cardTransactionCounter consecutivo de la tarjeta
     * @return Folio de la transaccion
     */
    private String generateFoil(String serialCard, Long cardTransactionCounter) {
        return serialCard + String.format("%06X", cardTransactionCounter);
    }

    /**
     * Da formato a un numero, rellena con ceros el inicio ajustando la cadena a tres digitos
     * Si es un 10 quedaria 010, o un 1 seria 001
     * @param debitType Numero a formatear
     * @return Cadena que representa un numero de 3 digitos relleno de ceros al inicio
     */
    private String getFIMPEFormatOperation(Integer debitType) {
        if(debitType == 0)
            debitType = 1;
        return String.format("%03d" , debitType);
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getTransactionFoil() {
        return transactionFoil;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getSerialCard() {
        return serialCard;
    }

    public Float getTransactionAmmount() {
        return transactionAmmount;
    }

    public String getSamId() {
        return samId;
    }

    public String getProfile() {
        return profile;
    }

    public ProductSale getProductCreditId() {
        return productCreditId;
    }

    public ProductSale getProductMoneyId() {
        return productMoneyId;
    }

    public Product getProductId() {
        return productId;
    }

    public Float getInitialBalance() {
        return initialBalance;
    }

    public Float getFinalBalance() {
        return finalBalance;
    }

    public String getSamTransactionCounter() {
        return samTransactionCounter;
    }

    public Long getCardTransactionCounter() {
        return cardTransactionCounter;
    }

    public String getRechargeType() {
        return rechargeType;
    }

    public OperationType getOperationType() {
        return operationType;
    }
}
