package io.lightflame.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import io.lightflame.annotations.Endpoint;
import io.lightflame.annotations.Handler;

/**
 * HTTPHandlers
 */
public class HTTPHandlers {

    private static final Logger LOGGER = Logger.getLogger(HTTPHandlers.class);
    private Map<String, HandlerStore> handleMap = new HashMap<>();

    public HTTPResponse getHandle(HTTPSession session, HTTPRequest request) throws Exception{
        HandlerStore handler = handleMap.get(request.getLocation());
        return handler.getResponse(session, request);
    }

    public void createHandlers(Class<?> mainCLass) throws Exception {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage(mainCLass.getPackageName()))
            .setScanners(new SubTypesScanner(), new MethodAnnotationsScanner(), new TypeAnnotationsScanner())
        );

        System.out.println(mainCLass.getPackageName());
        
        Map<String, Class<?>> mapClazzes = getMapCLazzes(reflections.getTypesAnnotatedWith(Endpoint.class));
        Set<Method> setMethods = reflections.getMethodsAnnotatedWith(Handler.class);
        

        for (Method method : setMethods){
            String url = "";
            Class<?> clazz = mapClazzes.get(method.getDeclaringClass().getName());
            if (clazz != null){
                Endpoint webPath =  clazz.getAnnotation(Endpoint.class);
                url = webPath == null ? "/" : webPath.value();
            }

            Handler webPath = method.getAnnotation(Handler.class);
            url += webPath.value();
            handleMap.put(url, new HandlerStore(method, clazz));
            LOGGER.info("registering url at: " + url);

           
        }
    }

    private static Map<String, Class<?>> getMapCLazzes(Set<Class<?>> setClazzes){
        Map<String, Class<?>> mapClazzes = new HashMap<>();
        for (Class<?> clazz : setClazzes){
            mapClazzes.put(clazz.getName(), clazz);
        }
        return mapClazzes;
    }

}