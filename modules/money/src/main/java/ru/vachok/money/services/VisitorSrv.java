package ru.vachok.money.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 @since 14.09.2018 (19:48) */
@Service
public class VisitorSrv {

    /*Fields*/

    /**
     {@link }
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VisitorSrv.class.getSimpleName());

    /**
     Simple Name класса, для поиска настроек
     */

    private HttpServletRequest request;

    private HttpServletResponse response;

    public void makeVisit(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = request.getSession().getId();
        String msg = "SET NEW Session ID" + request.getSession().getId() + " sessionID = " + sessionId;
        LOGGER.warn(msg);
    }
}