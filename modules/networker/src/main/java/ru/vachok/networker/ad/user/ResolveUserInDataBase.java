// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;


/**
 @see ResolveUserInDataBaseTest
 @since 02.04.2019 (10:25) */
class ResolveUserInDataBase extends UserInfo {
    
    
    ResolveUserInDataBase() {
        this.aboutWhat = "No pc name set";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        ResolveUserInDataBase that = (ResolveUserInDataBase) o;
        
        if (aboutWhat != null ? !aboutWhat.equals(that.aboutWhat) : that.aboutWhat != null) {
            return false;
        }
        return dataConnectTo.equals(that.dataConnectTo);
    }
    
    private Object aboutWhat;
    
    private DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LIB_REGRU);
    
    @Override
    public int hashCode() {
        int result = aboutWhat != null ? aboutWhat.hashCode() : 0;
        result = 31 * result + dataConnectTo.hashCode();
        return result;
    }
    
    ResolveUserInDataBase(String type) {
        this.aboutWhat = type;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        String res;
        this.aboutWhat = aboutWhat;
        List<String> foundedUserPC = searchDatabase(1, "SELECT * FROM `pcuserauto` WHERE `userName` LIKE ? ORDER BY `pcuserauto`.`whenQueried` DESC LIMIT ?");
        if (foundedUserPC.size() > 0) {
            res = foundedUserPC.get(0);
        }
        else {
            foundedUserPC = getLogins(aboutWhat, 1);
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
    public List<String> getLogins(String aboutWhat, int resultsLimit) {
        this.aboutWhat = aboutWhat;
        List<String> results = searchDatabase(resultsLimit, "SELECT * FROM `pcuserauto` WHERE `userName` LIKE ? ORDER BY `pcuserauto`.`whenQueried` DESC LIMIT ?");
        if (results.size() > 0) {
            return results;
        }
        else {
            return getPCLogins(aboutWhat, resultsLimit);
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ResolveUserInDataBase.class.getSimpleName() + "[\n", "\n]")
            .add("aboutWhat = " + aboutWhat)
            .add("dataConnectTo = " + dataConnectTo.toString())
            .toString();
    }
    
    @Override
    public void setClassOption(Object option) {
        this.aboutWhat = option;
    }
    
    private @NotNull List<String> getPCLogins(String pcName, int resultsLimit) {
        pcName = PCInfo.checkValidNameWithoutEatmeat(pcName);
        this.aboutWhat = pcName;
        return searchDatabase(resultsLimit, "SELECT * FROM `pcuserauto` WHERE `pcName` LIKE ? ORDER BY `pcuserauto`.`whenQueried` DESC LIMIT ?");
    }
    
    @Override
    public String getInfo() {
        String retString;
        try {
            retString = getLogins((String) aboutWhat, 1).get(0);
            retString = retString.split(" ")[0];
            return new NameOrIPChecker(retString).resolveInetAddress().getHostAddress();
        }
        catch (IndexOutOfBoundsException | UnknownFormatConversionException e) {
            return tryPcName();
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
                        retList.add(MessageFormat
                            .format("{0} : {1}", resultSet.getString(ConstantsFor.DBFIELD_PCNAME), resultSet.getString(ConstantsFor.DBFIELD_USERNAME)));
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
    
    private String tryPcName() {
        try {
            return getLogins((String) aboutWhat, 1).get(0).split(" ")[0];
        }
        catch (IndexOutOfBoundsException e) {
            return e.getMessage();
        }
    }
}
