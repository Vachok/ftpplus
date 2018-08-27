package ru.vachok.networker;



import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.web.ConstantsFor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * @since 26.08.2018 (12:29)
 */
public class DBMessanger implements MessageToUser {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = DBMessanger.class.getSimpleName();
    private static final DataConnectTo REG_RU_MYSQL = new RegRuMysql();


    @Override
    public void errorAlert( String s , String s1 , String s2 ) {
        dbSend(s,s1,s2);
    }


    private void dbSend( String s , String s1 , String s2 ) {
        String sql = "insert into networker (classname, msgtype, msgvalue) values (?,?,?)";
        try (Connection c = REG_RU_MYSQL.getDefaultConnection(ConstantsFor.DB_PREFIX+"webapp");
             PreparedStatement p = c.prepareStatement(sql);){
            p.setString(1,s);
            p.setString(2,s1);
            p.setString(3,s2);
            p.executeUpdate();
        }catch (SQLException e){
            ApplicationConfiguration.logger().error(e.getMessage(), e);
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
        throw new UnsupportedOperationException();
    }


    @Override
    public String confirm( String s , String s1 , String s2 ) {
        throw new UnsupportedOperationException();
    }
}