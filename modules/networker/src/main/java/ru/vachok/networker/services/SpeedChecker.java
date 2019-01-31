package ru.vachok.networker.services;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.EMailAndDB.MailMessages;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.systray.ActionDefault;
import ru.vachok.networker.systray.MessageToTray;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 @since 22.08.2018 (9:36) */
public class SpeedChecker implements Callable<Long> {

    /**
     {@link RegRuMysql}
     */
    private static final DataConnectTo DATA_CONNECT_TO = new RegRuMysql();

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    @Override
    public Long call() {
        Long chkForLastLong = chkForLast();
        String msg = new java.util.Date(chkForLastLong) + " from " + SpeedChecker.class.getSimpleName();
        LOGGER.info(msg);
        new MessageToTray().infoNoTitles(new Date(chkForLastLong).toString());
        return chkForLastLong;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    private static Long chkForLast() {
        String classMeth = "SpeedChecker.chkForLast";
        new MessageCons().infoNoTitles(classMeth);
        Thread.currentThread().setName(classMeth);
        final long stArt = System.currentTimeMillis();
        String sql = ConstantsFor.SELECT_FROM_SPEED;
        Long rtLong = Calendar.getInstance().getTimeInMillis() - ConstantsFor.getAtomicTime();
        Connection c = DATA_CONNECT_TO.getDefaultConnection(ConstantsFor.DB_PREFIX + "liferpg");
        try (PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                if (r.last()) {
                    double timeSpend = r.getDouble(ConstantsFor.TIME_SPEND);
                    long timeStamp = r.getTimestamp(ConstantsFor.COL_SQL_NAME_TIMESTAMP).getTime();
                    String msg = timeSpend + " time spend;\n" + timeStamp;
                    rtLong = timeStamp + TimeUnit.MINUTES.toMillis(3);
                    LOGGER.info(msg);
                    return rtLong;
                }
            }
        } catch (SQLException e) {
            new MessageCons().errorAlert("SpeedChecker", "chkForLast", e.getMessage());
            FileSystemWorker.error("SpeedChecker.chkForLast", e);
        }
        methMetr(stArt);
        return rtLong;
    }

    private static void methMetr(long stArt) {
        float f = (float) (System.currentTimeMillis() - stArt) / 1000;
        String msgTimeSp = new StringBuilder()
            .append("SpeedChecker.chkForLast: ")
            .append(f)
            .append(ConstantsFor.STR_SEC_SPEND)
            .toString();
        LOGGER.info(msgTimeSp);
    }

    public static final class ChkMailAndUpdateDB implements Runnable {

        private MailMessages mailMessages = new MailMessages();

        @Override
        public void run() {
            String msg = chechMail();
            LOGGER.info(msg);
        }

        private String chechMail() {
            Message[] messagesBot = mailMessages.call();
            String chDB = new TForms().fromArray(checkDB(), false);
            FileSystemWorker.recFile(this.getClass().getSimpleName() + ConstantsFor.LOG, Collections.singletonList(chDB));
            for (Message m : messagesBot) {
                parseMsg(m, chDB);
            }
            return chDB;
        }

        private Map<String, String> checkDB() {
            Map<String, String> retMap = new HashMap<>();
            DataConnectTo dataConnectTo = new RegRuMysql();
            String sql = ConstantsFor.SELECT_FROM_SPEED;
            try (Connection c = dataConnectTo.getDefaultConnection(ConstantsFor.U_0466446_LIFERPG);
                 PreparedStatement p = c.prepareStatement(sql);
                 ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    String valueS = r.getInt("Road") +
                        " road, " +
                        r.getString("Speed") +
                        " speed, " + r.getString(ConstantsFor.TIME_SPEND) + " time in min, " +
                        DayOfWeek.of(r.getInt("WeekDay") - 1);
                    retMap.put(r.getTimestamp(ConstantsFor.COL_SQL_NAME_TIMESTAMP).toString(), valueS);
                }
                retMap.put(LocalDateTime.now().toString(), "okok");
            } catch (SQLException e) {
                retMap.put(e.getMessage(), new TForms().fromArray(e, false));
            }
            return retMap;
        }

        private void parseMsg(Message m, String chDB) {
            MessageToUser eSender = new ESender(ConstantsFor.GMAIL_COM);
            try {
                String subjMail = m.getSubject();
                if (subjMail.toLowerCase().contains("speed:")) {
                    Date dateSent = m.getSentDate();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dateSent);
                    LocalDate of = LocalDate.of(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH));
                    int dayOfWeek = of.getDayOfWeek().getValue();
                    long timeSt = calendar.getTimeInMillis();
                    if (writeDB(m.getSubject().toLowerCase().split("speed:")[1], dayOfWeek, timeSt)) delMessage(m);
                    eSender.info(ChkMailAndUpdateDB.class.getSimpleName(), true + " sending to base", chDB);
                } else {
                    new MessageToTray(new ActionDefault(ConstantsFor.HTTP_LOCALHOST_8880_SLASH)).infoNoTitles("No new messages");
                }
            } catch (MessagingException e) {
                eSender.errorAlert(
                    this.getClass().getSimpleName(),
                    LocalDateTime.now() + " " + e.getMessage(),
                    new TForms().fromArray(e, false));
            }
        }

        private boolean writeDB(String s, int dayOfWeek, long timeSt) {
            double timeSpend;
            double speedFromStr = Double.parseDouble(s.split(" ")[0]);
            int roadFromStr = Integer.parseInt(s.split(" ")[1]);
            if (roadFromStr == 0) timeSpend = (21.6 / speedFromStr) * 60;
            else timeSpend = (31.2 / speedFromStr) * 60;
            Timestamp timestamp = new Timestamp(timeSt);
            String sql = "insert into speed (Speed, Road, WeekDay, TimeSpend, TimeStamp) values (?,?,?,?,?)";
            try (Connection c = new RegRuMysql().getDefaultConnection("u0466446_liferpg");
                 PreparedStatement p = c.prepareStatement(sql)) {
                p.setDouble(1, speedFromStr);
                p.setInt(2, roadFromStr);
                p.setInt(3, dayOfWeek + 1);
                p.setFloat(4, (float) timeSpend);
                p.setTimestamp(5, timestamp);
                p.executeUpdate();
                new MessageToTray().info("DB updated", "Today is " + DayOfWeek.of(dayOfWeek), " Time spend " + timeSpend);
                return true;
            } catch (SQLException e) {
                new MessageCons().errorAlert("ChkMailAndUpdateDB", "writeDB", e.getMessage());
                FileSystemWorker.error("ChkMailAndUpdateDB.writeDB", e);
                return false;
            }
        }

        private void delMessage(Message m) {
            Folder inboxFolder = mailMessages.getInbox();
            try {
                inboxFolder.getMessage(m.getMessageNumber()).setFlag(Flags.Flag.DELETED, true);
                inboxFolder.close(true);
            } catch (MessagingException e) {
                new MessageCons().errorAlert("ChkMailAndUpdateDB", "delMessage", e.getMessage());
                FileSystemWorker.error("ChkMailAndUpdateDB.delMessage", e);
            }
        }
    }
}
