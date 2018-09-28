package ru.vachok.money.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.money.components.CalculatorForSome;
import ru.vachok.money.components.Currencies;
import ru.vachok.money.components.MyOpel;
import ru.vachok.money.components.ThrowMeMaybe;
import ru.vachok.money.other.MailMessages;
import ru.vachok.money.other.SpeedRunActualize;


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
        return new SpeedRunActualize();
    }

    @Bean
    @Scope("singleton")
    public MyOpel myOpel(SpeedRunActualize speedRunActualize) {
        MyOpel myOpel = new MyOpel();
        try {
            myOpel.setAvgSpeedA107(speedRunActualize.avgInfo(0));
            myOpel.setAvgSpeedRiga(speedRunActualize.avgInfo(1));
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
        return myOpel;
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

    public Currencies currencies() {
        return new Currencies();
    }

    public ThrowMeMaybe throwMeMaybe() {
        return new ThrowMeMaybe();
    }
}