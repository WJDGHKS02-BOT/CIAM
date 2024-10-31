package com.samsung.ciam.common.core.component;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SlackAppender extends AppenderBase<ILoggingEvent> {

    private String webhookUrl;

    @Override
    protected void append(ILoggingEvent event) {
        if (event.getLevel().isGreaterOrEqual(ch.qos.logback.classic.Level.ERROR)) {
            try {
                // 에러가 발생한 클래스와 라인번호를 추출
                StackTraceElement[] callerData = event.getCallerData();
                String className = "";
                int lineNumber = -1;

                if (callerData != null && callerData.length > 0) {
                    className = callerData[0].getClassName();  // 호출한 클래스 이름
                    lineNumber = callerData[0].getLineNumber(); // 호출한 라인 번호
                }

                // 로그 메시지 포맷
                String message = String.format("[%s] %s - %s (at %s:%d)",
                        event.getLevel(),
                        event.getLoggerName(),
                        event.getFormattedMessage(),
                        className,
                        lineNumber);

                String payload = String.format("{\"text\": \"%s\"}", message);

                // Slack Webhook에 HTTP 요청 전송
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                HttpClient client = HttpClient.newHttpClient();
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() != 200) {
                                addError("Failed to send log to Slack, status: " + response.statusCode());
                            }
                        });
            } catch (Exception e) {
                addError("Failed to send log to Slack", e);
            }
        }
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}