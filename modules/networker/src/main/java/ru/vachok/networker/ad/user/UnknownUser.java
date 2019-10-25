package ru.vachok.networker.ad.user;


import ru.vachok.networker.componentsrepo.UsefulUtilities;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;


/**
 @since 27.08.2019 (10:08) */
class UnknownUser extends UserInfo {
    
    
    private static final String USER_UNKNOWN = "Unknown user {0} : {1} : {2}";
    
    private String credentials;
    
    private String fromClass;
    
    UnknownUser(String fromClass) {
        this.credentials = UsefulUtilities.thisPC();
        this.fromClass = fromClass;
    }
    
    @Override
    public List<String> getLogins(String pcName, int resultsLimit) {
        this.credentials = pcName;
        return Collections.singletonList(MessageFormat.format(USER_UNKNOWN, credentials, fromClass, LocalDateTime.now().toString()));
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.credentials = aboutWhat;
        return MessageFormat.format(USER_UNKNOWN, credentials, fromClass);
    }
    
    @Override
    public void setClassOption(Object option) {
        this.credentials = (String) option;
    }
    
    @Override
    public String getInfo() {
        return MessageFormat.format(USER_UNKNOWN, credentials, fromClass);
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
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", UnknownUser.class.getSimpleName() + "[\n", "\n]")
            .add("credentials = '" + credentials + "'")
            .add("fromClass = '" + fromClass + "'")
            .toString();
    }
}
