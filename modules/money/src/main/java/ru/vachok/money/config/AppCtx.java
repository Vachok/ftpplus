package ru.vachok.money.config;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import ru.vachok.money.ConstantsFor;

/**
 * @since 14.09.2018 (11:17)
 */
@Configuration
public class AppCtx {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

    public static AnnotationConfigApplicationContext getCtx() {
        minimalConf();
        return ctx;
    }

    private static void minimalConf() {
        ctx.scan("ru.vachok.money.services");
        ctx.scan("ru.vachok.money.components");
        ctx.setDisplayName(ConstantsFor.APP_NAME);
        ctx.refresh();
    }


}
