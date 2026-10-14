package org.jeecg.modules.youcai.util;

import org.jeecg.modules.youcai.config.QWeatherConfig;
import org.jeecg.modules.youcai.config.WebClientConfig;
import org.jeecg.modules.youcai.entity.weatherEntity.QWeatherResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class QWeatherApiUtilTest {
    private static final String FORECAST = "{\"code\":\"200\",\"daily\":[{\"fxDate\":\"2026-10-14\","
            + "\"windSpeedDay\":\"36\",\"precip\":\"2.5\"}]}";

    @Test
    void decompressesGzipBeforeJsonDecoding() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
            gzip.write(FORECAST.getBytes(StandardCharsets.UTF_8));
        }
        DisposableServer server = HttpServer.create().host("127.0.0.1").port(0)
                .handle((request, response) -> response
                        .header("Content-Type", "application/json")
                        .header("Content-Encoding", "gzip")
                        .sendByteArray(Mono.just(bytes.toByteArray())))
                .bindNow();
        try {
            QWeatherResponse forecast = new WebClientConfig().webClientBuilder().build().get()
                    .uri("http://127.0.0.1:" + server.port())
                    .retrieve().bodyToMono(QWeatherResponse.class).block(Duration.ofSeconds(5));
            assertNotNull(forecast);
            assertEquals("200", forecast.getCode());
            assertEquals(1, forecast.getDaily().size());
        } finally {
            server.disposeNow();
        }
    }

    @Test
    void doesNotRetryInvalidJson() {
        AtomicInteger requests = new AtomicInteger();
        QWeatherApiUtil util = util(requests, HttpStatus.OK, "invalid JSON");
        assertThrows(RuntimeException.class, () -> util.get7DayWeatherForecast("112,31").block());
        assertEquals(1, requests.get());
    }

    @Test
    void doesNotRetryAuthenticationFailure() {
        AtomicInteger requests = new AtomicInteger();
        QWeatherApiUtil util = util(requests, HttpStatus.UNAUTHORIZED, "{}");
        assertThrows(WebClientResponseException.class,
                () -> util.get7DayWeatherForecast("112,31").block());
        assertEquals(1, requests.get());
    }

    @Test
    void retriesServerFailureAndConvertsForecast() {
        AtomicInteger requests = new AtomicInteger();
        QWeatherApiUtil util = new QWeatherApiUtil(WebClient.builder().exchangeFunction(request -> {
            boolean first = requests.incrementAndGet() == 1;
            return Mono.just(ClientResponse.create(first ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.OK)
                    .header("Content-Type", "application/json").body(first ? "{}" : FORECAST).build());
        }), config());
        var result = util.get7DayWeatherForecast("112,31").block();
        assertNotNull(result);
        assertEquals(2, requests.get());
        assertEquals(10.0, result.get(0).getWindSpeed());
        assertEquals(2.5, result.get(0).getRainfall());
    }

    @Test
    void boundsTotalRetryTime() {
        QWeatherConfig config = config();
        config.setTimeout(100);
        config.setRetryInterval(1000);
        AtomicInteger requests = new AtomicInteger();
        QWeatherApiUtil util = new QWeatherApiUtil(WebClient.builder().exchangeFunction(request -> {
            requests.incrementAndGet();
            return Mono.just(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE).build());
        }), config);
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> util.get7DayWeatherForecast("112,31").block(Duration.ofSeconds(3)));
        assertInstanceOf(TimeoutException.class, error.getCause());
        assertEquals(1, requests.get());
    }

    private QWeatherApiUtil util(AtomicInteger requests, HttpStatus status, String body) {
        return new QWeatherApiUtil(WebClient.builder().exchangeFunction(request -> {
            requests.incrementAndGet();
            return Mono.just(ClientResponse.create(status).header("Content-Type", "application/json")
                    .body(body).build());
        }), config());
    }

    private QWeatherConfig config() {
        QWeatherConfig config = new QWeatherConfig();
        config.setHost("weather.test");
        config.setApiKey("test-key");
        config.setRetryInterval(10);
        return config;
    }
}
