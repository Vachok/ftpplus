package ru.vachok.money.moneypack;


/**
 <b>Банковские продукты</b>

 @since 20.08.2018 (11:31) */
public class Banking {

    private Integer idBanking = null;

    private String product = null;

    private double value = 0.0;


    public Banking(String product, int value) {
        this.product = product;
        this.value = value;
    }


    Integer getIdBanking() {
        return idBanking;
    }


    void setIdBanking(Integer idBanking) {
        this.idBanking = idBanking;
    }


    String getProduct() {
        return product;
    }


    void setProduct(String product) {
        this.product = product;
    }


    double getValue() {
        return value;
    }


    void setValue(double value) {
        this.value = value;
    }
}
