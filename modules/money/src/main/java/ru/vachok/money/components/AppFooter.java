package ru.vachok.money.components;


import org.springframework.stereotype.Component;


/**
 @since 03.10.2018 (22:38) */
@Component
public class AppFooter {

    public String getTheFooter() {
        StringBuilder stringBuilder = new StringBuilder();
        String razDel = "   |   ";
        stringBuilder
            .append("<center><h3>Навигация</h3></center>")
            .append("<a href=\"/\">MAIN</a>")
            .append(razDel)
            .append("<a href=\"/calc\">Calculator</a>")
            .append(razDel)
            .append("<a href=\"/chkcar\">Roads checker</a>")
            .append(razDel)
            .append("<a href=\"/carinfo\">MAF sensor</a>")
            .append(razDel)
            .append("<a href=\"/money\">Money</a>")
            .append(razDel)
            .append("<a href=\"/sysinfo\">System information</a>")
            .append(razDel)
            .append("<a href=\"/nav\">NAV Test</a>");
        return stringBuilder.toString();
    }
}