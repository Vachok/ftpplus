package ru.vachok.ostpst.utils;


import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Set;


/**
 @since 14.05.2019 (10:17) */
public class TForms {
    
    
    public String fromArray(Exception e) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(new Date());
        stringBuilder.append(e.getMessage()).append(" (").append(e.getLocalizedMessage()).append(")").append("\n");
        for (StackTraceElement traceElement : e.getStackTrace()) {
            stringBuilder.append(traceElement).append("\n");
        }
        stringBuilder.append("\n\n");
        if (e.getSuppressed() != null && e.getSuppressed().length > 0) {
            stringBuilder.append(e.getMessage()).append("\n");
            for (StackTraceElement element : e.getStackTrace()) {
                stringBuilder.append(element);
            }
        }
        return stringBuilder.toString();
    }
    
    public String fromArray(Set<?> set) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : set) {
            stringBuilder.append(o).append("\n");
        }
        return stringBuilder.toString();
    }
    
    public String fromArray(Deque<?> deque) {
        StringBuilder stringBuilder = new StringBuilder();
        
        for (Object o : deque) {
            stringBuilder.append(deque.poll());
        }
        
        return stringBuilder.toString();
    }
    
    public String fromArray(List<?> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            stringBuilder.append(list.get(i)).append("\n");
        }
        return stringBuilder.toString();
    }
}
