package ru.vachok.money.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.CalculatorForSome;
import ru.vachok.money.components.MyOpel;
import ru.vachok.money.services.MailMessages;
import ru.vachok.money.services.ParserCBRru;
import ru.vachok.money.services.SpeedRunActualize;


/**
 * @since 09.09.2018 (13:02)
 */
@ComponentScan
public class AppComponents {

    private static final Logger LOGGER = AppComponents.getLogger();

    @Bean
    @Scope("singleton")
    public static Logger getLogger() {
        return LoggerFactory.getLogger(ConstantsFor.DB_PREFIX + ConstantsFor.APP_NAME);
    }

    @Bean
    @Scope("singleton")
    public static SpeedRunActualize getSpeedActualizer() {
        SpeedRunActualize speedRunActualize = new SpeedRunActualize();
        String msg1 = speedRunActualize.avgInfo(0) + " A107";
        LOGGER.info(msg1);
        String msg = speedRunActualize.avgInfo(1) + " Riga";
        LOGGER.info(msg);
        return speedRunActualize;
    }

    @Bean
    @Scope("singleton")
    public MyOpel myOpel() {
        return new MyOpel();
    }

    @Bean
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