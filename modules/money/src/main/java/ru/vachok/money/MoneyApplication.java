package ru.vachok.money;



import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.springframework.boot.SpringApplication.run;


@EnableAutoConfiguration
@SpringBootApplication
public class MoneyApplication {


    public static void main( String[] args ) {
        run(MoneyApplication.class , args);
        scheduleSpeedAct();
    }


    private static void scheduleSpeedAct() {
        ScheduledExecutorService scheduledExecutorService = Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        Runnable r = new SpeedRunActualize();
        scheduledExecutorService.scheduleWithFixedDelay(r , ConstantsFor.INITIAL_DELAY , ConstantsFor.DELAY , TimeUnit.SECONDS);
    }
}
