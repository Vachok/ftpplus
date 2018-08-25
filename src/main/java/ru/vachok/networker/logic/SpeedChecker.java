package ru.vachok.networker.logic;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.networker.DBMessanger;

import java.util.Date;


/**
 @since 22.08.2018 (9:36) */
public class SpeedChecker implements Runnable{

    private static final MessageToUser messageToUser = new DBMessanger();

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
        SpeedRunActualize speedRunActualize = new SpeedRunActualize();
        speedRunActualize.run();
        speedRunActualize.avgInfo(0);
        speedRunActualize.avgInfo(1);
        messageToUser.infoNoTitles(SpeedChecker.class.getSimpleName()+" is run! "+ new Date());
    }
}
