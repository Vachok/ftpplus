package ru.vachok.money.config;


import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.MoneyApplication;
import ru.vachok.money.other.SpeedRunActualize;


/**
 * @since 14.09.2018 (11:17)
 */
@Configuration
public class AppCtx {

    private static AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

    private static SpringApplication application = new SpringApplication();
    private static final Logger LOGGER = AppComponents.getLogger();

    public static AnnotationConfigApplicationContext getCtx() {
        minimalConf();
        Thread thread = new Thread(()->new SpeedRunActualize().call());
        thread.setName(SpeedRunActualize.class.getSimpleName());
        thread.start();
        String msg = "Thread " + thread.getName() + " is alive: " + thread.isAlive();
        LOGGER.warn(msg);
        return ctx;
    }

    static SpringApplication getApplication() {
        application.setMainApplicationClass(MoneyApplication.class);
        return application;
    }

    private static void minimalConf() {
        ctx.scan("ru.vachok.money.services");
        ctx.scan("ru.vachok.money.components");
        ctx.setDisplayName(ConstantsFor.APP_NAME);
        ctx.addApplicationListener(new AppEventListener());
        ctx.refresh();
    }
}
