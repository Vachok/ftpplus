package ru.vachok.money.web;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.DBMessage;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


/**
 @since 01.09.2018 (22:41) */
public class Cont implements ServletContextAttributeListener, HttpSessionListener {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = Cont.class.getSimpleName();

    private static final UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("Not Completed|Не готово... 01.09.2018 (22:46)");

    /**
     {@link }
     */
    private static MessageToUser messageToUser = new DBMessage();

    /**
     Notification that a new attribute was added to the servlet context.
     Called after the attribute is added.  @param scae Information about the new attribute
     */
    @Override
    public void attributeAdded(ServletContextAttributeEvent scae) {
        throw unsupportedOperationException;
    }

    /**
     Notification that an existing attribute has been removed from the servlet
     context. Called after the attribute is removed.  @param scae Information about the removed attribute
     */
    @Override
    public void attributeRemoved(ServletContextAttributeEvent scae) {
        throw unsupportedOperationException;
    }

    /**
     Notification that an attribute on the servlet context has been replaced.
     Called after the attribute is replaced.  @param scae Information about the replaced attribute
     */
    @Override
    public void attributeReplaced(ServletContextAttributeEvent scae) {
        throw unsupportedOperationException;
    }

    /**
     Notification that a session was created.

     @param se the notification event
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        throw unsupportedOperationException;
    }

    /**
     Notification that a session is about to be invalidated.

     @param se the notification event
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        throw unsupportedOperationException;
    }
}