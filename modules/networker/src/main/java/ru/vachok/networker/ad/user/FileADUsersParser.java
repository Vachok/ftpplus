// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;


/**
 @see ru.vachok.networker.ad.user.FileADUsersParserTest
 @since 09.10.2018 (10:35)
 */
public class FileADUsersParser {
    
    private List<ADUser> adUsers = new ArrayList<>();
    
    private Map<String, String> paramNameValue = new HashMap<>();
    
    public FileADUsersParser() {
        this.adUser = new ADUser();
    }
    
    private ADUser adUser;
    
    public FileADUsersParser(ADUser adUser) {
        this.adUser = adUser;
    }
    
    public List<ADUser> getADUsers(Queue<String> csvAsStrings) {
        fileParser(csvAsStrings);
        return adUsers;
    }
    
    /**
     @param adUsersFileAsQueue файл-выгрузка из AD
     */
    private @NotNull Map<String, String> fileParser(@NotNull Queue<String> adUsersFileAsQueue) {
    
        while (adUsersFileAsQueue.iterator().hasNext()) {
            String parameterValueString = adUsersFileAsQueue.poll();
            if (parameterValueString != null && parameterValueString.contains("IsSecurityPrincipal")) {
                adUsers.add(adUser);
                this.adUser = new ADUser();
            }
            try {
                parseUser(parameterValueString);
            }
            catch (ArrayIndexOutOfBoundsException | NullPointerException ignore) {
                //
            }
        }
        System.out.println("adUsers.size() = " + adUsers.size());
        return paramNameValue;
    }
    
    private void parseUser(String parameterValueString) {
        for (ADUserParamNames name : ADUserParamNames.values()) {
            if (parameterValueString.toLowerCase().contains(name.toString())) {
                paramNameValue.put(name.toString(), parameterValueString.split(" : ")[1]);
                adUser.setDistinguishedName(name.toString());
            }
        }
    }
    
    private boolean dbUploader() {
        try (Connection defaultConnection = new AppComponents().connection(ConstantsFor.DBPREFIX + ConstantsFor.STR_VELKOM)) {
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
