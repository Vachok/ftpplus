package ru.vachok.networker.componentsrepo;


import org.jetbrains.annotations.NotNull;

import java.util.*;


public class PassGen {
    
    
    private int numOfChars = 8;
    
    private List<String> userNames = new ArrayList<>();
    
    private char[] chars = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'A', 'B', 'C', 'D', 'E'};
    
    public String generatePasswords() {
        StringBuilder stringBuilder = new StringBuilder();
        
        for (int i = 1; i < 8; i++) {
            userNames.add("storeman" + i);
        }
        
        userNames.forEach(userName->stringBuilder.append(genPass(userName)));
        return stringBuilder.toString();
    }
    
    @NotNull
    private String genPass(String userName) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(userName).append(":");
        for (int i = 1; i < numOfChars; i++) {
            stringBuilder.append(chars[random.nextInt(chars.length)]);
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PassGen{");
        sb.append("userNames=").append(userNames);
        sb.append(", numOfChars=").append(numOfChars);
        sb.append(", chars=").append(Arrays.toString(chars));
        sb.append('}');
        return sb.toString();
    }
}
