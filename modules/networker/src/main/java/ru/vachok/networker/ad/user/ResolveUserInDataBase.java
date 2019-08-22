// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.restapi.DataConnectTo;

import java.net.InetAddress;
import java.sql.*;
import java.util.StringJoiner;


/**
 @see ResolveUserInDataBaseTest
 @since 02.04.2019 (10:25) */
public class ResolveUserInDataBase extends UserInfo {
    
    
    private Object aboutWhat;
    
    private DataConnectTo dataConnectTo = DataConnectTo.getDefaultI();
    
    public String getUsage(String userCred) {
        throw new TODOException("21.08.2019 (12:50)");
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        InetAddress address = new NameOrIPChecker(searchAutoResolvedPCName()).resolveIP();
        return address.getHostAddress();
    }
    
    private @NotNull String searchAutoResolvedPCName() {
        MysqlDataSource mysqlDataSource = dataConnectTo.getDataSource();
        String retStr = "No info";
        try (Connection connection = mysqlDataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("SELECT * FROM `pcuserauto` WHERE `userName` LIKE ? ORDER BY `pcuserauto`.`whenQueried` DESC LIMIT 1")) {
                preparedStatement.setString(1, String.format("%%%s%%", aboutWhat));
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        retStr = resultSet.getString(ConstantsFor.DBFIELD_PCNAME);
                    }
                }
            }
        }
        catch (SQLException e) {
            retStr = e.getMessage();
        }
        return retStr;
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.aboutWhat = classOption;
    }
    
    @Override
    public String getInfo() {
        throw new TODOException("ru.vachok.networker.ad.user.UserPCInfo.getInfo created 21.08.2019 (12:29)");
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ResolveUserInDataBase.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
}
