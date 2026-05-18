package com.yw.local.task.message.domain.service.notify.strategy.impl;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.service.notify.strategy.INotifyStrategy;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP 通知策略。
 */
@Service("httpNotifyStrategy")
public class HttpNotifyStrategy implements INotifyStrategy {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    public String notify(LocalTaskMessageEntityCommand event) throws IOException {
        LocalTaskMessageEntityCommand.NotifyConfig.HTTP http = event.getNotifyConfig() == null ? null : event.getNotifyConfig().getHttp();
        if (http == null || http.getUrl() == null || http.getUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("HTTP 通知配置不能为空");
        }

        String method = http.getMethod() == null
                || http.getMethod().trim().isEmpty()
                ? "POST"
                : http.getMethod().trim().toUpperCase(Locale.ROOT);

        Request.Builder requestBuilder = new Request.Builder().url(http.getUrl());
        requestBuilder.headers(buildHeaders(http.getHeaders()));

        if ("GET".equals(method)) {
            requestBuilder.get();
        } else {
            String body = event.getParameterJson() == null ? "" : event.getParameterJson();
            requestBuilder.method(method, RequestBody.create(body, JSON_MEDIA_TYPE));
        }

        try (Response response = okHttpClient.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP 通知失败，status=" + response.code());
            }
            return response.body() == null ? "" : response.body().string();
        }
    }

    private Headers buildHeaders(Map<String, String> headers) {
        Headers.Builder headersBuilder = new Headers.Builder();
        if (headers == null || headers.isEmpty()) {
            return headersBuilder.build();
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            headersBuilder.add(entry.getKey(), entry.getValue());
        }
        return headersBuilder.build();
    }
}
