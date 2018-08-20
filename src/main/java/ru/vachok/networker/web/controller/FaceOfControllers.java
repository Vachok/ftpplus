package ru.vachok.networker.web.controller;


import org.thymeleaf.ITemplateEngine;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 <h1>интерфейс контроллеров</h1>

 @since 20.08.2018 (12:06) */
public interface FaceOfControllers {

   void proCess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, ServletContext servletContext, ITemplateEngine iTemplateEngine) throws Exception;
}
