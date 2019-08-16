// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import ru.vachok.networker.Keeper;


/**
 @since 19.05.2019 (23:04) */
public interface Stats extends Keeper {
    
    
    String getPCStats();
    
    String getInetStats();
}
