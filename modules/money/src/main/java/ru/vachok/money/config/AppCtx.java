package ru.vachok.money.config;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import ru.vachok.money.MoneyApplication;
import ru.vachok.money.services.TForms;

import javax.servlet.http.HttpServletRequest;


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
        ctx.refresh();
        return ctx;
    }

    public static String getCtxInfoFromRequest(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<center><h3>Инфо из контекста</h3></center>");
        stringBuilder.append(request.getServletContext().getServletContextName()).append("      |getServletContextName<br>");
        stringBuilder.append(request.getServletContext().getContextPath()).append("     |getContextPath<br>");
        stringBuilder.append(request.getServletContext().getServerInfo()).append("      |getServerInfo<br>");
        stringBuilder.append(request.getServletContext().getRealPath("/")).append("     |getRealPath(\"/\")<br>");
        stringBuilder.append(request.getServletContext().getVirtualServerName()).append("   |getVirtualServerName<br>");
        stringBuilder.append(new TForms()
            .enumToString(request.getServletContext().getInitParameterNames(), true)).append("   |getInitParameterNames<br>");
        stringBuilder.append(new TForms()
            .enumToString(request.getServletContext().getAttributeNames(), true)).append("   |getAttributeNames<br>");
        return stringBuilder.toString();
    }
}
