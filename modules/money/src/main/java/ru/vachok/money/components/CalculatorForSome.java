package ru.vachok.money.components;


import org.springframework.stereotype.Component;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 09.09.2018 (14:55) */
@Component ("CalculatorForSome")
public class CalculatorForSome {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CalculatorForSome.class.getSimpleName();

    private int userInt;

    private double userDouble;

    private String userInput;

    @Override
    public String toString() {
        return "CalculatorForSome{" +
            "calcDouble=" + calcDouble +
            ", calcInt=" + calcInt +
            ", messageToUser=" + messageToUser +
            ", SOURCE_CLASS='" + SOURCE_CLASS + '\'' +
            ", userDouble=" + userDouble +
            ", userInput='" + userInput + '\'' +
            ", userInt=" + userInt +
            '}';
    }

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

    public int getUserInt() {
        return userInt;
    }

    public void setUserInt(int userInt) {
        this.userInt = userInt;
    }

    public double getUserDouble() {
        return userDouble;
    }

    public void setUserDouble(double userDouble) {
        this.userDouble = userDouble;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    private void showUserIn() {
        messageToUser.infoNoTitles(userInput);
    }
}