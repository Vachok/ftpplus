package ru.vachok.money.services;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.CalculatorForSome;
import ru.vachok.money.config.AppComponents;


/**
 @since 19.09.2018 (21:44) */
@Service ("CalcSrv")
public class CalcSrv {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CalcSrv.class.getSimpleName();

    /**
     {@link }
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    private static final AnnotationConfigApplicationContext CONTEXT = ConstantsFor.CONTEXT;

    public String resultCalc() {
        CalculatorForSome calculatorForSome = CONTEXT.getBean(CalculatorForSome.class);
        String result = calculatorForSome.getUserInput();
        return result;
    }
}