// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.exceptions;


import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.text.MessageFormat;


/**
 Class ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException
 <p>

 @since 23.06.2019 (0:27) */
public class InvokeIllegalException extends Throwable {


    private final String message;

    public InvokeIllegalException(String message) {
        this.message = message;
        if (UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
            sendEmail();
        }
    }

    private void sendEmail() {
        MessageToUser toUser = MessageToUser.getInstance(MessageToUser.EMAIL, "InvokeIllegalException");
        toUser.error(message);
    }

    public InvokeIllegalException() {
        this.message = "This functional is not ready yet.";
    }

    @Override
    public String getMessage() {
        System.out.println("InvokeIllegalException = " + message);
        return MessageFormat.format("{0}\n This is {1} :{2}", message, ConstantsFor.APPNAME_WITHMINUS, getStackTrace()[0]);
    }
}