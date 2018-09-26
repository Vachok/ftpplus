package ru.vachok.money.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.money.components.*;
import ru.vachok.money.other.MailMessages;
import ru.vachok.money.other.SpeedRunActualize;
import ru.vachok.money.services.AppVerSrv;


/**
 * @since 09.09.2018 (13:02)
 */
@ComponentScan
public class AppComponents {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(AppComponents.class.getSimpleName());


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

    @Bean
    public AppVerSrv appVerSrv() {
        return new AppVerSrv(appVersion());
    }

    @Bean
    @Scope ("singleton")
    public static AppVersion appVersion() {
        final AppVersion appVersion = new AppVersion();
        final int genericId = AppVersion.GENERIC_ID;
        String msg = "appVersion wanted! ID this version = " + genericId;
        LOGGER.warn(msg);
        return new AppVersion();
    }
}