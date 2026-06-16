package com.rapidx.aggregator.service;

import com.rapidx.aggregator.client.OAuthTokenCache;
import com.rapidx.aggregator.exception.UcsApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class ApiServiceImpl implements ApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiServiceImpl.class);

    private final WebClient webClient;
    private final OAuthTokenCache oAuthTokenCache;

    @Value("${oim.sync.bypass:false}")
    private boolean byPassOimSync;

    @Value("${api.retries.max:3}")
    private long apiRetriesMax;

    @Value("${api.retries.delay:2}")
    private long apiRetriesDelay;

    public ApiServiceImpl(WebClient.Builder webClientBuilder, OAuthTokenCache oAuthTokenCache) {
        this.webClient = webClientBuilder.build();
        this.oAuthTokenCache = oAuthTokenCache;
    }

    @Override
    @Async("oimDataSyncThreadpool")
    public <T> void get(String url, Object object, String operation, Class<T> responseType) {
        if (!byPassOimSync) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread Interrupted exception " + e);
            }
            LOGGER.info("ORG API: OIM Sync Get service call started... for URL-" + url + " Primary Key-" + object + " Opreation-" + operation + " Thread name " + Thread.currentThread().getName());

            webClient.get()
                    .uri(url)
                    .headers(h -> h.add("Authorization", oAuthTokenCache.getOAuthAccessToken()))
                    .exchangeToMono(clientResponse -> {
                        LOGGER.info("Recived response from OIM Sync application with status code " + clientResponse.statusCode().value());
                        if (clientResponse.statusCode().is4xxClientError()) {
                            throw new UcsApiException(HttpStatus.valueOf(clientResponse.statusCode().value()), "Org API :: URL is wrong ");
                        } else if (clientResponse.statusCode().is5xxServerError()) {
                            throw new UcsApiException(HttpStatus.valueOf(clientResponse.statusCode().value()), " Org API :: Error occured in OIM Sync :: for more details check OIM sync logs");
                        } else {
                            return clientResponse.bodyToMono(responseType);
                        }
                    })
                    .retryWhen(Retry.backoff(apiRetriesMax, Duration.ofSeconds(apiRetriesDelay))
                            .jitter(0d)
                            .doAfterRetry(retrySignal -> LOGGER.info("Retried " + retrySignal.totalRetries()))
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new UcsApiException(HttpStatus.valueOf(500),
                                    " Retries exhausted ", retrySignal.failure())))
                    .doOnSuccess(clientResponse -> LOGGER.info("Received reply from " + url + " Primary Key-" + object + " Opreation-" + operation + " Thread name " + Thread.currentThread().getName() + " with response " + clientResponse))
                    .subscribe();
        }
    }
}
