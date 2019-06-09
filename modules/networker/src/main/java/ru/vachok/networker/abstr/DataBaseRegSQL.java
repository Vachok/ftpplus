package ru.vachok.networker.abstr;


/**
 @since 10.04.2019 (0:27) */
public interface DataBaseRegSQL {
    
    
    int selectFrom();

    int insertTo();

    int deleteFrom();

    int updateTable();
}
