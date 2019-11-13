package ru.vachok.networker.ad.user;


import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.UsefulUtilities;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;


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
        try {
            return Collections.singletonList(UserInfo.resolvePCUserOverDB(PCInfo.checkValidNameWithoutEatmeat(credentials)));
        }
        catch (RuntimeException e) {
            return Collections.singletonList(MessageFormat.format(USER_UNKNOWN, credentials, fromClass, LocalDateTime.now().toString()));
        }
    }
    
    @Override
    public String getInfo() {
        return MessageFormat.format(USER_UNKNOWN, credentials, fromClass, AbstractForms.networkerTrace(Thread.currentThread().getStackTrace()));
    }
    
    @Override
    public void setClassOption(Object option) {
        this.credentials = (String) option;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.credentials = aboutWhat;
        return MessageFormat.format(USER_UNKNOWN, credentials, fromClass, AbstractForms.networkerTrace(Thread.currentThread().getStackTrace()));
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", UnknownUser.class.getSimpleName() + "[\n", "\n]")
            .add("credentials = '" + credentials + "'")
            .add("fromClass = '" + fromClass + "'")
            .toString();
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
