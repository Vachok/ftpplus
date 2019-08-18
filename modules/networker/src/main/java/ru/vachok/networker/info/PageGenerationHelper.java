// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.util.StringJoiner;


/**
 @see ru.vachok.networker.info.PageGenerationHelperTest
 @since 02.10.2018 (16:28) */
@Component
public class PageGenerationHelper implements HTMLGeneration {
    
    
    private String footerUtext;
    
    public PageGenerationHelper() {
        setClassOption(getFooter());
    }
    
    @Override
    public String getAsLink(@NotNull String linkTo, String text) {
        StringBuilder htmlStringBuilder = new StringBuilder();
        htmlStringBuilder.append("<a href=\"").append(linkTo).append("\">").append(text).append("</a>\n");
        return htmlStringBuilder.toString();
    }
    
    @Override
    public PageGenerationHelper getInst() {
        return this;
    }
    
    @Override
    public String setColor(String color, String text) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<font color=\"").append(color).append("\">").append(text).append("</font>");
        return stringBuilder.toString();
    }
    
    @Override
    public String getFooter(@NotNull String aboutWhat) {
        String retStr = toString();
        if (aboutWhat.equalsIgnoreCase(ModelAttributeNames.ATT_HEAD)) {
            retStr = getHeaderUtext();
        }
        if (aboutWhat.equalsIgnoreCase(ModelAttributeNames.ATT_FOOTER)) {
            retStr = getFooterUtext();
        }
        return retStr;
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.footerUtext = (String) classOption;
        FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".log", footerUtext);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PageGenerationHelper.class.getSimpleName() + "[\n", "\n]")
            .add("footerUtext = '" + footerUtext + "'")
            .toString();
    }
    
    private @NotNull Object getFooter() {
        StringBuilder builder = new StringBuilder()
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
            .append("<font size=\"1\"><p align=\"right\">By Vachok. (c) 2019</font></p>");
        return builder.toString();
    }
    
    private @NotNull String getHeaderUtext() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getHTMLCenterColor("", getAsLink("/", "Главная")));
        return stringBuilder.toString();
    }
    
    @Contract(pure = true)
    private String getFooterUtext() {
        return footerUtext;
    }
}
