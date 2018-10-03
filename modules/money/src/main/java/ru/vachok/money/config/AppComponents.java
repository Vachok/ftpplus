package ru.vachok.money.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.money.components.*;
import ru.vachok.money.services.*;

import javax.servlet.http.Cookie;


/**
 @since 09.09.2018 (13:02) */
@ComponentScan
public class AppComponents {

    @Bean
    public ParserCBRruSRV parserCBRruSRV() {
        return new ParserCBRruSRV(currencies());
    }

    @Bean
    @Scope ("prototype")
    public Currencies currencies() {
        return new Currencies();
    }

    @Bean
    public CookIES cookIES() {
        Cookie cookie = new Cookie("id", appVersion().toString());
        CookieMaker cookieMaker = new CookieMaker();
        return new CookIES(cookieMaker);
    }

    @Bean
    public URLParser urlParser() {
        return new URLParser(new URLContent());
    }

    @Bean
    public AppVersion appVersion() {
        return new AppVersion();
    }

    @Bean
    @Scope ("prototype")
    public Visitor visitor() {
        VisitorSrv visitorSrv = new VisitorSrv(new CookieMaker());
        return new Visitor(visitorSrv);
    }

    @Bean
    @Scope ("prototype")
    public CalcSrv calcSrv() {
        return new CalcSrv(calculatorForSome());
    }

    @Bean ("CalculatorForSome")
    @Scope ("prototype")
    public CalculatorForSome calculatorForSome() {
        return new CalculatorForSome();
    }

    @Bean
    @Scope ("singleton")
    public static MyOpel myOpel() {
        return MyOpel.getI();
    }
}