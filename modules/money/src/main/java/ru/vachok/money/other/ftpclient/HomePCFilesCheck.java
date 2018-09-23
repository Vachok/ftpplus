package ru.vachok.money.other.ftpclient;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.config.ConstantsFor;
import ru.vachok.mysqlandprops.props.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;


/**
 * Проверка файлов на локальном ПК
 *
 * @since 20.08.2018 (23:10)
 */
public class HomePCFilesCheck implements Callable<Stream<String>> {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = HomePCFilesCheck.class.getSimpleName();

    /**
     * {@link MessageCons}
     */
    private final MessageToUser messageToUser = new MessageCons();

    /**
     * @see DbProperties
     */
    private InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + SOURCE_CLASS);


    /**
     * @return {@link #getCheckLocalFiles()}
     */
    @Override
    public Stream<String> call() {
        return getCheckLocalFiles();
    }


    /**
     * {@link #call()}
     *
     * @return "Размер файла " + f.getName() + f.length() / ConstantsFor.MEGABYTE + " Megabytes.\n" + "Всего: " + l / ConstantsFor.MEGABYTE;
     */
    private Stream<String> getCheckLocalFiles() {
        Properties p = initProperties.getProps();
        File videoDir = new File("f:\\Video\\Captures\\IPCamera\\IV2405P_00626E6A45EA\\record\\");
        File[] locVideoFiles = videoDir.listFiles();
        List<String> toReturn = new ArrayList<>();
        messageToUser.info(this.toString(), videoDir.getAbsolutePath(), Objects.requireNonNull(locVideoFiles).length + " кол-во файлов на компе.");
        long l = 0;
        for (File f : locVideoFiles) {
            l = l + f.length();
            String retStr = ("\nРазмер файла " + f.getName() + f.length() / ConstantsFor.MEGABYTE + " Megabytes.\n" + "  Всего: " + l / ConstantsFor.MEGABYTE);
            toReturn.add(retStr);
            messageToUser.infoNoTitles(retStr);
        }
        initProperties = new FileProps(SOURCE_CLASS);
        initProperties.setProps(p);

        return toReturn.stream();
    }
}