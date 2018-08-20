package ru.vachok.money.ftpclient;



import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.logic.UTF8;
import ru.vachok.money.logic.W1251;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.*;


/**
 * Периодическая проверка FTP-сервера. Если размер файлов менялся - отправить об этом сообщение.
 *
 * @since 20.08.2018 (23:10)
 */
public class FtpHomeCamCheck implements FtpConnect, Callable<String>, Runnable {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = FtpHomeCamCheck.class.getSimpleName();

    /**
     * {@link MessageCons}
     */
    private static final MessageToUser messageToUser = new MessageCons();
    /**
     * {@link FtpConnect#getClient()}
     */
    private final FTPClient ftpClient = getClient();
    /**
     * имя папки, исходя из сегодняшней даты.
     * {@link #dateAsFolderName()}
     */
    private final String eDateSt = dateAsFolderName();
    private InitProperties dbRegProperties = new DBRegProperties(ConstantsFor.APP_NAME + SOURCE_CLASS);
    /**
     * <b>Init - </b> {@link #FtpHomeCamCheck()}
     * {@link #connect()} , {@link #isVeryOld(String)}
     */
    private Properties properties = dbRegProperties.getProps();


    /**
     * 0. <b>Конструктор-инициализатор класса</b>.
     * <p>
     * Тащит {@link #properties} из {@link DBRegProperties}.
     * <p>
     * 1 {@link #call()}
     */
    public FtpHomeCamCheck() {
        call();
    }


    @Override
    public String call() {
        return connect();
    }


    /**
     * 1. Соединение с сервером физически.
     * 1.1 {@link #dnLoader(FTPFile)}
     * 1.3 {@link #messageToSend(long , long)}
     * 1.4 {@link #eSend(String)}
     *
     * @see #getWorkFolderName()
     */
    @Override
    public String connect() {
        long sizeAll = 0;
        long size2downMeg = 0;
        for (FTPFile ftpFile : getWorkFolderName()) {
            FtpHomeCamCheck.dnLoader(ftpFile);
            sizeAll += ftpFile.getSize();
        }
        sizeAll = sizeAll / ConstantsFor.MEGABYTE;
        Object size2downMeg1 = properties.getOrDefault("size2downMeg" , "1");
        size2downMeg = Long.parseUnsignedLong(size2downMeg1.toString());
        long lo = sizeAll - size2downMeg;
        properties.setProperty("size2downMeg" , sizeAll + "");
        FtpHomeCamCheck.messageToUser.info(FtpHomeCamCheck.SOURCE_CLASS , new UTF8().toAnotherEnc("Всего в мегабайтах, после последней проверки: ") , sizeAll + "/(LO: " + lo + ")");
        if (lo != 0) eSend(messageToSend(sizeAll , lo));
        return FtpHomeCamCheck.SOURCE_CLASS + new W1251().toAnotherEnc("\nВсего в мегабайтах, после последней проверки: " + "\n") + sizeAll + "/(LO: " + lo + ")";
    }


    /**
     * Делает имя рабочей папки
     * {@link #connect()}
     *
     * @return массив {@link FTPFile} из рабочей папки {@link #eDateSt}. Получает методом {@link #ftpFiles(FTPClient , String)}, после получения рабочей папки.
     */
    @Override
    public FTPFile[] getWorkFolderName() {
        String workFolderName = null;
        String dateFolderOnServer = null;
        try {

            dateFolderOnServer = "/IPCamera/IV2405P_00626E6A45EA/record/" + eDateSt + "/";
            ftpClient.doCommand("CWD" , dateFolderOnServer);
        } catch (IOException e) {
            FtpHomeCamCheck.messageToUser.errorAlert(FtpHomeCamCheck.SOURCE_CLASS , e.getMessage() , Arrays.toString(e.getStackTrace()));
        }
        try {
            FTPFile[] dirsOnCamera = ftpClient.listDirectories();

            workFolderName = dateFolderOnServer + dirsOnCamera[0].getName() + "/";
        } catch (IOException e) {
            FtpHomeCamCheck.messageToUser.errorAlert(FtpHomeCamCheck.SOURCE_CLASS , e.getMessage() , Arrays.toString(e.getStackTrace()));
        }
        try {
            ftpClient.doCommand("CWD" , workFolderName);
        } catch (IOException e) {
            FtpHomeCamCheck.messageToUser.errorAlert(FtpHomeCamCheck.SOURCE_CLASS , e.getMessage() , Arrays.toString(e.getStackTrace()));
        }
        return ftpFiles(ftpClient , workFolderName);
    }


    /**
     * 1 {@link #connect()}
     * 1.2 загрузка одного файла с сервера.
     *
     * @param ftpFile пофайлово
     */
    private static void dnLoader( FTPFile ftpFile ) {
        messageToUser.info(ftpFile.getName() , ftpFile.getGroup() , ftpFile.getLink());
        messageToUser.info(ftpFile.getUser() , ftpFile.getSize() + " size" , new Date(ftpFile.getTimestamp().getTimeInMillis()).toString());
    }


    /**
     * 1.4 Почтовое отправление {@link #connect()}
     *
     * @param msg тело письма.
     */
    private void eSend( String msg ) {
        InitProperties toFile = new FileProps(SOURCE_CLASS);
        toFile.setProps(properties);
        dbRegProperties.delProps();
        dbRegProperties.setProps(properties);
    }


