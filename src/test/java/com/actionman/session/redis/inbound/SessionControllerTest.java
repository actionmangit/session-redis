package com.actionman.session.redis.inbound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.actionman.session.redis.RedisApplication;

@ExtendWith(SpringExtension.class)
@ImportAutoConfiguration(classes = {RedisAutoConfiguration.class, SessionAutoConfiguration.class})
@SpringBootTest(classes = RedisApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class SessionControllerTest {
    
    @LocalServerPort
    private int port;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Test
    void 세션확인() {

        String value = "test";

        WebClient client = WebClient.create(getTestUrl());

        SessionDto dto = new SessionDto(value);
        ResponseEntity<String> res = client.post()
            .uri("/session")
            .bodyValue(dto)
            .retrieve()
            .toEntity(String.class)
            .block();

        String sessionCookie = res.getHeaders().get("Set-Cookie").get(0).split(";")[0];

        ResponseEntity<String> res1 = client.get()
            .uri("/session")
            .header("Cookie", sessionCookie)
            .retrieve()
            .toEntity(String.class)
            .block();
        
        assertEquals(res1.getBody(), value);

        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.flushAll();
                return null;
            }
        });

        ResponseEntity<String> res2 = client.get()
            .uri("/session")
            .header("Cookie", sessionCookie)
            .retrieve()
            .toEntity(String.class)
            .block();
        
        assertTrue(StringUtils.isBlank(res2.getBody()));
    }

    private String getTestUrl() {
        return String.format("http://127.0.0.1:%d", port);
    }
}
