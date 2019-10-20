// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.text.MessageFormat;
import java.util.StringJoiner;
import java.util.UnknownFormatConversionException;


/**
 @see ru.vachok.networker.ad.pc.PCOffTest
 @since 08.08.2019 (13:20) */
class PCOff extends PCInfo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PCOff.class.getSimpleName());
    
    private String pcName;
    
    private ru.vachok.mysqlandprops.DataConnectTo dataConnectTo = DataConnectTo.getRemoteReg();
    
    public PCOff(String aboutWhat) {
        this.pcName = aboutWhat;
    }
    
    PCOff() {
        this.pcName = "";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        PCOff off = (PCOff) o;
        
        if (pcName != null ? !pcName.equals(off.pcName) : off.pcName != null) {
            return false;
        }
        return dataConnectTo != null ? dataConnectTo.equals(off.dataConnectTo) : off.dataConnectTo == null;
    }
    
    @Override
    public int hashCode() {
        return dataConnectTo != null ? dataConnectTo.hashCode() : 0;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.pcName = aboutWhat;
        HTMLInfo dbPCInfo = new DBPCHTMLInfo(pcName);
        dbPCInfo.setClassOption(pcName);
        String unrStr = pcNameUnreachable(dbPCInfo.fillAttribute(pcName)); //fixme 20.10.2019 (14:44)
        return MessageFormat.format("{0}", dbPCInfo.fillAttribute(aboutWhat));
    }
    
    @Override
    public String getInfo() {
        if (pcName == null) {
            return "Please - set the pcName!\n" + this.toString();
        }
        return PCInfo.defaultInformation(pcName, false);
    }
    
    @Override
    public void setClassOption(Object option) {
        this.pcName = (String) option;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PCOff.class.getSimpleName() + "[\n", "\n]")
            .add("pcName = '" + pcName + "'")
            .add("dataConnectTo = " + dataConnectTo)
            .toString();
    }
    
    String pcNameUnreachable(String onOffCounter) {
        HTMLInfo dbPCInfo = new DBPCHTMLInfo(pcName);
        String onLines = new StringBuilder()
            .append("online ")
            .append(NetScanService.isReach(pcName)).toString();
        try {
            NetKeeper.getPcNamesForSendToDatabase()
                .add(checkValidNameWithoutEatmeat(pcName) + ":" + new NameOrIPChecker(pcName).resolveInetAddress().getHostAddress() + " " + onLines + "<br>");
        }
        catch (UnknownFormatConversionException e) {
            messageToUser.error(e.getMessage() + " see line: 213 ***");
        }
        String retStr = onLines + " " + onOffCounter;
        return retStr;
    }
}
