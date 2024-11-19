package com.samsung.ciam.common.core.component;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * 1. FileName	: SlackAppender.java
 * 2. Package	: com.samsung.ciam.common.core.component
 * 3. Comments	: Logback을 사용하여 특정 수준 이상의 로그를 Slack으로 전송하는 커스텀 Appender
 * 4. Author	: 서정환
 * 5. DateTime	: 2024. 11. 04.
 * 6. History	:
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * Date		 |	Name			|	Comment
 * <p>
 * -------------  -----------------   ------------------------------
 * <p>
 * 2024. 11. 04.		 | 서정환			|	최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Slf4j
public class SlackAppender extends AppenderBase<ILoggingEvent> {

    private String webhookUrl;

    /*
     * 1. 메소드명: append
     * 2. 클래스명: SlackAppender
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    로그 이벤트가 발생할 때 특정 수준 이상의 로그 메시지를 Slack Webhook을 통해 전송
     * 2. 사용법
     *    Logback 설정 파일에서 SlackAppender를 등록하여 사용
     * </PRE>
     * @param event 발생한 로그 이벤트
     */
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

    /*
     * 1. 메소드명: setWebhookUrl
     * 2. 클래스명: SlackAppender
     * 3. 작성자명: 서정환
     * 4. 작성일자: 2024. 11. 04.
     */
    /**
     * <PRE>
     * 1. 설명
     *    Slack Webhook URL 설정 메소드
     * 2. 사용법
     *    Logback 설정 파일에서 webhookUrl 설정을 통해 Slack Webhook URL 지정
     * </PRE>
     * @param webhookUrl Slack Webhook URL
     */
    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}