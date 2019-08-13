package ru.vachok.networker.info;


/**
 @since 13.08.2019 (17:15) */
public interface DatabaseInfo extends InformationFactory {
    
    
    String getUserPCFromDB(String userName);
    
    String getPCUsersFromDB(String pcName);
}
