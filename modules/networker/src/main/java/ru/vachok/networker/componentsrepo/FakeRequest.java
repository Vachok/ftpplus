package ru.vachok.networker.componentsrepo;


import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;


public class FakeRequest implements HttpServletRequest {


    public static final String SUN_JAVA_COMMAND = "sun.java.command";

    @Override
    public String getAuthType() {
        throw new TODOException("null");
    }

    @Override
    public Cookie[] getCookies() {
        throw new TODOException("new javax.servlet.http.Cookie[0]");
    }

    @Override
    public long getDateHeader(String name) {
        throw new TODOException("0");
    }

    @Override
    public String getHeader(String name) {
        throw new TODOException("null");
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        throw new TODOException("null");
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        throw new TODOException("null");
    }

    @Override
    public int getIntHeader(String name) {
        throw new TODOException("0");
    }

    @Override
    public String getMethod() {
        throw new TODOException("null");
    }

    @Override
    public String getPathInfo() {
        throw new TODOException("null");
    }

    @Override
    public String getPathTranslated() {
        throw new TODOException("null");
    }

    @Override
    public String getContextPath() {
        return System.getProperty("sun.desktop", "");
    }

    @Override
    public String getQueryString() {
        return System.getProperty("java.runtime.version", "");
    }

    @Override
    public String getRemoteUser() {
        return System.getProperty("user.name", "");
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new TODOException("false");
    }

    @Override
    public Principal getUserPrincipal() {
        throw new TODOException("null");
    }

    @Override
    public String getRequestedSessionId() {
        return System.getProperty("java.vm.specification.version", String.valueOf(new Random().nextDouble()));
    }

    @Override
    public String getRequestURI() {
        return System.getProperty(SUN_JAVA_COMMAND, "");
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer stringBuffer = new StringBuffer();
        return stringBuffer.append(System.getProperty(SUN_JAVA_COMMAND, ""));
    }

    @Override
    public String getServletPath() {
        throw new TODOException("null");
    }

    @Override
    public HttpSession getSession(boolean create) {
        throw new TODOException("HttpSession");
    }

    @Override
    public HttpSession getSession() {
        return new FakeSession();
    }

    @Override
    public String changeSessionId() {
        throw new TODOException("null");
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new TODOException(ConstantsFor.STR_FALSE);
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new TODOException(ConstantsFor.STR_FALSE);
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new TODOException(ConstantsFor.STR_FALSE);
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new TODOException(ConstantsFor.STR_FALSE);
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return true;
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new TODOException("just do it!");
    }

    @Override
    public void logout() throws ServletException {
        throw new TODOException("just do it!");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new TODOException("null");
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new TODOException("null");
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass) throws IOException, ServletException {
        throw new TODOException("null");
    }

    @Override
    public Object getAttribute(String name) {
        throw new TODOException("null");
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new TODOException("null");
    }

    @Override
    public String getCharacterEncoding() {
        return System.getProperty(PropertiesNames.ENCODING);
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        throw new TODOException("just do it!");
    }

    @Override
    public int getContentLength() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getContentLengthLong() {
        return Long.MAX_VALUE;
    }

    @Override
    public String getContentType() {
        return "Only static contents";
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new TODOException("null");
    }

    @Override
    public String getParameter(String name) {
        throw new TODOException("null");
    }

    @Override
    public Enumeration<String> getParameterNames() {
        throw new TODOException("null");
    }

    @Override
    public String[] getParameterValues(String name) {
        throw new TODOException("new java.lang.String[0]");
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        throw new TODOException("null");
    }

    @Override
    public String getProtocol() {
        return "fuck";
    }

    @Override
    public String getScheme() {
        throw new TODOException("null");
    }

    @Override
    public String getServerName() {
        return "Local FAKE";
    }

    @Override
    public int getServerPort() {
        return 65535;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new TODOException("null");
    }

    @Override
    public String getRemoteAddr() {
        return "127.0.0.1";
    }

    @Override
    public String getRemoteHost() {
        return UsefulUtilities.thisPC();
    }

    @Override
    public void setAttribute(String name, Object o) {
        throw new TODOException("just do it!");
    }

    @Override
    public void removeAttribute(String name) {
        throw new TODOException("just do it!");
    }

    @Override
    public Locale getLocale() {
        return Locale.forLanguageTag("ru-RU");
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new TODOException("null");
    }

    @Override
    public boolean isSecure() {
        throw new TODOException(ConstantsFor.STR_FALSE);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new TODOException("null");
    }

    @Override
    public String getRealPath(String path) {
        throw new TODOException("null");
    }

    @Override
    public int getRemotePort() {
        throw new TODOException("0");
    }

    @Override
    public String getLocalName() {
        return UsefulUtilities.getOS();
    }

    @Override
    public String getLocalAddr() {
        return UsefulUtilities.getUpTime();
    }

    @Override
    public int getLocalPort() {
        throw new TODOException("0");
    }

    @Override
    public ServletContext getServletContext() {
        throw new TODOException("null");
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new TODOException("null");
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new TODOException("null");
    }

    @Override
    public boolean isAsyncStarted() {
        throw new TODOException("false");
    }

    @Override
    public boolean isAsyncSupported() {
        throw new TODOException("false");
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new TODOException("null");
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new TODOException("null");
    }

    private class FakeSession implements HttpSession {


        @Override
        public long getCreationTime() {
            return UsefulUtilities.getMyTime();

        }

        @Override
        public String getId() {
            return new FakeRequest().getRequestedSessionId();

        }

        @Override
        public long getLastAccessedTime() {
            return System.currentTimeMillis();

        }

        @Override
        public ServletContext getServletContext() {
            throw new TODOException("null");

        }

        @Override
        public void setMaxInactiveInterval(int interval) {
            throw new TODOException("just do it!");
        }

        @Override
        public int getMaxInactiveInterval() {
            return Integer.parseInt(System.getProperty("java.specification.version", "2019"));

        }

        @Override
        public HttpSessionContext getSessionContext() {
            throw new TODOException("null");

        }

        @Override
        public Object getAttribute(String name) {
            throw new TODOException("null");

        }

        @Override
        public Object getValue(String name) {
            throw new TODOException("null");

        }

        @Override
        public Enumeration<String> getAttributeNames() {
            throw new TODOException("null");

        }

        @Override
        public String[] getValueNames() {
            throw new TODOException("new java.lang.String[0]");

        }

        @Override
        public void setAttribute(String name, Object value) {
            throw new TODOException("just do it!");
        }

        @Override
        public void putValue(String name, Object value) {
            throw new TODOException("just do it!");
        }

        @Override
        public void removeAttribute(String name) {
            throw new TODOException("just do it!");
        }

        @Override
        public void removeValue(String name) {
            throw new TODOException("just do it!");
        }

        @Override
        public void invalidate() {
            throw new TODOException("just do it!");
        }

        @Override
        public boolean isNew() {
            throw new TODOException("false");

        }
    }
}
