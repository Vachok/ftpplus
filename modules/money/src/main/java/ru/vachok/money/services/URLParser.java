package ru.vachok.money.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.URLContent;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;


/**
 @since 01.10.2018 (21:52) */
@Service
public class URLParser {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(URLParser.class.getSimpleName());

    private URLContent urlContent;

    public URLContent getUrlContent() {
        return urlContent;
    }

    /*Instances*/
    @Autowired
    public URLParser(URLContent urlContent) {
        this.urlContent = urlContent;
    }

    public void showContents() {
        InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + URLParser.class.getSimpleName());
        String urlStr = initProperties.getProps().getProperty("url", ConstantsFor.HTTP_LOCALHOST_8881);
        URL url = null;
        try{
            url = new URL(urlStr);
            urlContent.setUrlString(urlStr);
        }
        catch(MalformedURLException | NullPointerException e){
            LOGGER.error(e.getMessage(), e);
        }
        try{
            URLConnection urlConnection = Objects.requireNonNull(url).openConnection();
            urlConnection.connect();
            String msg = urlConnection.getPermission().getName() + " " + urlConnection.getPermission().getActions();
            urlContent.setUrlPermissions(msg);
            urlContent.setContentType(urlConnection.getContentType());
            urlContent.setContentObj(urlConnection.getContent());
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }

    }

}