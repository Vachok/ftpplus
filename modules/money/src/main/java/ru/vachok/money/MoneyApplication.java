package ru.vachok.money;


import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import java.util.concurrent.Callable;

import static org.springframework.boot.SpringApplication.run;


@EnableAutoConfiguration
@SpringBootApplication
public class MoneyApplication {


    /*Fields*/
    private static final Logger LOGGER = ApplicationConfiguration.getLogger();

    public static void main(String[] args) {
        run(MoneyApplication.class , args);
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
    }


    public static String scheduleSpeedAct(Scheduler scheduler) {
        Callable<String> r = new SpeedRunActualize();
        try{
            throw new UnsupportedOperationException("06.09.2018 (2:22)"); //todo
        }
        catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        }
        return "06.09.2018 (2:23) todo";
    }

    private static JobFactory jobFactory(Scheduler scheduler) {
        JobFactory jobFactory = new SpringBeanJobFactory();
        try{
            scheduler.setJobFactory(jobFactory);
        }
        catch(SchedulerException e){
            LOGGER.error(e.getMessage(), e);
        }
        return jobFactory;
    }
}
