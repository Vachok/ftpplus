package ru.vachok.networker.services;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * @since 26.08.2018 (12:29)
 */
public class DBMessenger implements MessageToUser {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = DBMessenger.class.getSimpleName();

    @Override
    public void errorAlert( String s , String s1 , String s2 ) {
        dbSend(s,s1,s2);
    }

    private void dbSend(String s , String s1 , String s2 ) {
        String sql = "insert into ru_vachok_networker (classname, msgtype, msgvalue) values (?,?,?)";
        try (Connection c = new AppComponents().connection(ConstantsFor.DBPREFIX + "webapp");
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1,s);
            p.setString(2,s1);
            p.setString(3,s2);
            p.executeUpdate();
        }
        catch(SQLException | IOException e){
            FileSystemWorker.error("DBMessenger.dbSend", e);
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
    public void info(String s) {
        infoNoTitles(s);
    }

    @Override
    public void error(String s) {
        errorAlert("", "", s);
    }

    @Override
    public void error(String s, String s1, String s2) {
        errorAlert(s, s1, s2);
    }

    @Override
    public void infoTimer( int i , String s ) {
        throw new UnsupportedOperationException("07.09.2018 (0:11)");
    }

    @Override
    public void warn(String s, String s1, String s2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void warn(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void warning(String s, String s1, String s2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void warning(String s) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String confirm( String s , String s1 , String s2 ) {
        throw new UnsupportedOperationException("07.09.2018 (0:11)");
    }
}