// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.exceptions;


/**
 Class ru.vachok.networker.componentsrepo.exceptions.TODOException
 <p>

 @since 18.07.2019 (19:21) */
public class TODOException extends IllegalStateException {


    private final String whatToDo;

    public TODOException(String whatToDo) {

        this.whatToDo = whatToDo;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "\n\nTODO: " + whatToDo;
    }
}