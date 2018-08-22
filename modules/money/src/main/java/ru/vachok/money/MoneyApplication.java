package ru.vachok.money;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@EnableAutoConfiguration
@SpringBootApplication
public class MoneyApplication {

    public static void main( String[] args ) {
        SpringApplication.run(MoneyApplication.class , args);
    }
}
