package ru.vachok.networker.ad.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.services.MessageLocal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;


/**
 @since 09.10.2018 (10:35)
 @see ru.vachok.networker.ad.user.DataBaseADUsersSRVTest
 */
@Service
public class DataBaseADUsersSRV {

    private static MessageToUser messageToUser = new MessageLocal(DataBaseADUsersSRV.class.getSimpleName());
    
    private List<ADUser> adUsers = new ArrayList<>();
    
    public DataBaseADUsersSRV() {
        this.adUser = new ADUser();
    }
    
    private ADUser adUser;

    @Autowired
    public DataBaseADUsersSRV(ADUser adUser) {
        this.adUser = adUser;
    }
    
    public List<ADUser> getAdUsers() {
        return adUsers;
    }
    
    /**
     @param adUsersFileAsQueue файл-выгрузка из AD
     @return {@link Map} имя параметра - значение.
     
     @see ru.vachok.networker.ad.user.DataBaseADUsersSRVTest#testFileParser()
     */
    public Map<String, String> fileParser(Queue<String> adUsersFileAsQueue) {
        Map<String, String> paramNameValue = new HashMap<>();
        StringBuilder stringBuilderSQL = new StringBuilder();
        String distinguishedName = "";
        String enabled = "";
        String givenName;
        String name = "";
        String objectClass = "";
        String objectGUID = "";
        String samAccountName = "";
        String SID = "";
        String surname = "";
        String userPrincipalName = "";
        
        while (adUsersFileAsQueue.iterator().hasNext()) {
            String parameterValueString = adUsersFileAsQueue.poll();
            if (parameterValueString.contains("IsSecurityPrincipal")) {
                adUsers.add(adUser);
                this.adUser = new ADUser();
            }
            try {
                if (parameterValueString.toLowerCase().contains("distinguishedName".toLowerCase())) {
                    distinguishedName = parameterValueString.split(" : ")[1];
                    paramNameValue.put("distinguishedName", distinguishedName);
                    adUser.setDistinguishedName(distinguishedName);
                }
                if (parameterValueString.toLowerCase().contains("enabled".toLowerCase())) {
                    enabled = parameterValueString.split(" : ")[1];
                    paramNameValue.put("enabled", enabled);
                    adUser.setEnabled(enabled);
                }
                if (parameterValueString.toLowerCase().contains("givenName".toLowerCase())) {
                    givenName = parameterValueString.split(" : ")[1];
                    paramNameValue.put("givenName", givenName);
                    adUser.setGivenName(givenName);
                }
                if (parameterValueString.toLowerCase().contains("name".toLowerCase())) {
                    name = parameterValueString.split(" : ")[1];
                    paramNameValue.put("name", name);
                    adUser.setName(name);
                }
                if (parameterValueString.toLowerCase().contains("objectClass".toLowerCase())) {
                    objectClass = parameterValueString.split(" : ")[1];
                    paramNameValue.put("objectClass", objectClass);
                    adUser.setObjectClass(objectClass);
                }
                if (parameterValueString.toLowerCase().contains("objectGUID".toLowerCase())) {
                    objectGUID = parameterValueString.split(" : ")[1];
                    paramNameValue.put("objectGUID", objectGUID);
                    adUser.setObjectGUID(objectGUID);
                }
                if (parameterValueString.toLowerCase().contains("SamAccountName".toLowerCase())) {
                    samAccountName = parameterValueString.split(" : ")[1];
                    paramNameValue.put("samAccountName", samAccountName);
                    adUser.setSamAccountName(samAccountName);
                }
                if (parameterValueString.toLowerCase().contains("sid".toLowerCase())) {
                    SID = parameterValueString.split(" : ")[1];
                    paramNameValue.put("sid", SID);
                    adUser.setSid(SID);
                }
                if (parameterValueString.toLowerCase().contains("surname".toLowerCase())) {
                    surname = parameterValueString.split(" : ")[1];
                    paramNameValue.put("surname", surname);
                    adUser.setSurname(surname);
                }
                if (parameterValueString.toLowerCase().contains("userPrincipalName".toLowerCase())) {
                    userPrincipalName = parameterValueString.split(" : ")[1];
                    paramNameValue.put("userPrincipalName", userPrincipalName);
                }
            }
            catch (ArrayIndexOutOfBoundsException | NullPointerException ignore) {
                //
            }
        }
        System.out.println("adUsers.size() = " + adUsers.size());
        return paramNameValue;
    }

    ConcurrentMap<String, String> fileRead(BufferedReader bufferedReader) {
        throw new UnsupportedOperationException();
    }

    private boolean dbUploader() {

        try (Connection defaultConnection = new AppComponents().connection(ConstantsFor.DBPREFIX + ConstantsFor.STR_VELKOM);
             InputStream resourceAsStream = getClass().getResourceAsStream(ConstantsFor.FILEPATHSTR_USERSTXT)) {
            InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            ConcurrentMap<String, String> paramNameValueMap = fileRead(bufferedReader);
            try (PreparedStatement p = defaultConnection.prepareStatement("")) {
                p.executeUpdate();
            }
        } catch (SQLException | IOException e) {
            return false;
        }
        return true;
    }

}
