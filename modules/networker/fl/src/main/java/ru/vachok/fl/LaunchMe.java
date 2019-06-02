package ru.vachok.fl;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;


/**
 Class ru.vachok.fl.LaunchMe
 <p>

 @since 06.05.2019 (20:41) */
public class LaunchMe {

    private static MessageToUser messageToUser = new MessageCons(ru.vachok.fl.LaunchMe.class.getSimpleName());

    public static void main(String[] args) {
        messageToUser.info(ru.vachok.fl.LaunchMe.class.getSimpleName()+".main" , "args" , " = "+args.length);
    }
}