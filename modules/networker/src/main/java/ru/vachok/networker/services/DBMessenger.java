// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * @since 26.08.2018 (12:29)
 */
public class DBMessenger implements MessageToUser {
    
    
    private String headerMsg;
    
    private String titleMsg;
    
    private String bodyMsg;
    
    public DBMessenger(String headerMsg) {
        this.headerMsg = headerMsg;
    }
    
    @Override public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg; this.titleMsg = titleMsg; this.bodyMsg = bodyMsg; dbSend(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override public void infoNoTitles(String s) {
        dbSend(headerMsg, "INFO", s);
    }


    @Override
    public void info( String s , String s1 , String s2 ) {
        dbSend(s,s1,s2);
    }

    @Override public void error(String s) {
        this.bodyMsg = s; errorAlert(headerMsg, "untitled", s);
    }

    @Override
    public void info(String s) {
        infoNoTitles(s);
    }

    @Override public void error(String headerMsg, String s1, String s2) {
        this.headerMsg = headerMsg; this.titleMsg = s1; this.bodyMsg = s2; errorAlert(headerMsg, s1, s2);
    }
    
    private void dbSend(String headerMsg, String titleMsg, String bodyMsg) {
        final String sql = "insert into ru_vachok_networker (classname, msgtype, msgvalue, pc) values (?,?,?,?)";
        try (Connection c = new AppComponents().connection(ConstantsFor.DBPREFIX + "webapp");
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, headerMsg);
            p.setString(2, titleMsg);
            p.setString(3, bodyMsg);
            p.setString(4, ConstantsFor.thisPC() + " up: " + ConstantsFor.getUpTime());
            p.executeUpdate();
        }
        catch (SQLException | IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".dbSend");
        }
    }

    @Override
    public void infoTimer( int i , String s ) {
        throw new UnsupportedOperationException("07.09.2018 (0:11)");
    }

    @Override
    public void warn(String s, String s1, String s2) {
        info(s, s1, s2);
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