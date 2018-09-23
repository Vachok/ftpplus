package ru.vachok.money.config;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import ru.vachok.money.MoneyApplication;



/**
 * @since 14.09.2018 (11:17)
 */
@Configuration
public class AppCtx extends AnnotationConfigApplicationContext {

    static SpringApplication getApplication() {
        SpringApplication application = new SpringApplication();
        application.setMainApplicationClass(MoneyApplication.class);
        return application;
    }

    public static AnnotationConfigApplicationContext getCtx() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.scan("ru.vachok.money.services");
        ctx.scan("ru.vachok.money.components");
        ctx.setDisplayName(ConstantsFor.APP_NAME);
        ctx.addApplicationListener(new AppEventListener());
        ctx.refresh();
        return ctx;
    }
}
