package ru.vachok.money.banking;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.StringJoiner;

/**
 Created by 14350 on 20.09.2018 23:20
 */
@Component("currency")
public class Currencies {

    private static final float USD_2014 = 34.26f;

    private static final float E_2014 = 46.8985f;

    private static final LocalDate DATE_2014 = LocalDate.of(2014, 01, 26);

    private double euro;

    private double usDollar;

    public static float getUsd2014() {
        return USD_2014;
    }

    public static float getE2014() {
        return E_2014;
    }

    public static LocalDate getDate2014() {
        return DATE_2014;
    }

    public double getEuro() {
        return euro;
    }

    public void setEuro(double euro) {
        this.euro = euro;
    }

    public double getUsDollar() {
        return usDollar;
    }

    public void setUsDollar(double usDollar) {
        this.usDollar = usDollar;
    }

    @Override
    public String toString() {
        return new StringJoiner("\n", Currencies.class.getSimpleName() + "\n", "\n")
            .add("euro=" + euro)
            .add("usDollar=" + usDollar)
            .toString();
    }
}
