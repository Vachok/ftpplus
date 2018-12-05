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
            .append("<a href=\"/pflists\">Списки PF</a><br>\n")
            .append("<a href=\"/netscan\">Скан локальных ПК</a><br>\n")
            .append("<a href=\"/odinass\">Сформировать лист команд PoShell для сверки должностей</a><br>\n")
            .append("<a href=\"/exchange\"><strike>Парсинг правил MS Exchange</a><br></strike>\n")
            .append("<a href=\"/adphoto\">Добавить фотографии в Outlook</a><br>\n")
            .append("<a href=\"/common\">Восстановить из архива</a><br>\n")
            .append("<a href=\"/cleaner\"><strike>Ищейка файлов</strike></a><br>\n")
            .append("<a href=\"/sshacts\"><strike>SSH worker</strike></a><br>\n")
            .append("<a href=\"/serviceinfo\"><font color=\"#999eff\">SERVICEINFO</font></a>")
            .toString();
    }
}
