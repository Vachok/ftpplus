package ru.vachok.networker.logic;


import java.util.Queue;
import java.util.function.BiFunction;

/**
 @since 26.09.2018 (13:47) */

public class SimpleCalculator {

    public double countDoubles(Queue<Double> doubleQueue) {
        BiFunction<Double, Queue<Double>, Double> plusAction = (aDouble, doubles) -> {
            aDouble = 0d;
            while (doubles.iterator().hasNext()) {
                aDouble = doubles.poll();
                aDouble += aDouble;
            }
            return aDouble;
        };
        double resultDouble = 0d;
        return plusAction.apply(resultDouble, doubleQueue);
    }

}
