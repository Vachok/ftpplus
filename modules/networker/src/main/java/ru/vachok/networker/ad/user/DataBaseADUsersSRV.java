// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.ADAttributeNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;


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
    
    public List<ADUser> getAdUsers(File usersCsv) {
        fileParser(FileSystemWorker.readFileToQueue(usersCsv.toPath().toAbsolutePath().normalize()));
        return adUsers;
    }
    
    /**
     @see ru.vachok.networker.ad.user.DataBaseADUsersSRVTest#testFileParser()
     @param adUsersFileAsQueue файл-выгрузка из AD
     @return aduser parameters as map
     */
    public Map<String, String> fileParser(Queue<String> adUsersFileAsQueue) {
        Map<String, String> paramNameValue = new HashMap<>();
        StringBuilder stringBuilderSQL = new StringBuilder();
        String distinguishedName;
        String enabled;
        String givenName;
        String name;
        String objectClass;
        String objectGUID;
        String samAccountName;
        String SID;
        String surname;
        String userPrincipalName;
        while (adUsersFileAsQueue.iterator().hasNext()) {
            String parameterValueString = adUsersFileAsQueue.poll();
            if (parameterValueString.contains("IsSecurityPrincipal")) {
                adUsers.add(adUser);
                this.adUser = new ADUser();
            }
            try {
                if (parameterValueString.toLowerCase().contains(ADAttributeNames.DISTINGUISHED_NAME.toLowerCase())) {
                    distinguishedName = parameterValueString.split(" : ")[1];
                    paramNameValue.put(ADAttributeNames.DISTINGUISHED_NAME, distinguishedName);
                    adUser.setDistinguishedName(distinguishedName);
                }
                if (parameterValueString.toLowerCase().contains(ADAttributeNames.ENABLED.toLowerCase())) {
                    enabled = parameterValueString.split(" : ")[1];
                    paramNameValue.put(ADAttributeNames.ENABLED, enabled);
                    adUser.setEnabled(enabled);
                }
                if (parameterValueString.toLowerCase().contains(ADAttributeNames.GIVEN_NAME.toLowerCase())) {
                    givenName = parameterValueString.split(" : ")[1];
                    paramNameValue.put(ADAttributeNames.GIVEN_NAME, givenName);
                    adUser.setGivenName(givenName);
                }
                if (parameterValueString.toLowerCase().contains("name".toLowerCase())) {
                    name = parameterValueString.split(" : ")[1];
                    paramNameValue.put("name", name);
                    adUser.setName(name);
                }
                if (parameterValueString.toLowerCase().contains(ADAttributeNames.OBJECT_CLASS.toLowerCase())) {
                    objectClass = parameterValueString.split(" : ")[1];
                    paramNameValue.put(ADAttributeNames.OBJECT_CLASS, objectClass);
                    adUser.setObjectClass(objectClass);
                }
                if (parameterValueString.toLowerCase().contains(ADAttributeNames.OBJECT_GUID.toLowerCase())) {
                    objectGUID = parameterValueString.split(" : ")[1];
                    paramNameValue.put(ADAttributeNames.OBJECT_GUID, objectGUID);
                    adUser.setObjectGUID(objectGUID);
                }
                if (parameterValueString.toLowerCase().contains(ADAttributeNames.SAM_ACCOUNT_NAME.toLowerCase())) {
                    samAccountName = parameterValueString.split(" : ")[1];
                    paramNameValue.put("samAccountName", samAccountName);
                    adUser.setSamAccountName(samAccountName);
                }
                if (parameterValueString.toLowerCase().contains("sid".toLowerCase())) {
                    SID = parameterValueString.split(" : ")[1];
                    paramNameValue.put("sid", SID);
                    adUser.setSid(SID);
                }
                if (parameterValueString.toLowerCase().contains(ADAttributeNames.SURNAME.toLowerCase())) {
                    surname = parameterValueString.split(" : ")[1];
                    paramNameValue.put(ADAttributeNames.SURNAME, surname);
                    adUser.setSurname(surname);
                }
                if (parameterValueString.toLowerCase().contains(ADAttributeNames.USER_PRINCIPAL_NAME.toLowerCase())) {
                    userPrincipalName = parameterValueString.split(" : ")[1];
                    paramNameValue.put(ADAttributeNames.USER_PRINCIPAL_NAME, userPrincipalName);
                }
            }
            catch (ArrayIndexOutOfBoundsException | NullPointerException ignore) {
                //
            }
        }
        System.out.println("adUsers.size() = " + adUsers.size());
        return paramNameValue;
    }

    private boolean dbUploader() {
    
        try (Connection defaultConnection = new AppComponents().connection(ConstantsFor.DBPREFIX + ConstantsFor.STR_VELKOM)) {
            Map<String, String> paramNameValueMap = fileParser(FileSystemWorker.readFileToQueue(Paths.get("users.csv").toAbsolutePath().normalize()));
            try (PreparedStatement p = defaultConnection.prepareStatement("")) {
                p.executeUpdate();
            }
        }
        catch (SQLException e) {
            return false;
        }
        return true;
    }

}
