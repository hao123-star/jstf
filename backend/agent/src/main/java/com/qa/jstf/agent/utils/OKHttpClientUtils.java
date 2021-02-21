package com.qa.jstf.agent.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;

@Component
public class OKHttpClientUtils {

    @Autowired
    OkHttpClient okHttpClient;

    public Response get(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request).execute();
    }

    public Response postJson(String url, Map<String, Object> jsonData) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(jsonData);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return okHttpClient.newCall(request).execute();
    }

    public Response postForm(String url, Map<String, String> formData) throws IOException {

        Request.Builder requestBuilder = new Request.Builder()
                .url(url);

        if (null != formData) {
            FormBody.Builder builder = new FormBody.Builder();

            formData.forEach((k, v) -> {
                builder.add(k, v);
            });

            FormBody formBody = builder.build();
            requestBuilder.post(formBody);
        }

        return okHttpClient.newCall(requestBuilder.build()).execute();
    }
}
