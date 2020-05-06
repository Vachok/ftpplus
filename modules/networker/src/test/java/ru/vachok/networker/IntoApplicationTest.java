// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.boot.origin.Origin;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventPublicationInterceptor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.events.MyEvent;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;


/**
 @see IntoApplication */
public class IntoApplicationTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 4));
        testConfigureThreadsLogMaker.before();
    }

    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }

    @Test
    @Ignore
    public void testMain() {
        try {
            IntoApplication.main(new String[]{"test"});
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }

    @Test
    @Ignore
    public void testBeforeSt() {
        IntoApplication.setUTF8Enc();
        Assert.assertTrue(new File(FileNames.SYSTEM).lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)));
    }

    @Test
    @Ignore
    public void contextTest() {
        try (ConfigurableApplicationContext context = IntoApplication.getContext()) {
            ConfigurableEnvironment environment = context.getEnvironment();
            StringBuilder stringBuilder = new StringBuilder();
            for (PropertySource<?> propertySource : environment.getPropertySources()) {
                String propSourceName = propertySource.getName();
                propSourceName = MessageFormat.format("{0} class: {1}", propSourceName, propertySource.getClass().getCanonicalName());
                if (propertySource instanceof EnumerablePropertySource) {
                    stringBuilder.append(propSourceName).append(" ***").append("\n\n");
                    for (String propertyName : ((EnumerablePropertySource<Map<String, Object>>) propertySource).getPropertyNames()) {
                        stringBuilder.append(propertyName).append(":").append(propertySource.getProperty(propertyName)).append("\n");
                    }
                }

            }
            stringBuilder.append(environment.getProperty("build.version"));
            PropertySource<?> appConf = environment.getPropertySources().get("applicationConfig");
            if (appConf != null) {
                Origin from = Origin.from(appConf.getName());
                stringBuilder.append(from.getClass().getSimpleName());
            }
            System.out.println("stringBuilder = " + stringBuilder.toString());
            stringBuilder.append("\n\n\n");
            context.setId(MessageFormat.format("{0}.{1}-{2}", MyCalen.getWeekNumber(), LocalDate.now().getDayOfWeek().getValue(), (int) (LocalTime.now()
                .toSecondOfDay() / ConstantsFor.ONE_HOUR_IN_MIN)));
            EventPublicationInterceptor eventPublicationInterceptor;
            ApplicationEvent event = new MyEvent(context);
            context.addApplicationListener(new ApplicationListener<ApplicationEvent>() {
                @Override
                public void onApplicationEvent(ApplicationEvent event) {
                    stringBuilder.append(getClass().getName());
                    stringBuilder.append(event.getTimestamp()).append(" : ").append(event.toString());
                }
            });
            context.getBeanDefinitionNames();
            showInFile(stringBuilder.toString());
        }
    }

    private void showInFile(String ctxProps) {
        String toFilePath = FileSystemWorker.writeFile("PropertySources.txt", ctxProps);
        try {
            Runtime.getRuntime().exec("\"C:\\Program Files (x86)\\Notepad++\\notepad++.exe\" \"" + toFilePath + "\"");
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

}