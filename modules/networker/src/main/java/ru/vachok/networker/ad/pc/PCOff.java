// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


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
            dbPCInfo.setClassOption(pcName);
        }
        String htmlStr = dbPCInfo.fillWebModel();
        addToMap(pcName, new NameOrIPChecker(pcName).resolveInetAddress().getHostAddress());
        NetKeeper.getUsersScanWebModelMapWithHTMLLinks().put(htmlStr + "<br>", false);
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
}
