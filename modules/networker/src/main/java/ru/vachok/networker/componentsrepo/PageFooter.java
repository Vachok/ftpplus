package ru.vachok.networker.componentsrepo;


import org.springframework.stereotype.Component;

/**
 @since 02.10.2018 (16:28) */
@Component
public class PageFooter {

    private String footerUtext;

    public PageFooter() {
        setFooterUtext();
    }

    public String getHeaderUtext() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append("<a href=\"/\">Главная</a>");
        return stringBuilder.toString();
    }

    public String getFooterUtext() {
        return footerUtext;
    }

    private void setFooterUtext() {
        this.footerUtext = new StringBuilder()
            .append("<a href=\"/\"><img align=\"right\" src=\"/images/icons8-плохие-поросята-100g.png\" alt=\"_\"/></a>\n")
            .append("<a href=\"/pflists\"><font color=\"#00cc66\">Списки PF</font></a><br>\n")
            .append("<a href=\"/netscan\"><font color=\"#00cc66\">Скан локальных ПК</font></a><br>\n")
            .append("<a href=\"/odinass\">Сформировать лист команд PoShell для сверки должностей</a><br>\n")
            .append("<a href=\"/exchange\"><strike>Парсинг правил MS Exchange</a><br></strike>\n")
            .append("<a href=\"/adphoto\">Добавить фотографии в Outlook</a><br>\n")
            .append("<a href=\"/common\"><font color=\"#00cc66\">Восстановить из архива</font></a><br>\n")
            .append("<a href=\"/sshacts\">SSH worker (Only Allow Domains)</a><br>\n")
            .append("<p>")
            .append("<a href=\"/serviceinfo\"><font color=\"#999eff\">SERVICEINFO</font></a><br>\n")
            .append("<font size=\"1\"><p align=\"right\">By Vachok. (c) 2019</font></p>")
            .toString();
    }
}
