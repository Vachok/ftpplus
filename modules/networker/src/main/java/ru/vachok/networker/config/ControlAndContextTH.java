package ru.vachok.networker.config;


import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import ru.vachok.networker.TForms;

import javax.servlet.ServletContext;
import java.util.Set;

/**
 @since 19.09.2018 (9:07) */
public class ControlAndContextTH {

    private final TemplateEngine templateEngine;

    public ControlAndContextTH(final ServletContext servletContext) {
        super();
        ServletContextTemplateResolver servletContextTemplateResolver = new ServletContextTemplateResolver(servletContext);
        servletContextTemplateResolver.setTemplateMode(TemplateMode.HTML);
        servletContextTemplateResolver.setCacheable(false);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(servletContextTemplateResolver);
    }

    public TemplateEngine getTemplateEngine() {
        Set cacheSet = templateEngine.getCacheManager().getTemplateCache().keySet();
        LoggerFactory.getLogger(TemplateEngine.class.getSimpleName()).info(new TForms().fromArray(cacheSet, false));
        return templateEngine;
    }
}
