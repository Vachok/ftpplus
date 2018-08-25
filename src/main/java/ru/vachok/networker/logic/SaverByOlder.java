package ru.vachok.networker.logic;



import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.DBMessanger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;


/**
 * The type Saver by older.
 *
 * @since 11.08.2018 (18:20)
 */
public class SaverByOlder implements Runnable {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = SaverByOlder.class.getSimpleName();
    /**
     * {@link }
     */
    private static MessageToUser messageToUser = new DBMessanger();
    private Map<String, String> showMe;


    /**
     * Instantiates a new Saver by older.
     *
     * @param showMe the show me
     */
    public SaverByOlder( Map<String, String> showMe ) {
        this.showMe = showMe;
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        BiConsumer<String, String> biConsumer = ( x , y ) -> {
            File file = new File("show.me");
            try {
                FileWriter fileWriter = new FileWriter(file);
            } catch (IOException e) {
                messageToUser.errorAlert(SOURCE_CLASS , e.getMessage() , e.getStackTrace().toString().replaceAll(", " , "\n"));
            }
        };
        showMe.forEach(biConsumer);
    }
}