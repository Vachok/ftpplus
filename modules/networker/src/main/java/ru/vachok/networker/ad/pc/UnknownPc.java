package ru.vachok.networker.ad.pc;


import java.text.MessageFormat;


/**
 @since 27.08.2019 (11:04) */
class UnknownPc extends PCInfo {
    
    
    private static final String PC_UNKNOWN = "Unknown PC: {0}\n {1}";
    
    private String credentials = "";
    
    private String fromClass;
    
    UnknownPc(String fromClass) {
        this.fromClass = fromClass;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.credentials = aboutWhat;
        return MessageFormat.format(PC_UNKNOWN, credentials, fromClass);
    }
    
    @Override
    public String getInfo() {
        return MessageFormat.format(PC_UNKNOWN, credentials, fromClass);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UnknownUser{");
        sb.append("credentials='").append(credentials).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public void setOption(Object option) {
        this.credentials = (String) option;
    }
}
