package com.idear.fimpe.cet.domain;

import com.idear.fimpe.enums.Product;

import java.time.LocalDateTime;

import static com.idear.fimpe.enums.Product.*;

public class CETTransaction {

    private Long transactionId;
    private String transactionFoil;
    private LocalDateTime transactionDate;
    private String serialCard;
    private Product productId;//enum
    private Float transactionAmmount;
    private Float initialBalance;
    private Float finalBalance;
    private String samId;
    private String samTransactionCounter;
    private Long cardTransactionCounter;
    private String debitType;//enum
    private String rechargeType;//enum

    public CETTransaction(Long transactionId, LocalDateTime transactionDate, String serialCard, String product,
                          Float transactionAmmount, Float initialBalance, Float finalBalance, Float initialBPD, Float finalBBPD,
                          String samId, String samTransactionCounter, Long cardTransactionCounter, Integer debitType) {

        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.serialCard = serialCard;
        this.samId = samId;
        this.samTransactionCounter = samTransactionCounter;
        this.cardTransactionCounter = cardTransactionCounter;
        //Se rellena el numero con ceros al incio, ejemplo
        this.debitType = getFIMPEFormatOperation(debitType);
        this.rechargeType = this.debitType;

        //relaciona los valores de productos BEA a FIMPE
        this.productId = setProduct(product);

        //Asigna los saldos segun el producto
        if(isBPDProduct(productId)){
            this.transactionAmmount = 1F;
            this.initialBalance = initialBPD;
            this.finalBalance = finalBBPD;
        } else{
            this.transactionAmmount = transactionAmmount;
            this.initialBalance = initialBalance;
            this.finalBalance = finalBalance;
        }

        this.transactionFoil = generateFoil(serialCard, cardTransactionCounter);
    }

    /**
     * Da formato a un numero, rellena con ceros el inicio ajustando la cadena a tres digitos
     * Si es un 10 quedaria 010, o un 1 seria 001
     * @param debitType Numero a formatear
     * @return Cadena que representa un numero de 3 digitos relleno de ceros al inicio
     */
    private String getFIMPEFormatOperation(Integer debitType) {
        return String.format("%03d" , debitType);
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
     * Compara y asigna los identificadores del producto
     * @param product identificador del producto a comparar
     * @return Producto con los identificadores correspondientes
     */
    private Product setProduct(String product){
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
     * Evalua si el producto es un BPD
     * @param product Producto a evaluar
     * @return Si es un BPD o no
     */
    private boolean isBPDProduct(Product product){
        return product.equals(BPD1) || product.equals(BPD2);
    }

    public String getTransactionFoil() {
        return transactionFoil;
    }

    public void setTransactionFoil(String transactionFoil) {
        this.transactionFoil = transactionFoil;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getSerialCard() {
        return serialCard;
    }

    public Product getProductId() {
        return productId;
    }

    public Float getTransactionAmmount() {
        return transactionAmmount;
    }

    public Float getInitialBalance() {
        return initialBalance;
    }

    public Float getFinalBalance() {
        return finalBalance;
    }

    public String getSamId() {
        return samId;
    }

    public String getSamTransactionCounter() {
        return samTransactionCounter;
    }

    public Long getCardTransactionCounter() {
        return cardTransactionCounter;
    }

    public String getDebitType() {
        return debitType;
    }

    public String getRechargeType() {
        return rechargeType;
    }
}
