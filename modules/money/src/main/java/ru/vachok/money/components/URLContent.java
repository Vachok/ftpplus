package ru.vachok.money.components;


import org.springframework.stereotype.Component;


/**
 @since 01.10.2018 (21:52) */
@Component
public class URLContent {

    private String urlPermissions;

    private String contentType;

    private Object contentObj;

    private String urlString;

    public String getUrlPermissions() {
        return urlPermissions;
    }

    public void setUrlPermissions(String urlPermissions) {
        this.urlPermissions = urlPermissions;
    }

    public Object getContentObj() {
        return contentObj;
    }

    public void setContentObj(Object contentObj) {
        this.contentObj = contentObj;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

}