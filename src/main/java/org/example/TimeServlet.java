package org.example;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;
    private static final String PARAMETER_NAME = "timezone";
    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss z";


    @Override
    public void init() {
        engine = new TemplateEngine();

        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix("/WEB-INF/templates");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DateTimeFormatter formatter;
        String formattedTime;
        String preparedTimezone = "";

        String timezone = getTimeZoneValue(request).get("name");
        System.out.println(timezone);
        ZoneId zoneId = ZoneId.of(timezone);
        String hourDigit = getTimeZoneValue(request).get("offset");
        String sign = getTimeZoneValue(request).get("sign");
        long offset = Long.parseLong(hourDigit);


        if (sign.equals("+")) {
            formatter = DateTimeFormatter.ofPattern(PATTERN + sign + offset);
            formattedTime = makeDateTime(zoneId).plusHours(offset).format(formatter);
            preparedTimezone = timezone + sign + offset;
        } else if (sign.equals("-")) {
            formatter = DateTimeFormatter.ofPattern(PATTERN + sign + offset);
            formattedTime = makeDateTime(zoneId).minusHours(offset).format(formatter);
        } else {
            formatter = DateTimeFormatter.ofPattern(PATTERN);
            formattedTime = makeDateTime(zoneId).format(formatter);
            preparedTimezone = timezone;
        }

        response.setContentType("text/html; charset=utf-8");
        response.addCookie(new Cookie("lastTimezone", preparedTimezone));
        Context simpleContext = new Context(request.getLocale(), Map.of("time", formattedTime));
        engine.process("timeTemplate", simpleContext, response.getWriter());
        response.getWriter().close();
    }

    public static Map<String, String> getTimeZoneValue(HttpServletRequest request){
        String queryString = request.getQueryString();
        String value = request.getParameter(PARAMETER_NAME);
        Map<String, String> map = new HashMap<>();

        if (queryString != null) {
            if (queryString.contains("+")) {
                map.put("name", value.split(" ")[0]);
                map.put("sign", "+");
                map.put("offset", value.split(" ")[1]);
            } else if (queryString.contains("-")) {
                int minusIndex = value.indexOf("-");
                map.put("name", value.substring(0, minusIndex));
                map.put("sign", "-");
                map.put("offset", value.substring(minusIndex+1));
            } else {
                map.put("name", value);
                map.put("sign", "");
                map.put("offset", "0");
            }

        }else if (!parseCookies(request).isEmpty()){
            return parseCookies(request);
        }else{
            map.put("name", "UTC");
            map.put("sign", "");
            map.put("offset", "0");
        }
        return map;
    }

    public static Map<String, String> parseCookies(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookiesMap = new HashMap<>();
        Map<String, String> result = new HashMap<>();
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            String value = cookie.getValue();
            cookiesMap.put(name, value);
        }
        String cookie = cookiesMap.getOrDefault("lastTimezone", "");
        if (cookie.contains("+")){
            int index = cookie.indexOf("+");
            result.put("name", cookie.substring(0, index));
            result.put("sign", "+");
            result.put("offset", cookie.substring(index+1));

        }else if (cookie.contains("-")){
            int index = cookie.indexOf("-");
            result.put("name", cookie.substring(0, index));
            result.put("sign", "-");
            result.put("offset", cookie.substring(index+1));
        }else {
            result.put("name", cookie);
            result.put("sign", "");
            result.put("offset", "0");
        }
        return result;
    }

    public  static ZonedDateTime makeDateTime (ZoneId zoneId){
        return LocalDateTime.now()
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(zoneId);
    }
}