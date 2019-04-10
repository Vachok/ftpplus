package ru.vachok.networker.abstr;


import ru.vachok.mysqlandprops.DataConnectTo;


/**
 @since 10.04.2019 (0:27) */
public interface DataBaseRegSQL extends DataConnectTo {
    
    
    int selectFrom();

    int insertTo();

    int deleteFrom();

    int updateTable();
}
