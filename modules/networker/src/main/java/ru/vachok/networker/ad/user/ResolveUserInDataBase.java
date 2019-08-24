// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.DataConnectTo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;


/**
 @see ResolveUserInDataBaseTest
 @since 02.04.2019 (10:25) */
class ResolveUserInDataBase extends UserInfo {
    
    
    private Object aboutWhat;
    
    private DataConnectTo dataConnectTo = DataConnectTo.getDefaultI();
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        List<String> foundedUserPC = searchAutoResolvedPCName(1, "SELECT * FROM `pcuserauto` WHERE `userName` LIKE ? ORDER BY `pcuserauto`.`whenQueried` DESC LIMIT ?");
        if (foundedUserPC.size() > 0) {
            return new NameOrIPChecker(foundedUserPC.get(0)).resolveIP().getHostAddress();
        }else {
            return new NameOrIPChecker(aboutWhat).resolveIP().getHostAddress();
        }
    }
    
    private @NotNull List<String> searchAutoResolvedPCName(int linesLimit, String sql) {
        MysqlDataSource mysqlDataSource = dataConnectTo.getDataSource();
        List<String> retList = new ArrayList<>();
        try (Connection connection = mysqlDataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection
                .prepareStatement(sql)) {
                preparedStatement.setString(1, String.format("%%%s%%", aboutWhat));
                preparedStatement.setInt(2, linesLimit);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        retList.add(MessageFormat.format("{0} : {1}\n", resultSet.getString(ConstantsFor.DBFIELD_PCNAME), resultSet.getString(ConstantsFor.DB_FIELD_USER)));
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
    public void setClassOption(Object classOption) {
        this.aboutWhat = classOption;
    }
    
    @Override
    public List<String> getPossibleVariantsOfPC(String userName, int resultsLimit) {
        this.aboutWhat = userName;
        return searchAutoResolvedPCName(resultsLimit, "SELECT * FROM `pcuserauto` WHERE `userName` LIKE ? ORDER BY `pcuserauto`.`whenQueried` DESC LIMIT ?");
    }
    
    @Override
    public List<String> getPossibleVariantsOfUser(String pcName, int resultsLimit) {
        this.aboutWhat = pcName;
        return searchAutoResolvedPCName(resultsLimit, "SELECT * FROM `pcuserauto` WHERE `pcName` LIKE ? ORDER BY `pcuserauto`.`whenQueried` DESC LIMIT ?");
    }
    
    @Override
    public String getInfo() {
        if (aboutWhat != null) {
            return getInfoAbout((String) aboutWhat);
        }
        else {
            return MessageFormat.format("Identificator is not set <br>\n{0}", this);
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ResolveUserInDataBase.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
}
