package com.yw.local.task.message.infrastructure.adapter.port;

import com.yw.local.task.message.domain.adapter.port.ILocalMessagePort;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2026/5/10
 */
@Service("httpLocalMessagePort")
@Slf4j
public class HttpLocalMessagePort implements ILocalMessagePort {

    // 建议全局单例
    private final OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    public String notify(LocalTaskMessageEntityCommand event) {
        // 获取 HTTP 配置
        LocalTaskMessageEntityCommand.NotifyConfig.HTTP http =
                event.getNotifyConfig().getHttp();
        try {
            // method
            String method = http.getMethod().toUpperCase();

            // url
            String url = http.getUrl();

            // body json
            String parameterJson = event.getParameterJson();

            // Request Builder
            Request.Builder builder = new Request.Builder()
                    .url(url);

            // headers
            if (http.getHeaders() != null) {
                http.getHeaders().forEach(builder::addHeader);
            }

            // contentType
            String contentType = http.getHeaders() != null
                    ? http.getHeaders().getOrDefault("Content-Type", "application/json")
                    : "application/json";

            // body
            RequestBody body = RequestBody.create(
                    parameterJson == null ? "" : parameterJson,
                    MediaType.parse(contentType)
            );

            // 自动根据 method 选择请求方式
            switch (method) {
                case "GET":
                    builder.get();
                    break;
                case "POST":
                    builder.post(body);
                    break;
                case "PUT":
                    builder.put(body);
                    break;
                case "DELETE":
                    builder.delete(body);
                    break;
                default:
                    throw new RuntimeException("不支持的HTTP Method：" + method);
            }

            // build request
            Request request = builder.build();

            log.info("HTTP通知开始 url:{} method:{} body:{}",
                    url,
                    method,
                    parameterJson);

            // execute
            try (Response response = okHttpClient
                    .newCall(request)
                    .execute()) {

                String result = response.body() != null
                        ? response.body().string()
                        : "";

                log.info("HTTP通知完成 code:{} result:{}",
                        response.code(),
                        result);

                if (!response.isSuccessful()) {
                    throw new RuntimeException(
                            "HTTP通知失败 code:" + response.code()
                    );
                }
                return result;
            }
        } catch (Exception e) {
            log.error("HTTP通知异常", e);
            throw new RuntimeException(e);
        }
    }
}
