package com.djira.ProyectoDjira.Handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

import com.djira.ProyectoDjira.Handler.JsonArg;

public class JsonPathArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String JSONBODYATTRIBUTE = "JSON_REQUEST_BODY";

    private ObjectMapper om = new ObjectMapper();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(JsonArg.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String body = getRequestBody(webRequest);
        if (body.isEmpty()) {
            throw new HttpMessageNotReadableException("Required request body is missing: " + parameter.getMethod().toGenericString());
        }

        String arg = parameter.getParameterAnnotation(JsonArg.class).value();
        if (arg.isEmpty()) {
            arg = parameter.getParameterName();
        }
        JsonNode rootNode = om.readTree(body);
        JsonNode node = rootNode.path(arg);

        boolean required = parameter.getParameterAnnotation(JsonArg.class).required();
        if (required && node instanceof MissingNode) {
            throw new MissingServletRequestParameterException(arg, parameter.getNestedParameterType().getSimpleName());
        } else if (node instanceof MissingNode) {
            return null;
        }

        return om.readValue(node.toString(), parameter.getParameterType());
    }


    private String getRequestBody(NativeWebRequest webRequest) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        String jsonBody = (String) webRequest.getAttribute(JSONBODYATTRIBUTE, NativeWebRequest.SCOPE_REQUEST);
        if (jsonBody == null) {
            try {
                jsonBody = IOUtils.toString(servletRequest.getInputStream());
                webRequest.setAttribute(JSONBODYATTRIBUTE, jsonBody, NativeWebRequest.SCOPE_REQUEST);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return jsonBody;

    }

}