    /**
     * 1.3 {@link FtpHomeCamCheck#connect()}
     *
     * @param sizeAll размер файлов в байтах
     * @param difSize разница сейчас-сохраненный в {@link Properties}
     * @return сообщение чтобы отправить по-почте.
     */
    private String messageToSend( long sizeAll , long difSize ) {
        Callable<String> locFiles = new LocalFilesWorker();
        Future<String> submit = Executors.newSingleThreadExecutor().submit(locFiles);
        try {
            return FtpHomeCamCheck.SOURCE_CLASS + "\n" + new UTF8().toAnotherEnc("Всего в мегабайтах, после последней проверки: ") + "\n" + sizeAll + "/(dif: " + difSize + ")" + "\n" + submit.get();
        } catch (InterruptedException | ExecutionException e) {
            messageToUser.errorAlert(SOURCE_CLASS , e.getMessage() , Arrays.toString(e.getStackTrace()));
            Thread.currentThread().interrupt();
        }
        return "GATTAFUCKER";
    }


    /**
     * 1 {@link #getWorkFolderName()}
     * 1.1 {@link #isVeryOld(String)} проверка "давности". Производится путём сверки текущего времени и времени файлика <i>index.dat</i>
     * на FTP-сервере.
     * 1.2 {@link #dateAsFolderName()}
     *
     * @param ftpClient      {@link FtpConnect#getClient()}
     * @param workFolderName {@link #eDateSt}
     * @return массив {@link FTPFile}
     */
    private FTPFile[] ftpFiles( FTPClient ftpClient , String workFolderName ) {
        FTPFile[] ftpFiles = new FTPFile[0];
        String indexChangeTime = "Getting FTP Status";
        try {
            ftpFiles = ftpClient.listFiles();
            indexChangeTime = ftpClient.getModificationTime(workFolderName + "index.dat");
        } catch (IOException | NullPointerException e) {
            FtpHomeCamCheck.messageToUser.out("FTPCTest_75" , (e.getMessage() + "\n\n" + Arrays.toString(e.getStackTrace()).replaceAll(", " , " ")).getBytes());
            FtpHomeCamCheck.messageToUser.errorAlert("FTPCTest" , e.getMessage() , Arrays.toString(e.getStackTrace()));
        }
        if (isVeryOld(indexChangeTime)) {
            FtpHomeCamCheck.messageToUser.errorAlert(indexChangeTime , ftpFiles.length + " files to download. " , dateAsFolderName() + " is very old? " + isVeryOld(indexChangeTime));
        }
        if (!isVeryOld(indexChangeTime)) {
            FtpHomeCamCheck.messageToUser.info(indexChangeTime , ftpFiles.length + " files to download. " , dateAsFolderName() + " is very old? " + isVeryOld(indexChangeTime));
        }
        return ftpFiles;
    }


    /**
     * 1.1 {@link #ftpFiles(FTPClient , String)}
     *
     * @param indexChangeTime timestamp когда менялся файл index.dat
     * @return давно или нет производились изменения.
     */
    private boolean isVeryOld( String indexChangeTime ) {
        int hourNow = LocalTime.now().getHour();
        String hourNowString = String.valueOf(LocalTime.now().getHour());
        if (hourNow < 10) hourNowString = "0" + LocalTime.now().getHour();
        String minNowString = String.valueOf(LocalTime.now().getMinute());
        int minuteNow = LocalTime.now().getMinute();
        if (minuteNow < 10) minNowString = "0" + minNowString;
        String secNowString = String.valueOf(LocalTime.now().getSecond());
        int secondNow = LocalTime.now().getSecond();
        if (secondNow < 10) secNowString = "0" + secNowString;
        String nowTime = hourNowString + minNowString + secNowString;
        try {
            long l = Long.parseLong(dateAsFolderName() + nowTime);
            long l1 = Long.parseLong(indexChangeTime);
            long l2 = l - l1;
            properties.setProperty("now" , String.valueOf(l));
            properties.setProperty("was" , String.valueOf(l1));
            properties.setProperty("diff" , String.valueOf(l2));
            return l2 > 100000;
        } catch (NumberFormatException e) {
            FtpHomeCamCheck.messageToUser.errorAlert("No index.dat" , e.getMessage() , "ID 129");
        }
        return false;
    }


    /**
     * 1 {@link #ftpFiles(FTPClient , String)}
     * 1.2 создаёт имя {@link #eDateSt}
     *
     * @return имя рабочей папки на FTP
     *
     * @see Calendar
     * {@link #eDateSt}
     */
    private String dateAsFolderName() {
        Integer year = Calendar.getInstance().get(Calendar.YEAR);
        Integer month = Calendar.getInstance().get(Calendar.MONTH);
        month = month + 1;
        Integer day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String dateT = year.toString() + "0" + month.toString() + day.toString();
        if (day < 10 && month < 10) dateT = year.toString() + "0" + month.toString() + "0" + day.toString();
        if (day >= 10 && month >= 10) dateT = year.toString() + month.toString() + day.toString();
        if (day < 10 && month >= 10) dateT = year.toString() + month.toString() + "0" + day.toString();
        return dateT;
    }


    /**
     *
     */
    @Override
    public void run() {
        long l = System.currentTimeMillis();
        Callable<String> locFiles = new LocalFilesWorker();
        Future<String> submit = Executors.newSingleThreadExecutor().submit(locFiles);
        String filesLocal;
        try {
            filesLocal = submit.get();
            messageToUser.infoNoTitles(filesLocal);
            String s2 = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - l) + new UTF8().toAnotherEnc(" сек.");
            String s1 = new UTF8().toAnotherEnc("Время");
            messageToUser.info(SOURCE_CLASS + ".run" , s1 , s2);
            eSend(filesLocal + s1 + s2);
        } catch (InterruptedException | ExecutionException e) {
            messageToUser.errorAlert(SOURCE_CLASS , e.getMessage() , Arrays.toString(e.getStackTrace()));
            Thread.currentThread().interrupt();
        }
    }
}