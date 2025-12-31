package com.photon.common.sdk;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.Base64;

public class ReportingInterceptor implements Interceptor {
    private final String reportingJson;

    public ReportingInterceptor(String reportingJson) { this.reportingJson = reportingJson; }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request().newBuilder()
                .header("X-Reporting-Params", Base64.getEncoder().encodeToString(reportingJson.getBytes()))
                .build();
        return chain.proceed(req);
    }
}