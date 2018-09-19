package ru.vachok.networker.config;


import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import ru.vachok.networker.ConstantsFor;

import javax.servlet.ServletContext;

/**
 * @since 19.09.2018 (9:07)
 */
public class ControlAndContextTH {

    private final TemplateEngine templateEngine;

    public ControlAndContextTH(final ServletContext servletContext) {
        super();
        ServletContextTemplateResolver servletContextTemplateResolver = new ServletContextTemplateResolver(servletContext);
        servletContextTemplateResolver.setTemplateMode(TemplateMode.HTML);
        servletContextTemplateResolver.setCacheTTLMs(ConstantsFor.CACHE_TIME_MS);
        servletContextTemplateResolver.setCacheable(false);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(servletContextTemplateResolver);
    }

}
