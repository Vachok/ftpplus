package ru.vachok.money.components;


import org.springframework.stereotype.Component;


/**
 @since 03.10.2018 (22:38) */
@Component
public class AppFooter {

    public String getTheFooter() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append("<center><h3>Навигация</h3></center>")
            .append("<a href=\"/\">MAIN</a>")
            .append("   |   ")
            .append("<a href=\"/calc\">Calculator</a>")
            .append("   |   ")
            .append("<a href=\"/chkcar\">Roads checker</a>")
            .append("   |   ")
            .append("<a href=\"/ftp\">FTP</a>")
            .append("   |   ")
            .append("<a href=\"/money\">Money</a>")
            .append("   |   ")
            .append("<a href=\"/sysinfo\">System information</a>");
        return stringBuilder.toString();
    }
}