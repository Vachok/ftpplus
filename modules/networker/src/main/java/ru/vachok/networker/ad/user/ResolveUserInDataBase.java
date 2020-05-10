// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;


/**
 @see ResolveUserInDataBaseTest
 @since 02.04.2019 (10:25) */
class ResolveUserInDataBase extends UserInfo {


    private static final String SQL_GETLOGINS = "SELECT * FROM velkom.pcuserauto WHERE userName LIKE ? ORDER BY idRec DESC LIMIT ?";

    private static final String RESULTS = "Results: ";

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ResolveUserInDataBase.class.getSimpleName());

    private Object aboutWhat;

    private final DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);

    @Override
    public String getInfo() {
        String result;
        try {
            result = getLogins((String) aboutWhat, 1).get(0);
            result = result.split(" ")[0];
            String resolvedAddress = new NameOrIPChecker(result).resolveInetAddress().getHostAddress();
            if (resolvedAddress.matches(String.valueOf(ConstantsFor.PATTERN_IP)) & !resolvedAddress.equals("127.0.0.1")) {
                result = resolvedAddress;
            }
            else {
                result = tryPcName();
            }
        }
        catch (IndexOutOfBoundsException | UnknownFormatConversionException e) {
            result = MessageFormat.format("ResolveUserInDataBase.getInfo {0}\n{1}", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        return result;
    }

    @Override
    public void setClassOption(Object option) {
        this.aboutWhat = option;
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
        res = res + " Last online: " + getLastOnlineTime();
        return res;
    }

    @NotNull
    private List<String> searchDatabase(int linesLimit, String sql) {
        List<String> retList = new ArrayList<>();
        if (!ConstantsFor.argNORUNExist()) {
            try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_PCUSERAUTO_FULL)) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, String.format("%%%s%%", aboutWhat));
                    preparedStatement.setInt(2, linesLimit);
                    preparedStatement.setQueryTimeout(18);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            Timestamp timestamp = resultSet.getTimestamp(ConstantsFor.DB_FIELD_WHENQUERIED);
                            String addStr = MessageFormat.format("{0} : {1} : {2}", resultSet.getString(ConstantsFor.DBFIELD_PCNAME), resultSet
                                .getString(ConstantsFor.DBFIELD_USERNAME), timestamp);
                            retList.add(addStr);
                        }
                    }
                }
                catch (RuntimeException e) {
                    messageToUser
                        .error(MessageFormat.format("ResolveUserInDataBase.searchDatabase", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace())));
                }
            }
            catch (SQLException e) {
                retList.add(e.getMessage());
                retList.add(AbstractForms.fromArray(e));
            }
        }
        else {
            retList.add(MessageFormat.format("{0} ConstantsFor.noRunOn", UsefulUtilities.thisPC()));
        }
        return retList;
    }

    @Override
    public List<String> getLogins(String aboutWhat, int resultsLimit) {
        Thread.currentThread().setName(getClass().getSimpleName());
        List<String> result;
        if (new NameOrIPChecker(aboutWhat).isLocalAddress()) {
            this.aboutWhat = new NameOrIPChecker(aboutWhat).resolveInetAddress().getHostName();
        }
        else {
            this.aboutWhat = aboutWhat;
        }
        if (this.aboutWhat.toString().contains("pp")) {
            result = Collections.singletonList(this.aboutWhat.toString());
        }
        else {
            result = checkDataBase(aboutWhat, resultsLimit);
        }
        if (result.size() == 0) {
            result.add(aboutWhat);
        }
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", ResolveUserInDataBase.class.getSimpleName() + "[\n", "\n]")
                .add("aboutWhat = " + aboutWhat)
                .add("dataConnectTo = " + dataConnectTo.toString())
                .toString();
    }

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

    private String getLastOnlineTime() {
        if (NetScanService.isReach(aboutWhat.toString())) {
            return "NOW!";
        }
        else {
            String info = PCInfo.getInstance(aboutWhat.toString()).getInfo();
            if (info.toLowerCase().contains("last online")) {
                info = info.split("online:")[1];
            }
            return info;
        }
    }

    @NotNull
    private List<String> checkDataBase(String aboutWhat, int resultsLimit) {
        List<String> results = searchDatabase(resultsLimit, SQL_GETLOGINS);
        if (results.size() > 0) {
            messageToUser.info(this.getClass().getSimpleName(), RESULTS, String.valueOf(results.size()));
        }
        else {
            this.aboutWhat = PCInfo.checkValidNameWithoutEatmeat(aboutWhat);
            if (this.aboutWhat.toString().contains(ConstantsFor.STR_UNKNOWN)) {
                results.add(this.aboutWhat.toString());
            }
            else {
                results = searchDatabase(resultsLimit, SQL_GETLOGINS.replace(ConstantsFor.DBFIELD_USERNAME, ConstantsFor.DBFIELD_PCNAME));
            }
            if (results.size() <= 0 | AbstractForms.fromArray(results).contains(ConstantsFor.USERS)) {
                this.aboutWhat = new NameOrIPChecker(aboutWhat).resolveInetAddress().getHostName();
                results = searchDatabase(resultsLimit, "select * from velkom.pcuser where pcName like ? limit ?");
            }
        }
        return results;
    }

    @Contract(value = "null -> false", pure = true)
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

    @NotNull String getLoginFromStaticDB(String pcName) {
        pcName = PCInfo.checkValidNameWithoutEatmeat(pcName);
        this.aboutWhat = pcName;
        List<String> velkomPCUser = searchDatabase(1, "SELECT * FROM velkom.pcuserauto WHERE pcName LIKE ? ORDER BY idRec DESC LIMIT ?");
        return HTMLGeneration.getInstance("").getHTMLCenterColor(ConstantsFor.YELLOW, AbstractForms.fromArray(velkomPCUser));
    }


}
