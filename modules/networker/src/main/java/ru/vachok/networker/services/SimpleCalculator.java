package ru.vachok.networker.services;


import org.springframework.stereotype.Service;

import java.util.List;

/**
 @since 26.09.2018 (13:47) */
@Service
public class SimpleCalculator {

    public double countDoubles(List<Double> doubleList) {
        double resultDouble = 0d;
        for (Double aDouble : doubleList) {
            resultDouble += aDouble;
        }
        return resultDouble;
    }

}
