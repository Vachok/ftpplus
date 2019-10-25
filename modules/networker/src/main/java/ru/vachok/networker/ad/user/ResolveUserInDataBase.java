// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UnknownFormatConversionException;


/**
 @see ResolveUserInDataBaseTest
 @since 02.04.2019 (10:25) */
class ResolveUserInDataBase extends UserInfo {
    
    
    private static final String SQL_GETLOGINS = "SELECT * FROM velkom.pcuserauto WHERE userName LIKE ? ORDER BY idRec DESC LIMIT ?";
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ResolveUserInDataBase.class.getSimpleName());
    
    private Object aboutWhat;
    
    private String userName;
    
    private DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
    
    ResolveUserInDataBase() {
        this.aboutWhat = "No pc name set";
    }
    
    ResolveUserInDataBase(String type) {
        this.aboutWhat = type;
    }
    
    @Override
    public int hashCode() {
        int result = aboutWhat != null ? aboutWhat.hashCode() : 0;
        result = 31 * result + dataConnectTo.hashCode();
        return result;
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
    
    private String tryPcName() {
        try {
            return getLogins((String) aboutWhat, 1).get(0).split(" ")[0];
        }
        catch (IndexOutOfBoundsException e) {
            return e.getMessage();
        }
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        String res;
        this.aboutWhat = aboutWhat;
        List<String> foundedUserPC = searchDatabase(1, SQL_GETLOGINS);
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
        List<String> results = searchDatabase(resultsLimit, SQL_GETLOGINS);
        if (results.size() > 0) {
            return results;
        }
        else {
            this.aboutWhat = PCInfo.checkValidNameWithoutEatmeat(aboutWhat);
            return searchDatabase(resultsLimit, SQL_GETLOGINS.replace(ConstantsFor.DBFIELD_USERNAME, ConstantsFor.DBFIELD_PCNAME));
        }
    }
    
    private @NotNull List<String> searchDatabase(int linesLimit, String sql) {
        List<String> retList = new ArrayList<>();
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_PCUSERAUTO_FULL)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, String.format("%%%s%%", aboutWhat));
                preparedStatement.setInt(2, linesLimit);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String addStr = MessageFormat
                            .format("{0} : {1} : {2}", resultSet.getString(ConstantsFor.DBFIELD_PCNAME), resultSet.getString(ConstantsFor.DBFIELD_USERNAME), resultSet
                                .getTimestamp(ConstantsNet.DB_FIELD_WHENQUERIED));
                        retList.add(addStr);
                    }
                }
            }
            catch (RuntimeException e) {
                messageToUser.error(MessageFormat.format("ResolveUserInDataBase.searchDatabase", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
            }
        }
        catch (SQLException e) {
            retList.add(e.getMessage());
            retList.add(AbstractForms.fromArray(e));
        }
        return retList;
    }
    
    private @NotNull String getPCLogins(String pcName, int resultsLimit) {
        pcName = PCInfo.checkValidNameWithoutEatmeat(pcName);
        this.aboutWhat = pcName;
        List<String> velkomPCUser = searchDatabase(resultsLimit, "SELECT * FROM velkom.pcuserauto WHERE pcName LIKE ? ORDER BY idRec DESC LIMIT ?");
        return HTMLGeneration.getInstance("").getHTMLCenterColor(ConstantsFor.YELLOW, AbstractForms.fromArray(velkomPCUser));
    }
    
    @Override
    public void setClassOption(Object option) {
        this.aboutWhat = option;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ResolveUserInDataBase.class.getSimpleName() + "[\n", "\n]")
                .add("aboutWhat = " + aboutWhat)
                .add("dataConnectTo = " + dataConnectTo.toString())
                .toString();
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
    
    
}
