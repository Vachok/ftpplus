package ru.vachok.networker.logic;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class GoogleCred {

   GoogleCredential credential = new GoogleCredential();

   public Stream<String> getCred() {
      List<String> list = new ArrayList<>();
      String serviceAccountUser = credential.getServiceAccountUser();
      list.add(serviceAccountUser);
      return list.parallelStream();
   }
}
