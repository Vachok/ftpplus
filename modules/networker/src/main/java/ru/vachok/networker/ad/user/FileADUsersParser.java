// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.PCUserResolver;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.enums.ADAttributeNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.UserInformation;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;


/**
 @see ru.vachok.networker.ad.user.FileADUsersParserTest
 @since 09.10.2018 (10:35)
 */
@Service
public class FileADUsersParser implements UserInformation {
    
    
    private static MessageToUser messageToUser = new MessageLocal(FileADUsersParser.class.getSimpleName());
    
    private List<ADUser> adUsers = new ArrayList<>();
    
    public FileADUsersParser() {
        this.adUser = new ADUser();
    }
    
    private ADUser adUser;

    @Autowired
    public FileADUsersParser(ADUser adUser) {
        this.adUser = adUser;
    }
    
    @Override
    public List<ADUser> getADUsers() {
        if (adUsers.size() > 0) {
            return adUsers;
        }
        else {
            throw new InvokeIllegalException("Please set csv file via setInfo()");
        }
    }
    
    @Override
    public String getCurrentUserName(String pcName) {
        return new PCUserResolver().getCurrentUserName(pcName);
    }
    
    @Override
    public String getInfoAbout(String samAccountName) {
        if (adUsers.size() <= 0) {
            throw new InvokeIllegalException("Please, set the CSV-file via setInfo()");
        }
        else {
            List<String> adUserSAM = new ArrayList<>(1);
            adUsers.forEach(user->{
                if (user.getSamAccountName().contains(samAccountName)) {
                    adUserSAM.add(user.toString());
                }
            });
            return adUserSAM.get(0);
        }
    }
    
    @Override
    public void setClassOption(Object classOption) {
        File usersCsv = (File) classOption;
        String fileNameAsCharset = usersCsv.getName();
        Map<String, String> parameterValue = fileParser(FileSystemWorker.readFileEncodedToQueue(usersCsv.toPath().toAbsolutePath().normalize(), fileNameAsCharset));
    }
    
    private List<ADUser> getADUsers(Queue<String> csvAsStrings) {
        fileParser(csvAsStrings);
        return adUsers;
    }
    
    /**
     @param adUsersFileAsQueue файл-выгрузка из AD
     */
    private @NotNull Map<String, String> fileParser(@NotNull Queue<String> adUsersFileAsQueue) {
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
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataBaseADUsersSRV{");
        sb.append("adUsers=").append(adUsers.size());
        sb.append(", adUser=").append(adUser.toString());
        sb.append('}');
        return sb.toString();
    }
}
