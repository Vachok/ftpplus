package ru.vachok.networker.ad.usermanagement;


import ru.vachok.networker.componentsrepo.exceptions.TODOException;

import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.util.StringJoiner;


/**
 Class ru.vachok.networker.ad.usermanagement.UserACLFromFileParser
 <p>

 @since 28.04.2020 (12:33) */
public class UserACLFromFileParser implements UserACLManager {


    private Object classOption;

    UserACLFromFileParser(Path path) {
        this.classOption = path;
    }

    @Override
    public String addAccess(UserPrincipal newUser) {
        throw new TODOException("ru.vachok.networker.ad.usermanagement.UserACLFromFileParser.addAccess( String ) at 28.04.2020 - (12:33)");
    }

    @Override
    public String removeAccess(UserPrincipal oldUser) {
        throw new TODOException("ru.vachok.networker.ad.usermanagement.UserACLFromFileParser.removeAccess( String ) at 28.04.2020 - (12:33)");
    }

    @Override
    public String replaceUsers(UserPrincipal oldUser, UserPrincipal newUser) {
        throw new TODOException("ru.vachok.networker.ad.usermanagement.UserACLFromFileParser.replaceUsers( String ) at 28.04.2020 - (12:33)");
    }

    @Override
    public void setClassOption(Object classOption) {
        this.classOption = classOption;
    }

    @Override
    public String getResult() {
        if (classOption instanceof String) {
            return parseFromString();
        }
        else {
            throw new TODOException("28.04.2020 (12:35)");
        }
    }

    private String parseFromString() {
        StringBuilder stringBuilder = new StringBuilder();
        String[] aclS = classOption.toString().split("\\QEATMEAT\\");
        for (String acl : aclS) {
            if (!acl.contains("001.")) {
                stringBuilder.append(acl.split("\\Q:")[0]).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", UserACLFromFileParser.class.getSimpleName() + "[\n", "\n]")
            .add("classOption = " + classOption)
            .toString();
    }
}