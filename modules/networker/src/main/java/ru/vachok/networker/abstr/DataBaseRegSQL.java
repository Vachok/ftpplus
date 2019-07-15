package ru.vachok.networker.abstr;


/**
 @since 10.04.2019 (0:27)
 @deprecated since 15.07.2019 (10:22)
 @see ru.vachok.networker.restapi.DataConnectTo
 */
public interface DataBaseRegSQL {
    
    
    int selectFrom();

    int insertTo();

    int deleteFrom();

    int updateTable();
}
