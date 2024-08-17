package com.example.daobe.auth.infrastructure.redis;

import static com.example.daobe.common.exception.redis.RedisExceptionType.DESERIALIZE_ERROR;
import static com.example.daobe.common.exception.redis.RedisExceptionType.SERIALIZE_ERROR;

import com.example.daobe.auth.domain.Token;
import com.example.daobe.auth.domain.repository.TokenRepository;
import com.example.daobe.common.exception.redis.RedisException;
import com.example.daobe.common.utils.DaoStringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisTokenRepository implements TokenRepository {

    private static final Long TTL = 10_080L;  // FIXME: 임시 TTL 10,080 초 (7일)
    private static final String PREFIX = "token";
    private static final String SEPARATOR = ":";

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(Token token) {
        redisTemplate.opsForValue().set(key(token.getTokenId()), serializeToken(token));
        redisTemplate.expire(key(token.getTokenId()), TTL, TimeUnit.MINUTES);
    }

    @Override
    public void deleteByTokenId(String tokenId) {
        redisTemplate.delete(key(tokenId));
    }

    @Override
    public Optional<Token> findByTokenId(String tokenId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(tokenId)))
                .map(this::deserializeToken);
    }

    private String key(String tokenId) {
        return DaoStringUtils.concatenateStrings(PREFIX, SEPARATOR, tokenId);
    }

    private String serializeToken(Token token) {
        try {
            return objectMapper.writeValueAsString(token);
        } catch (JsonProcessingException ex) {
            throw new RedisException(SERIALIZE_ERROR);
        }
    }

    private Token deserializeToken(String tokenJson) {
        try {
            return objectMapper.readValue(tokenJson, Token.class);
        } catch (JsonProcessingException ex) {
            throw new RedisException(DESERIALIZE_ERROR);
        }
    }
}