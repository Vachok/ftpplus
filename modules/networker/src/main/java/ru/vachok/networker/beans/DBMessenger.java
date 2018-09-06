package ru.vachok.networker.beans;


import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.config.AppComponents;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * @since 26.08.2018 (12:29)
 */
@Service ("MessageToUser")
public class DBMessenger implements MessageToUser {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = DBMessenger.class.getSimpleName();
    private static final DataConnectTo REG_RU_MYSQL = new RegRuMysql();

    @Override
    public void errorAlert( String s , String s1 , String s2 ) {
        dbSend(s,s1,s2);
    }


    /*Private metsods*/
    private void dbSend(String s , String s1 , String s2 ) {
        String sql = "insert into ru_vachok_networker (classname, msgtype, msgvalue) values (?,?,?)";
        try (Connection c = REG_RU_MYSQL.getDefaultConnection(ConstantsFor.DB_PREFIX+"webapp");
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1,s);
            p.setString(2,s1);
            p.setString(3,s2);
            p.executeUpdate();
        }catch (SQLException e){
            AppComponents.logger().error(e.getMessage(), e);
        }
    }


    @Override
    public void info( String s , String s1 , String s2 ) {
        dbSend(s,s1,s2);
    }


    @Override
    public void infoNoTitles( String s ) {
        dbSend(SOURCE_CLASS, "INFO", s);
    }


    @Override
    public void infoTimer( int i , String s ) {
        throw new UnsupportedOperationException("07.09.2018 (0:11)");
    }


    @Override
    public String confirm( String s , String s1 , String s2 ) {
        throw new UnsupportedOperationException("07.09.2018 (0:11)");
    }
}