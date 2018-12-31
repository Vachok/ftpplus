package ru.vachok.money.other;


import org.springframework.scheduling.annotation.Scheduled;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.services.DBMessage;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;


/**
 <b>Добавляет данные о ср. скорости в базу.</b>
 <p>
 В данной реализации из e-mail.

 @since 29.07.2018 (11:42) */
@Deprecated
public class SpeedRunActualizeLOC implements Callable<String> {


    /**
     {@linkplain SpeedRunActualizeLOC}
     */
    private static final String SOURCE_CLASS = SpeedRunActualizeLOC.class.getSimpleName();
    /**
     <b>Сообщения пользователю</b>
     {@link #messageToUser}
     */
    private final MessageToUser messageToUser = new DBMessage();

    /**
     {@link RegRuMysql}

     @see #DEF_CON
     */
    private static final DataConnectTo DATA_CONNECT_TO = new RegRuMysql();

    /**
     <b>Соединение с параметрами по-умолчанию.</b>
     {@link DataConnectTo#getDefaultConnection(String)}
     */
    private static final Connection DEF_CON = DATA_CONNECT_TO.getDefaultConnection(ConstantsFor.U_0466446_LIFERPG);

    /**
     0. Запуск
     .1 {@link #getMailMessages()}
     .2 {@link #checkDates(Map)}

     @see MailMessages
     */
    @Override
    @Scheduled (fixedDelay = 10000)
    public String call() {
        Thread.currentThread().setName("SpeedRunActualizeLOC.run");
        Map<Date, String> mailMessages = getMailMessages();
        messageToUser.infoNoTitles(mailMessages.toString());
        checkDates(mailMessages);
        throw new UnsupportedOperationException("Deprecated. 02.10.2018 (1:22)");
    }

    /**
     <b>Получить сообщения из ПЯ</b>.
     <p>

     @return {@link Message}[]
     */
    private Map<Date, String> getMailMessages() {
        Callable<Message[]> mailCall = new MailMessages();
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<Message[]> submit = executorService.submit(mailCall);
        Map<Date, String> mailMessagesMap = new HashMap<>();
        try{
            Message[] mailMsg = submit.get();
            for(Message mail : mailMsg){
                if(mail.getSubject()!=null){
                    if(mail.getSubject().toLowerCase().contains("SPEED:")){
                        mailMessagesMap.put(mail.getSentDate(), mail.getSubject().toLowerCase().replaceFirst("\\Qspeed:\\E", ""));
                    }
                    if(mail.getSubject().toLowerCase().contains("SPEED: ")){
                        mailMessagesMap.put(mail.getSentDate(), mail.getSubject().toLowerCase().replaceFirst("\\Qspeed:\\E", ""));
                    }
                }
            }
            return mailMessagesMap;
        }
        catch(MessagingException | ExecutionException | InterruptedException e){
            messageToUser.errorAlert(SOURCE_CLASS, "ID - 80", e.getMessage());
            Thread.currentThread().interrupt();
        }
        throw new UnsupportedOperationException("27.07.2018 (16:33), ***************I CANT MAKE THIS ORDER, SORRY MAN! *********************" +
            "\n\n" + "*****************     ru.vachok.pbem.chess.utilitar.SpeedRunActualizeLOC.getMailMessages      ******************************");
    }

