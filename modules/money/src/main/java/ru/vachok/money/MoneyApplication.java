package ru.vachok.money;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.services.ParserCBRru;
import ru.vachok.money.services.TForms;

import javax.xml.stream.XMLStreamException;

import static org.springframework.boot.SpringApplication.run;


@EnableAutoConfiguration
@SpringBootApplication
public class MoneyApplication {


    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    public static void main(String[] args) {
        run(MoneyApplication.class , args);
        ParserCBRru cbrBean = ConstantsFor.CONTEXT.getBean(ParserCBRru.class);
        try {
            LOGGER.info(new TForms().toStringFromArray(cbrBean.parseList()));
        } catch (XMLStreamException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info(cbrBean.usdCur());
    }
}
