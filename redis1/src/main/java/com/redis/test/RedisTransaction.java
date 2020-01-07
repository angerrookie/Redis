package com.redis.test;

import com.redis.utils.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 事务
 * @date 2020/1/7
 */
public class RedisTransaction {
    private  static Jedis jedis = RedisConfig.getJedis();

    public static void main(String[] args) {

//        test();
//        test1();
//        test2();
//        test3();
        test4();
    }
    /**
     * 功能描述: 正常执行事务
     * @Param: []
     * @Return: void
     * @Author: Administrator
     * @Date: 2020/1/7 9:11
     */
    public static void test(){
        Transaction tr = jedis.multi();
        tr.lpush("user","zhangsan");
        tr.lpush("user","lisi");
        tr.lpush("user","wangwu");
        List<Object> exec = tr.exec();
        System.out.println("--->"+exec);
//        System.out.println(jedis.lrange("user",0,jedis.llen("user")));
        while (jedis.llen("user")>0){
            System.out.print("--"+jedis.lpop("user")+"--");
        }
    }
    /**
     * 功能描述: 事务过程中抛出异常，事务中断,命令都不生效
     * @Param: []
     * @Return: void
     * @Author: Administrator
     * @Date: 2020/1/7 9:18
     */
    public static void test1(){
        try {
            Transaction tr = jedis.multi();
            tr.lpush("user","zhangsan");
            tr.lpush("user","lisi");
            int a = 6/0;
            tr.lindex("user",5);
            tr.lpush("user","wangwu");
            List<Object> exec = tr.exec();
            System.out.println("\n"+"--->"+exec);
            while (jedis.llen("user")>0){
                System.out.print("--"+jedis.lpop("user")+"--");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 功能描述: DISCARD：取消事务，放弃执行事务块内的所有命令。所有操作不生效
     * @Param: []
     * @Return: void
     * @Author: Administrator
     * @Date: 2020/1/7 10:14
     */
    public static void test2(){
        String watch = jedis.watch("user");
        Transaction tr = jedis.multi();
        tr.lpush("user","zhangsan");
        tr.lpush("user","lisi");
        tr.lpush("user","wangwu");
        //取消事务，前面的操作不会生效
        String discard = tr.discard();
        while (jedis.llen("user")>0){
            System.out.print("--"+jedis.lpop("user")+"--");
        }
    }
    /**
     * 功能描述:出现语法错误时，不影响其他命令执行
     * @Param: []
     * @Return: void
     * @Author: Administrator
     * @Date: 2020/1/7 10:35
     */
    public static void test3(){
        Transaction tr = jedis.multi();
        tr.set("phone","iphone");
        tr.set("book","book");

        tr.incr("book");

        tr.set("tree","tree");
        List<Object> exec = tr.exec();
       System.out.println(jedis.get("phone")+"--"+jedis.get("book")+"--"+jedis.get("tree"));
       jedis.del("phone");
       jedis.del("book");
       jedis.del("tree");
    }
    public static void test4(){
        String watch = jedis.watch("book");
        Transaction tr = jedis.multi();
        tr.set("phone","iphone");
        tr.set("book","book");

        tr.set("book","books");

        tr.set("tree","tree");
        //取消事务，前面的操作不会生效
        List<Object> exec = tr.exec();
        System.out.println(jedis.get("phone")+"--"+jedis.get("book")+"--"+jedis.get("tree"));
        jedis.del("phone");
        jedis.del("book");
        jedis.del("tree");
    }
}
