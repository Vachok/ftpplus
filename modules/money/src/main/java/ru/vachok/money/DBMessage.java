package ru.vachok.money;



import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * @since 28.08.2018 (0:30)
 */
public class DBMessage implements MessageToUser {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = DBMessage.class.getSimpleName();
    private static final DataConnectTo DATA_CONNECT_TO = new RegRuMysql();
    private static final Connection c = DATA_CONNECT_TO.getDefaultConnection(ConstantsFor.DB_PREFIX + "webapp");


    @Override
    public void info( String s , String s1 , String s2 ) {
        errorAlert(s , s1 , s2);
    }


    @Override
    public void errorAlert( String s , String s1 , String s2 ) {
        dbMSG(s , s1 , s2);
    }


    private void dbMSG( String s , String s1 , String s2 ) {
        String sql = "insert into ru_vachok_money (classname, msgtype, msgname) values (?,?,?)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1 , s);
            p.setString(2 , s1);
            p.setString(3 , s2);
        } catch (SQLException e) {
            ApplicationConfiguration.getLogger().error(e.getMessage() , e);
        }
    }


    @Override
    public void infoNoTitles( String s ) {
        errorAlert(SOURCE_CLASS , "Info. No title" , s);
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