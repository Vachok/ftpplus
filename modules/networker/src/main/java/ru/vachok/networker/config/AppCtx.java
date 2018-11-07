package ru.vachok.networker.config;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.util.PriorityQueue;
import java.util.Queue;


/**
 * @since 30.08.2018 (13:22)
 */
@EnableAutoConfiguration
public class AppCtx extends AnnotationConfigApplicationContext {

    public AppCtx() {

        this.resetCommonCaches();
        this.setDisplayName(ConstantsFor.APP_NAME.replace("-", ""));
        this.setResourceLoader(new ResLoader());
        this.refresh();
    }

    private static final String SOURCE_CLASS = AppCtx.class.getSimpleName();

    private static Queue<String> outQueue = new PriorityQueue<>();

    private AutowireCapableBeanFactory autowireCapableBeanFactory = this.getAutowireCapableBeanFactory();

    @Override
    public AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
        for (String s : configApplicationContext.getBeanDefinitionNames()) {
            LoggerFactory.getLogger(SOURCE_CLASS).info(s);
        }
        return autowireCapableBeanFactory;
    }

    private static AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext();
    public static AnnotationConfigApplicationContext scanForBeansAndRefreshContext() {
        configApplicationContext.scan("ru.vachok.networker.componentsrepo");
        configApplicationContext.scan("ru.vachok.networker.services");
        configApplicationContext.scan("ru.vachok.networker.config");
        qAdd();
        return configApplicationContext;
    }
    private static void qAdd() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<p><center><h3>Application Context</center></h3>");
        String msg = "<p><h3><center>Context</center></h3><b><br>Context loaded. Bean names:</b><br>" +
            new TForms().fromArray(configApplicationContext.getBeanDefinitionNames()) +
            "</p>";
        stringBuilder.append(msg);
        outQueue.add(msg);
        outQueue.add(AppCtx.CLASSPATH_ALL_URL_PREFIX);
        outQueue.add(AppCtx.LIFECYCLE_PROCESSOR_BEAN_NAME);
    }
}
