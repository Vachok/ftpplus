package ru.vachok.money.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.money.components.*;
import ru.vachok.money.other.MailMessages;
import ru.vachok.money.services.CalcSrv;
import ru.vachok.money.services.CookieMaker;
import ru.vachok.money.services.VisitorSrv;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;

import javax.servlet.http.Cookie;


/**
 @since 09.09.2018 (13:02) */
@ComponentScan
public class AppComponents {

    @Bean
    @Scope ("singleton")
    public static SpeedRunActualize getSpeedActualizer() {
        return new SpeedRunActualize();
    }

    @Bean
    public ParserCBRruSRV parserCBRruSRV(){
        return new ParserCBRruSRV(currencies());
    }

    @Bean
    public CookIES cookIES() {
        Cookie cookie = new Cookie("id", this.appVersion().toString());
        CookieMaker cookieMaker = new CookieMaker();
        return new CookIES(cookieMaker);
    }

    @Bean
    public AppVersion appVersion() {
        return new AppVersion();
    }

    @Bean
    @Scope ("singleton")
    public MyOpel myOpel(SpeedRunActualize speedRunActualize) {
        MyOpel myOpel = new MyOpel();
        myOpel.setCarName("Astra");
        myOpel.setGosNum("A939OO190");
        return myOpel;
    }

    @Bean
    @Scope ("singleton")
    public MailMessages mailMessages() {
        return new MailMessages();

    }

    @Bean
    @Scope ("prototype")
    public Currencies currencies() {
        return new Currencies();
    }

    @Bean
    @Scope ("prototype")
    public Visitor visitor(CookieMaker cookieMaker) {
        VisitorSrv visitorSrv = new VisitorSrv(cookieMaker);
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
}