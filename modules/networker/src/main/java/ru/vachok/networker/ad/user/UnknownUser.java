package ru.vachok.networker.ad.user;


import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;


/**
 @since 27.08.2019 (10:08) */
class UnknownUser extends UserInfo {
    
    
    private static final String USER_UNKNOWN = "Unknown user: {0}. {1}";
    
    private String credentials = this.getClass().getTypeName();
    
    private String fromClass;
    
    UnknownUser(String fromClass) {
        this.fromClass = fromClass;
    }
    
    @Override
    public List<String> getPCLogins(String pcName, int resultsLimit) {
        return Collections.singletonList(MessageFormat.format(USER_UNKNOWN, credentials, this.getClass().getTypeName()));
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.credentials = aboutWhat;
        return MessageFormat.format(USER_UNKNOWN, credentials, fromClass);
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.credentials = (String) classOption;
    }
    
    @Override
    public String getInfo() {
        return MessageFormat.format(USER_UNKNOWN, credentials, this.getClass().getTypeName());
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UnknownUser{");
        sb.append("credentials='").append(credentials).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public int hashCode() {
        return credentials.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        UnknownUser user = (UnknownUser) o;
        
        return credentials.equals(user.credentials);
    }
}
