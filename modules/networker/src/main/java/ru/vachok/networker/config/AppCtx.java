package ru.vachok.networker.config;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;


/**
 @since 30.08.2018 (13:22) */
@EnableAutoConfiguration
public class AppCtx extends AnnotationConfigApplicationContext {

    @SuppressWarnings("resource")
    private static AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext();

    private static final String SOURCE_CLASS = AppCtx.class.getSimpleName();

    private static Queue<String> miniLogger = new PriorityQueue<>();

    private AutowireCapableBeanFactory autowireCapableBeanFactory = this.getAutowireCapableBeanFactory();

    @Override
    public AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
        for (String s : configApplicationContext.getBeanDefinitionNames()) {
            LoggerFactory.getLogger(SOURCE_CLASS).info(s);
        }
        return autowireCapableBeanFactory;
    }

    public AppCtx() {

        this.resetCommonCaches();
        this.setDisplayName(ConstantsFor.APPNAME_WITHMINUS.replace("-", ""));
        this.refresh();
    }

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
        String msg = new StringBuilder()
            .append("<p><h3><center>Context</center></h3><b><br>Context loaded. Bean names:</b><br>")
            .append(new TForms().fromArray(configApplicationContext.getBeanDefinitionNames(), false))
            .append("<p>").toString();
        stringBuilder.append(msg);
        miniLogger.add(stringBuilder.toString());
        miniLogger.add(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX);
        miniLogger.add(AbstractApplicationContext.LIFECYCLE_PROCESSOR_BEAN_NAME);
        FileSystemWorker.writeFile(SOURCE_CLASS + ".qadd", miniLogger.stream());
        moveProps();
    }

    /**
     * Двигает .properties в properties/
     */
    private static void moveProps() {
        File[] filesInRoot = new File(".").listFiles();
        for (File f : Objects.requireNonNull(filesInRoot)) {
            if (f.isFile() && f.getName().toLowerCase().contains(ConstantsFor.FILEEXT_PROPERTIES)) {
                String pathToCopyWithFileName = String.valueOf(Paths.get(".\\props\\" + f.getName()));
                boolean isMoved = FileSystemWorker.copyOrDelFile(f, pathToCopyWithFileName, true);
                miniLogger.add(pathToCopyWithFileName);
                miniLogger.add(f.getName() + " is moved = " + isMoved);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppCtx{");
        sb.append("miniLogger=").append(new TForms().fromArray(miniLogger, false));
        sb.append('}');
        return sb.toString();
    }
}
