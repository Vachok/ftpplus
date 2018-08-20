package ru.vachok.networker.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


@EnableAutoConfiguration
@ComponentScan
public class ApplicationConfiguration {


   @Bean
   public static Logger logger() {
      return LoggerFactory.getLogger("ru.vachok");
   }
}
