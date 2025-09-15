package com.interview.web.advice;

import com.interview.web.annotation.LocationHeaderOnPost;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.lang.reflect.Method;
import java.net.URI;

@ControllerAdvice(annotations = Controller.class)
public class LocationHeaderOnPostAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return hasMarker(returnType);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (!(request instanceof ServletServerHttpRequest sshr)) return body;
        HttpServletRequest req = sshr.getServletRequest();


        if (!"POST".equalsIgnoreCase(req.getMethod())) return body;

        HttpHeaders headers = response.getHeaders();
        if (headers.containsKey(HttpHeaders.LOCATION)) return body;

        Object id = extractId(body);
        if (id == null) return body;

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .replaceQuery(null)
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        response.getHeaders().setLocation(location);

        return body;
    }

    private boolean hasMarker(MethodParameter returnType) {
        Method m = returnType.getMethod();
        if (m != null && AnnotationUtils.findAnnotation(m, LocationHeaderOnPost.class) != null) return true;
        Class<?> c = returnType.getContainingClass();
        return AnnotationUtils.findAnnotation(c, LocationHeaderOnPost.class) != null;
    }

    private Object extractId(Object body) {
        if (body == null) return null;
        try {
            return body.getClass().getMethod("getId").invoke(body);
        } catch (Exception ignored) {
        }
        try {
            var f = body.getClass().getDeclaredField("id");
            f.setAccessible(true);
            return f.get(body);
        } catch (Exception ex) {
        }
        return null;
    }
}
