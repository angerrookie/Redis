package com.redis.test;

import com.redis.utils.RedisPol;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Administrator
 * @description 基于redis的秒杀
 * @date 2020/1/7
 */
public class RedisSeckill {

    private  static final int MAX_THREADS = 5;//最大并发数
    protected static final String WATCH_KEY = "Goods";//监视WATCH_KEY，一旦发生改变  中断事务
    private static final int GOOS_NUM = 10;//商品数量
    private static final int USER_NUM = 100;//用户数量

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);//创建线程池，模拟多用户同时抢购
        Jedis jedis = RedisPol.getJedis();
        jedis.set(WATCH_KEY,String.valueOf(GOOS_NUM));
        jedis.close();
        for (int i=0;i<USER_NUM;i++){
            executorService.execute(new RedisRunnable(i));
        }
        executorService.shutdown();
    }
}
