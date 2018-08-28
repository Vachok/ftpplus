package ru.vachok.money;



import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.boot.SpringApplication.run;


@EnableAutoConfiguration
@SpringBootApplication
public class MoneyApplication {


    public static void main( String[] args ) {
        run(MoneyApplication.class , args);
        new Thread(() -> ConstantsFor.scheduleSpeedAct = MoneyApplication.scheduleSpeedAct()).start();
    }


    public static String scheduleSpeedAct() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<String> r = new SpeedRunActualize();
        String s = null;
        try {
            s = executorService.submit(r).get();
        } catch (InterruptedException | ExecutionException e) {
            ApplicationConfiguration.getLogger().error(e.getMessage() , e);
            Thread.currentThread().interrupt();
        }
        return s;
    }
}
