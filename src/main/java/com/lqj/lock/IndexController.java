package com.lqj.lock;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author luqianjiang
 * @Date 2023/2/26 21:14
 * @Description:
 */
@RestController
public class IndexController {

    @Resource
    private Redisson redisson;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/deduct_stock")
    public String deductStock() {
        String lockKey = "product001";
        String clientID = UUID.randomUUID().toString();
        try{
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, clientID,10,TimeUnit.SECONDS);// jedis.setnx(key, value);

            if(!result){
                return "error";
            }
            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock")); // jedis.get("stock");
            if (stock > 0) {
                int realStock = stock - 1;
                stringRedisTemplate.opsForValue().set("stock", realStock + ""); // jedis.set(key, value);
                System.out.println("扣减成功，剩余库存：" + realStock);
            } else {
                System.out.println("扣减失败，库存不足！");
            }
        }finally {
            if (clientID.equals(stringRedisTemplate.opsForValue().get(lockKey))) {
                // 释放锁
                stringRedisTemplate.delete(lockKey);
            }
        }

        return "end";
    }
}
