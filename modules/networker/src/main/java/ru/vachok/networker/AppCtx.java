package ru.vachok.networker;


import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.PriorityQueue;
import java.util.Queue;


/**
 * @since 30.08.2018 (13:22)
 */
@Configuration
@EnableAsync
public class AppCtx {

    /*Fields*/
    private static final String SOURCE_CLASS = AppCtx.class.getSimpleName();

    private static Queue<String> outQueue = new PriorityQueue<>();

    private static AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext();

    static AnnotationConfigApplicationContext scanForBeansAndRefreshContext() {
        configApplicationContext.clearResourceCaches();
        configApplicationContext.scan("ru.vachok.networker.componentsrepo");
        configApplicationContext.scan("ru.vachok.networker.services");
        configApplicationContext.scan("ru.vachok.networker.config");
        configApplicationContext.setDisplayName(ConstantsFor.APP_NAME);
        qAdd();
        outQueue.add(configApplicationContext.getApplicationName());
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
    }

    public static AutowireCapableBeanFactory getBeanFactory() {
        String msg = "<i>" + SOURCE_CLASS + " return Autowire Capable Bean Factory</i>";
        outQueue.add(msg);
        return configApplicationContext.getAutowireCapableBeanFactory();
    }

    public static Queue<String> getClassLoaderURLList() {
        ClassLoader classLoader = configApplicationContext.getClassLoader();
        ClassLoader parent = classLoader.getParent();

        String msg = "<p><h3><center>Class Loaders</center></h3><h4>Loader from context:</h4>" +
            classLoader.getClass().getTypeName() +
            "<h4>Loader from parent:</h4><br>" +
            parent.getClass().getTypeName() +
            "</p>";
        outQueue.add(msg);
        return outQueue;
    }
}
