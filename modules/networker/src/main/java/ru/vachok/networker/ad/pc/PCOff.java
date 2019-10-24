// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.text.MessageFormat;
import java.util.UnknownFormatConversionException;


/**
 @see ru.vachok.networker.ad.pc.PCOffTest
 @since 08.08.2019 (13:20) */
class PCOff extends PCInfo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PCOff.class.getSimpleName());
    
    private String pcName;
    
    private HTMLInfo dbPCInfo;
    
    public PCOff(String aboutWhat) {
        this.pcName = aboutWhat;
        this.dbPCInfo = new DBPCHTMLInfo(pcName);
    }
    
    PCOff() {
        this.dbPCInfo = new DBPCHTMLInfo(ConstantsFor.DBFIELD_PCNAME);
    }
    
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.pcName = aboutWhat;
        dbPCInfo.setClassOption(pcName);
        String counterOnOff = dbPCInfo.fillAttribute(pcName);
        String unrStr = addToMap(counterOnOff);
        messageToUser.info(this.getClass().getSimpleName(), "unrStr = ", unrStr);
        return MessageFormat.format("{0}", counterOnOff);
    }
    
    @Override
    public String getInfo() {
        if (pcName == null) {
            return "Please - set the pcName!\n" + this.toString();
        }
        String checkPCName = PCInfo.checkValidNameWithoutEatmeat(pcName);
        if (checkPCName.contains("Unknown PC:")) {
            throw new UnknownFormatConversionException(pcName);
        }
        else {
            this.pcName = checkPCName;
        }
        dbPCInfo.setClassOption(pcName);
        String htmlStr = MessageFormat.format("{0} {1}", addToMap(" "), dbPCInfo.fillWebModel());
        NetKeeper.getUsersScanWebModelMapWithHTMLLinks().put(htmlStr, false);
        return htmlStr;
    }
    
    @Override
    public void setClassOption(Object option) {
        this.pcName = (String) option;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PCOff{");
        sb.append("pcName='").append(pcName).append('\'');
        sb.append(", dbPCInfo=").append(dbPCInfo.toString());
        sb.append('}');
        return sb.toString();
    }
    
    private @NotNull String addToMap(String onOffCounter) {
        String toSet;
        try {
            toSet = checkValidNameWithoutEatmeat(pcName) + ":" + new NameOrIPChecker(pcName).resolveInetAddress().getHostAddress() + " online false<br>";
            NetKeeper.getPcNamesForSendToDatabase().add(toSet);
        }
        catch (UnknownFormatConversionException e) {
            toSet = MessageFormat.format("PCOff.addToMap({0}): {1} see line: 74", pcName, e.getMessage());
        }
        return MessageFormat.format("{0} {1}", toSet, onOffCounter);
    }
}
