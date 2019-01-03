package ru.vachok.money.banking;


import org.springframework.stereotype.Component;
import ru.vachok.money.ConstantsFor;

import java.time.LocalDate;

/**
 Created by 14350 on 20.09.2018 23:20
 */
@Component (ConstantsFor.CURRENCY)
public class Currencies {

    public static final float USD_2014 = 34.26f;

    public static final float E_2014 = 46.8985f;

    private static final LocalDate DATE_2014 = LocalDate.of(2014, 1, 26);

    private float euro;

    private float usDollar;

    private float howManyWas;

    private float percentBanka;

    public float getHowManyWas() {
        return howManyWas;
    }

    void setHowManyWas(float howManyWas) {
        this.howManyWas = howManyWas;
    }

    public float getPercentBanka() {
        return percentBanka;
    }

    void setPercentBanka(float percentBanka) {
        this.percentBanka = percentBanka;
    }

    public static float getUsd2014() {
        return USD_2014;
    }

    public static float getE2014() {
        return E_2014;
    }

    public static LocalDate getDate2014() {
        return DATE_2014;
    }

    public float getEuro() {
        return euro;
    }

    public void setEuro(float euro) {
        this.euro = euro;
    }

    public float getUsDollar() {
        return usDollar;
    }

    public void setUsDollar(float usDollar) {
        this.usDollar = usDollar;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Currencies{");
        sb.append("DATE_2014=").append(DATE_2014);
        sb.append(", E_2014=").append(E_2014);
        sb.append(", euro=").append(euro);
        sb.append(", howManyWas=").append(howManyWas);
        sb.append(", percentBanka=").append(percentBanka);
        sb.append(", USD_2014=").append(USD_2014);
        sb.append(", usDollar=").append(usDollar);
        sb.append('}');
        return sb.toString();
    }
}
