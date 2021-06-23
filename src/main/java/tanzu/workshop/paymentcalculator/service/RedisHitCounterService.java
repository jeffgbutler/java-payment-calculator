package tanzu.workshop.paymentcalculator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile({"cloud", "kubernetes"})
public class RedisHitCounterService implements HitCounterService {

    private static final String REDIS_KEY = "payment-calculator";
    private static final int DEFAULT_VALUE = 5000;

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    @Override
    public long incrementCounter() {
        redisTemplate.opsForValue().setIfAbsent(REDIS_KEY, DEFAULT_VALUE);
        return redisTemplate.opsForValue().increment(REDIS_KEY);
    }

    @Override
    public void resetCount() {
        redisTemplate.opsForValue().set(REDIS_KEY, DEFAULT_VALUE);
    }
}
