package com.redis.test;

import com.redis.utils.RedisPol;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @date 2020/1/7
 */
public class RedisRunnable implements Runnable {

    private Jedis jedis = RedisPol.getJedis();
    private int USER_ID;

    public RedisRunnable(int userId){
        this.USER_ID = userId;
    }
    @Override
    public void run() {

        try {
            String watch = jedis.watch(RedisSeckill.WATCH_KEY);
            int goos = Integer.valueOf(jedis.get(RedisSeckill.WATCH_KEY));//获取剩余商品数量
            if (goos>0){//还有剩余商品
                Transaction tr = jedis.multi();//开启事务
                tr.decr(RedisSeckill.WATCH_KEY);//商品总数减一
                List<Object> result = tr.exec();//执行事务
                //当result为null时，说明抢购失败
                if (result==null||result.isEmpty()){
                    String msg = "用户--"+USER_ID+"抢购失败！-->剩余商品数量:"+jedis.get(RedisSeckill.WATCH_KEY);
                    System.out.println(msg);
                    jedis.setnx("failmsg",msg);
                }else {//执行事务
                    for (Object re: result) {
                        String msg = "--->用户"+USER_ID+"--抢购成功！，已被抢"+(10-Integer.parseInt(result.get(0).toString()))+"--剩余："+re.toString();
                        System.out.println(msg);
                        jedis.setnx("smsg",msg);
                    }
                }

            }else{
                String over = "ocer--->"+USER_ID;
                System.out.println(over);
                jedis.setnx("overmsg",over);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            jedis.close();
        }
    }
}
