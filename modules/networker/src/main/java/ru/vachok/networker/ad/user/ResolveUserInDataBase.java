// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.DataConnectTo;

import java.net.InetAddress;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;


/**
 @see ResolveUserInDataBaseTest
 @since 02.04.2019 (10:25) */
class ResolveUserInDataBase extends UserInfo {
    
    
    private Object aboutWhat;
    
    private String userName;
    
    private DataConnectTo dataConnectTo = DataConnectTo.getDefaultI();
    
    public ResolveUserInDataBase() {
        this.userName = "No username set";
        this.aboutWhat = "No pc name set";
    }
    
    ResolveUserInDataBase(String type) {
        this.aboutWhat = type;
    }
    
    private String invalidUserBuild() {
        if (new NameOrIPChecker((String) aboutWhat).resolveInetAddress().equals(InetAddress.getLoopbackAddress())) {
            return new UnknownUser(this.toString()).getInfoAbout((String) aboutWhat);
        }
        else {
            return new NameOrIPChecker((String) aboutWhat).resolveInetAddress().getHostAddress();
        }
    }
    
    @Override
    public List<String> getPCLogins(String pcName, int resultsLimit) {
        this.aboutWhat = pcName;
        return searchDatabase(resultsLimit, "SELECT * FROM `pcuserauto` WHERE `pcName` LIKE ? ORDER BY `pcuserauto`.`whenQueried` DESC LIMIT ?");
    }
    
    @NotNull List<String> getUserLogins(String userName, int resultsLimit) {
        this.userName = userName;
        this.aboutWhat = userName;
        return searchDatabase(resultsLimit, "SELECT * FROM `pcuserauto` WHERE `userName` LIKE ? ORDER BY `pcuserauto`.`whenQueried` DESC LIMIT ?");
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        String res;
        this.aboutWhat = aboutWhat;
        List<String> foundedUserPC = searchDatabase(1, "SELECT * FROM `pcuserauto` WHERE `userName` LIKE ? ORDER BY `pcuserauto`.`whenQueried` DESC LIMIT ?");
        if (foundedUserPC.size() > 0) {
            res = new NameOrIPChecker(foundedUserPC.get(0)).resolveInetAddress().getHostAddress();
        }
        else {
            foundedUserPC = getPCLogins(aboutWhat, 1);
            try {
                res = foundedUserPC.get(0);
            }
            catch (IndexOutOfBoundsException e) {
                res = new UnknownUser(this.toString()).getInfoAbout(aboutWhat);
            }
        }
        return res;
    }
    
    @Override
    public void setClassOption(Object option) {
        this.aboutWhat = option;
    }
    
    @Override
    public String getInfo() {
        String retString;
        try {
            retString = getUserLogins((String) aboutWhat, 1).get(0);
            retString = retString.split(" ")[0];
            return new NameOrIPChecker(retString).resolveInetAddress().getHostAddress();
        }
        catch (IndexOutOfBoundsException | UnknownFormatConversionException e) {
            return tryPcName();
        }
    }
    
    private String tryPcName() {
        try {
            return getPCLogins((String) aboutWhat, 1).get(0).split(" ")[0];
        }
        catch (IndexOutOfBoundsException e) {
            return e.getMessage();
        }
    }
    
    private @NotNull List<String> searchDatabase(int linesLimit, String sql) {
        MysqlDataSource mysqlDataSource = dataConnectTo.getDataSource();
        List<String> retList = new ArrayList<>();
        try (Connection connection = mysqlDataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, String.format("%%%s%%", aboutWhat));
                preparedStatement.setInt(2, linesLimit);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        retList.add(MessageFormat.format("{0} : {1}", resultSet.getString(ConstantsFor.DBFIELD_PCNAME), resultSet.getString(ConstantsFor.DB_FIELD_USER)));
                    }
                }
            }
        }
        catch (SQLException e) {
            retList.add(e.getMessage());
            retList.add(new TForms().fromArray(e, false));
        }
        return retList;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ResolveUserInDataBase.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
    
    
}