    /**
     <b>Проверка даты последней записи в БД</b>
     Сверка даты полученного письма, с последней записью в БД.
     Период - 22 часа.
     <p>
     {@link MailMessages#call()}

     @param mailMessages {@link Map} почтовых сообщений. {@link Date} - дата сообщения, как ключ. {@link String} - значение. Письмо как строка.
     */
    private void checkDates(Map<Date, String> mailMessages) {
        Set<Date> dates = mailMessages.keySet();
        Map<Date, String> sendDB = new HashMap<>();
        DATA_CONNECT_TO.getSavepoint(DEF_CON);
        try(PreparedStatement p = DEF_CON.prepareStatement("select * from speed");
            ResultSet rs = p.executeQuery()){
            for(Date d : dates){
                while(rs.next()){
                    Timestamp timeStamp = rs.getTimestamp("TimeStamp");
                    long timeMessageSent = d.getTime();
                    long timeInDB = timeStamp.getTime();
                    long l = TimeUnit.MILLISECONDS.toHours(timeInDB - timeMessageSent);
                    if(l <= 22){
                        messageToUser.infoNoTitles(l + " hrs");
                        sendDB.put(d, mailMessages.get(d));
                    }
                }
            }
            messageToUser.info(SOURCE_CLASS, "Map<Date, String> sendDB", sendDB.size() + ".");
            sendSpeed(sendDB);
        }
        catch(SQLException e){
            DATA_CONNECT_TO.setSavepoint(DEF_CON);
            messageToUser.errorAlert(SOURCE_CLASS, "ID - 58", e.getMessage() + "\n\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     {@link #checkDates(Map)}
     Выводит сообщения полльзователю и работает с БД, через {@link BiConsumer}
     <p>
     Раскладывает прилетевший {@link Map} на {@link Date} + {@link String}.
     <p>
     {@link MailMessages}(<b>true</b>)

     @param toDB {@link Map} {@link Date} + {@link String}, информация о дате записи и скорости. Прошедшая через фильтр ({@link #checkDates(Map)}) почтовое сообщение
     */
    private void sendSpeed(Map<Date, String> toDB) {
        AtomicInteger road = new AtomicInteger();
        messageToUser.infoNoTitles("StartMePChessOnBoardTest.sendSpeed");
        messageToUser.infoNoTitles("toDB = [" + toDB + "]");
        BiConsumer<Date, String> biConsumer = (x, y) -> {
            double avSpeed = 0.0;
            try{

                avSpeed = Double.parseDouble(y.split(" ")[0]);
            }
            catch(NumberFormatException e){
                avSpeed = Double.parseDouble(y.split(", ")[0]);
                messageToUser.out(SOURCE_CLASS, (e.getMessage() + "\n\n" + Arrays.toString(e.getStackTrace()).replaceAll(", ", "\n")).getBytes());
            }
            Date time = Calendar.getInstance().getTime();
            int weekDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            long i = TimeUnit.MILLISECONDS.toMinutes(time.getTime() - x.getTime());
            messageToUser.info("dateL.compareTo(time)", i + " ?", "insert into SPEED (Speed, Road, WeekDay, TimeSpend) values (?,?,?,?)");
            double timeSpend = 0.0;
            road.set(Integer.valueOf(y.split(" ")[1]));
            if(road.get()==1) timeSpend = (ConstantsFor.NRIGA / avSpeed) * 60;
            if(road.get()==0) timeSpend = (ConstantsFor.A107 / avSpeed) * 60;
            try(PreparedStatement ps = DEF_CON.prepareStatement("insert into speed (Speed, Road, WeekDay, TimeSpend) values (?,?,?,?)");
                PreparedStatement p = DEF_CON.prepareStatement("SELECT speed.TimeStamp FROM u0466446_liferpg.speed order by idspeed desc limit 1;");
                ResultSet r = p.executeQuery()){
                long timeStampDB = 0;
                while(r.next()){
                    if(r.last()){
                        long l = r.getTimestamp("TimeStamp").getTime();
                        timeStampDB = TimeUnit.MILLISECONDS.toMinutes(time.getTime() - l);
                    }

                }
                ps.setDouble(1, avSpeed);
                messageToUser.infoNoTitles(avSpeed + "");

                ps.setInt(2, road.get());
                messageToUser.infoNoTitles(road + "");

                ps.setInt(3, weekDay);
                messageToUser.infoNoTitles(weekDay + "");

                ps.setDouble(4, timeSpend);
                messageToUser.infoNoTitles(timeSpend + "");
                long iL = (22 * 60);
                if(timeStampDB >= iL){
                    ps.executeUpdate();
                }
            }
            catch(SQLException e){
                DATA_CONNECT_TO.setSavepoint(DEF_CON);
                messageToUser.errorAlert(SOURCE_CLASS, "ID - 110",
                    e.getMessage() + "\n\n" +
                        Arrays.toString(e.getStackTrace()).replaceAll(", ", " ").replace("{", "").replace("}", ""));
            }
        };
        toDB.forEach(biConsumer);
    }
}