package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UnknownFormatConversionException;


/**
 @see ru.vachok.networker.net.scanner.DatabasePCSearcherTest
 @since 08.08.2019 (13:20) */
public class DatabasePCSearcher implements InformationFactory {
    
    
    private static final TForms T_FORMS = new TForms();
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        List<String> infoFromDBGetter = new ArrayList<>();
        try {
            infoFromDBGetter = theInfoFromDBGetter(aboutWhat);
        }
        catch (UnknownHostException | UnknownFormatConversionException e) {
            infoFromDBGetter.add(MessageFormat.format("DatabasePCSearcher.getInfoAbout: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        if (infoFromDBGetter.isEmpty()) {
            infoFromDBGetter.add("ok");
        }
        return T_FORMS.fromArray(infoFromDBGetter);
    }
    
    @Override
    public void setInfo(Object info) {
        AppComponents.netScannerSvc().setThePc((String) info);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatabasePCSearcher{");
        sb.append('}');
        return sb.toString();
    }
    
    private @NotNull List<String> theInfoFromDBGetter(@NotNull String thePcLoc) throws UnknownHostException, UnknownFormatConversionException {
        StringBuilder sqlQBuilder = new StringBuilder();
        
        if (thePcLoc.isEmpty()) {
            IllegalArgumentException argumentException = new IllegalArgumentException("Must be NOT NULL!");
            sqlQBuilder.append(argumentException.getMessage());
        }
        else if (new NameOrIPChecker(thePcLoc).resolveIP().isLinkLocalAddress()) {
            sqlQBuilder.append("select * from velkompc where NamePP like '%").append(thePcLoc).append("%'");
            return dbGetter(thePcLoc, sqlQBuilder.toString());
        }
        return Collections.singletonList("ok");
    }
    
    private List<String> dbGetter(@NotNull String thePcLoc, final String sql) {
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return parseResultSet(resultSet, thePcLoc);
            }
        }
        catch (SQLException | IndexOutOfBoundsException | UnknownHostException e) {
            return Collections.singletonList(MessageFormat.format("DatabasePCSearcher.dbGetter: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    private @NotNull List<String> parseResultSet(@NotNull ResultSet resultSet, @NotNull String thePcLoc) throws SQLException, UnknownHostException {
        List<String> timeNowDatabaseFields = new ArrayList<>();
        List<Integer> integersOff = new ArrayList<>();
        while (resultSet.next()) {
            int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
            if (onlineNow == 1) {
                timeNowDatabaseFields.add(resultSet.getString(ConstantsFor.DBFIELD_TIMENOW));
            }
            else {
                integersOff.add(onlineNow);
            }
            StringBuilder stringBuilder = new StringBuilder();
            String namePP = new StringBuilder()
                .append("<center><h2>").append(InetAddress.getByName(thePcLoc + ConstantsFor.DOMAIN_EATMEATRU)).append(" information.<br></h2>")
                .append("<font color = \"silver\">OnLines = ").append(timeNowDatabaseFields.size())
                .append(". Offline = ").append(integersOff.size()).append(". TOTAL: ")
                .append(integersOff.size() + timeNowDatabaseFields.size()).toString();
            
            stringBuilder
                .append(namePP)
                .append(". <br>");
            setInfo(stringBuilder.toString());
        }
        return timeNowDatabaseFields;
    }
}
