package com.idear.fimpe.torniquete.domain;

import com.idear.fimpe.enums.Product;

import java.time.LocalDateTime;

import static com.idear.fimpe.enums.Product.*;

public class TorniqueteTransaction {

    private Long transactionId;
    private String transactionFoil;//cambiar a long
    private LocalDateTime transactionDate;
    private String serialCard;
    private Product productId;//enum
    private Float transactionAmount;
    private Float initialBalance;
    private Float finalBalance;
    private String samId;
    private String samTransactionCounter;
    private Long cardTransactionCounter;
    private String debitType;//enum
    private String rechargeType;//enum

    //Constructor para debitos de tarjeta
    public TorniqueteTransaction(Long transactionId, LocalDateTime transactionDate, String serialCard, String product,
                          Float transactionAmmount, Float initialBalance, Float finalBalance, Float initialBPD, Float finalBBPD,
                          String samId, String samTransactionCounter, Long cardTransactionCounter, Integer debitType
                          ) {

        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.serialCard = serialCard;
        this.samId = samId;
        this.samTransactionCounter = samTransactionCounter;
        this.cardTransactionCounter = cardTransactionCounter;
        //Se rellena el numero con ceros al incio, ejemplo
        this.debitType = getFIMPEFormatOperation(debitType);
        this.rechargeType = this.debitType;
        //this.busId = busId;
        // this.routeId = routeId;

        //relaciona los valores de productos BEA a FIMPE
        this.productId = setProduct(product);

        //Asigna los saldos segun el producto
        if(isBPDProduct(productId)){
            this.transactionAmount = 1F;
            this.initialBalance = initialBPD;
            this.finalBalance = finalBBPD;
        } else{
            this.transactionAmount = transactionAmmount;
            this.initialBalance = initialBalance;
            this.finalBalance = finalBalance;
        }

        this.transactionFoil = generateFoil(serialCard, cardTransactionCounter);
    }

    //Constructor para debitos de QR
    public TorniqueteTransaction(Long transactionId, LocalDateTime transactionDate,
                                 String serialCard, Float transactionAmmount, Float initialBalance, Float finalBalance) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        //rellena con ceros a la izquierda del valor ajustando 14 caracteres en total
        serialCard = String.format("%1$" + 14 + "s", serialCard).replace(' ', '0');
        this.serialCard = serialCard;
        this.transactionFoil = serialCard;
        this.transactionAmount = transactionAmmount;
        this.initialBalance = initialBalance;
        this.finalBalance = finalBalance;
        this.productId = QR;
        this.samId = "0";
        this.samTransactionCounter = "0";
        this.cardTransactionCounter = transactionId;
        this.debitType = "001";
    }

    //Constructor para debitos de tarjeta bancaria
    public TorniqueteTransaction(Long transactionId, LocalDateTime transactionDate,
                                 String samId, String transactionReceipt, String panMasked, Float transactionAmount) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        String panMaskFirstPart = panMasked.substring(0, 4);
        String panMaskLastPart = panMasked.substring(12, 16);
        this.serialCard = panMaskFirstPart.concat("000000").concat(panMaskLastPart);
        this.transactionFoil = transactionReceipt;
        this.transactionAmount = transactionAmount;
        this.initialBalance = 0F;
        this.finalBalance = 0F;
        this.productId = TB;
        this.samId = samId;
        this.samTransactionCounter = "0";
        this.cardTransactionCounter = 0L;
        this.debitType = "001";
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

    public Product getProductId() {
        return productId;
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

    public Float getTransactionAmount() {
        return transactionAmount;
    }
}
