package ru.vachok.money.calc;


import org.springframework.stereotype.Component;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 09.09.2018 (14:55) */
@Component ("CalculatorForSome")
public class CalculatorForSome {

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CalculatorForSome.class.getSimpleName();

    private long uLong;

    private List<Double> userDouble;

    private String userInput;

    /**
     {@link }
     */
    private static MessageToUser messageToUser = new MessageCons();

    private Map<String, Double> calcDouble = new ConcurrentHashMap<>();

    private Map<String, Integer> calcInt = new ConcurrentHashMap<>();

    public Map<String, Double> getCalcDouble() {
        return calcDouble;
    }

    public void setCalcDouble(Map<String, Double> calcDouble) {
        this.calcDouble = calcDouble;
    }

    public Map<String, Integer> getCalcInt() {
        return calcInt;
    }

    public void setCalcInt(Map<String, Integer> calcInt) {
        this.calcInt = calcInt;
    }

    public long getuLong() {
        return uLong;
    }

    public void setuLong(long uLong) {
        this.uLong = uLong;
    }

    public List<Double> getUserDouble() {
        return userDouble;
    }

    public void setUserDouble(List<Double> userDouble) {
        this.userDouble = userDouble;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CalculatorForSome{");
        sb.append("calcDouble=").append(calcDouble);
        sb.append(", calcInt=").append(calcInt);
        sb.append(", messageToUser=").append(messageToUser);
        sb.append(", SOURCE_CLASS='").append(SOURCE_CLASS).append('\'');
        sb.append(", uLong=").append(uLong);
        sb.append(", userDouble=").append(userDouble);
        sb.append(", userInput='").append(userInput).append('\'');
        sb.append('}');
        return sb.toString();
    }
}