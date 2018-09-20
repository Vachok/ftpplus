package ru.vachok.money.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.money.components.CalculatorForSome;
import ru.vachok.money.components.MyOpel;
import ru.vachok.money.other.MailMessages;
import ru.vachok.money.other.SpeedRunActualize;
import ru.vachok.money.services.ParserCBRru;


/**
 * @since 09.09.2018 (13:02)
 */
@ComponentScan
public class AppComponents {



    @Bean
    @Scope("singleton")
    public static SpeedRunActualize getSpeedActualizer() {
        SpeedRunActualize speedRunActualize = new SpeedRunActualize();
        String msg1 = speedRunActualize.avgInfo(0) + " A107";
        String msg = speedRunActualize.avgInfo(1) + " Riga";
        return speedRunActualize;
    }

    @Bean
    @Scope("singleton")
    public MyOpel myOpel() {
        return new MyOpel();
    }

    @Bean("CalculatorForSome")
    @Scope("prototype")
    public CalculatorForSome calculatorForSome() {
        return new CalculatorForSome();
    }

    @Bean
    @Scope("singleton")
    public MailMessages mailMessages() {
        return new MailMessages();

    }

    @Bean
    @Scope("singleton")
    public ParserCBRru parserCBRru() {
        return ParserCBRru.getParser();
    }
}