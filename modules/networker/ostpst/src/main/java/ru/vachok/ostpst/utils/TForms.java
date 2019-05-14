package ru.vachok.ostpst.utils;


/**
 @since 14.05.2019 (10:17) */
public class TForms {
    
    
    public String fromArray(Exception e) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(e.getMessage()).append(" (").append(e.getLocalizedMessage()).append(")").append("\n");
        for (StackTraceElement traceElement : e.getStackTrace()) {
            stringBuilder.append(traceElement);
        }
        stringBuilder.append("\n");
        if (e.getSuppressed() != null && e.getSuppressed().length > 0) {
            stringBuilder.append(e.getMessage()).append("\n");
            for (StackTraceElement element : e.getStackTrace()) {
                stringBuilder.append(element);
            }
        }
        return stringBuilder.toString();
    }
    
}
