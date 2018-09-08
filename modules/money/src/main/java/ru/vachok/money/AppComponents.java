package ru.vachok.money;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.web.beans.CalculatorForSome;
import ru.vachok.money.web.beans.MyOpel;


/**
 @since 09.09.2018 (13:02) */
@ComponentScan
public class AppComponents {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = AppComponents.class.getSimpleName();

    /**
     {@link }
     */
    private static MessageToUser messageToUser = new MessageCons();

    @Bean
    @Scope ("singleton")
    public MyOpel myOpel() {
        return new MyOpel();
    }

    @Bean
    @Scope ("prototype")
    public CalculatorForSome calculatorForSome() {
        return new CalculatorForSome();
    }
}