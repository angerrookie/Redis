package com.redis.test;

import com.redis.utils.RedisConfig;
import com.redis.utils.RedisPol;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

import java.util.*;

/**
 * @author Administrator
 * @description
 * @date 2020/1/6
 */
public class Test {

//    private static Jedis jedis = RedisConfig.getJedis();
    private static Jedis jedis = RedisPol.getJedis();

    public static void main(String[] args) {
//        string类型
//        redisString();
        redisHash();
//        redisList();
//        redisSet();
        jedis.close();
    }
    /**
     * 功能描述: 字符串String
     * @Param: []
     * @Return: void
     * @Author: Administrator
     * @Date: 2020/1/6 14:54
     */
    public static void redisString(){
        //        添加数据 如果已经存在则覆盖
        jedis.set("redis","redisTest");
        System.out.println(jedis.get("redis"));
        jedis.set("redis","redisTest--->");
        System.out.println(jedis.get("redis"));
//        拼接数据
        jedis.append("redis","<-----");
        System.out.println(jedis.get("redis"));
//        删除数据
        jedis.del("redis");
        System.out.println(jedis.get("redis"));
//        批量添加数据
        jedis.mset("name","zhangsan","age","20","sex","nan");
        System.out.println(jedis.get("name")+"--"+jedis.get("age")+"--"+jedis.get("sex"));
//      decr:会将key中数值减一，如果key不存在，则会将key的值预置为0，如果键包含错误类型的值或包含不能表示为整数的字符串，则返回错误
//        将age减一
        jedis.decr("age");
        System.out.println(jedis.get("name")+"--"+jedis.get("age")+"--"+jedis.get("sex"));
//        将width设置为0，然后进行减一
        jedis.decr("tall");
        System.out.println(jedis.get("tall")+"---"+jedis.get("length"));
//        name中值不能表示为整数，返回一个错误：ERR value is not an integer or out of range
//        jedis.decr("name");
//        System.out.println(jedis.get("name"));
    }
    
    /**
     * 功能描述: Redis hash 是一个 string 类型的 field 和 value 的映射表，hash 特别适合用于存储对象。
     * Redis 中每个 hash 可以存储 232 - 1 键值对（40多亿）。
     * @Param: []
     * @Return: void
     * @Author: Administrator
     * @Date: 2020/1/6 14:55
     */
    public static void redisHash(){
        Map<String,String> map = new HashMap<String,String>();
        map.put("name","zhangsan");
        map.put("age","20");
        map.put("address","hangzhou");
        jedis.hmset("user",map);
        //HMGET key field1 [field2]
        //获取所有给定字段的值
        List<String> list = jedis.hmget("user","name","age","address");
        System.out.println("添加:"+list);

        //拼接数据
        jedis.hset("user","hobby","pingpong");
        List<String> list1 = jedis.hmget("user","name","age","address","hobby");
        System.out.println("拼接数据："+list1);
        //HVALS key
        //获取哈希表中所有值
        List<String> list2 = jedis.hvals("user");
        System.out.println(list2);

        //HGETALL key
        //获取在哈希表中指定 key 的所有字段和值
       Map<String,String> map1 = jedis.hgetAll("user");
        for (String s : map1.keySet()) {
            System.out.println(s+"--"+jedis.hget("user",s));
        }
        System.out.println("------------------");
        Set<String> keySet = jedis.hkeys("user");
        for (String s : keySet) {
            System.out.println(s+"--"+jedis.hget("user",s));
        }
        System.out.println("------------------");
        //HKEYS key
        //获取所有哈希表中的字段
        Iterator<String>keys = jedis.hkeys("user").iterator();
        while (keys.hasNext()){
            String field = keys.next();
            System.out.println(field+"--"+jedis.hget("user",field));
        }
        //部分删除数据
        jedis.hdel("user", "hobby");
        System.out.println("删除." + jedis.hmget("user", "name","age","address","hobby"));
        System.out.println("age:" + jedis.hmget("user", "age")); //因为删除了，所以返回的是null
        jedis.rename("user","newuser");
        System.out.println("user的键中存放的值的个数:" + jedis.hlen("user")); //返回key为user的键中存放的值的个数
        System.out.println("是否存在key为user的记录:" + jedis.exists("user"));//是否存在key为user的记录
        //删除整个hash
        jedis.del("user");
        System.out.println("删除后是否存在key为user的记录:" + jedis.exists("user"));//是否存在key为user的记录

    }
    /**
     * 功能描述:  Redis列表是简单的字符串列表，按照插入顺序排序。你可以添加一个元素到列表的头部（左边）或者尾部（右边）
     * 一个列表最多可以包含 232 - 1 个元素 (4294967295, 每个列表超过40亿个元素)。
     * 元素可重复
     * @Param: []
     * @Return: void
     * @Author: Administrator
     * @Date: 2020/1/6 15:59
     */
    public static void redisList(){
        //LPUSH key value1 [value2]
        //将一个或多个值插入到列表头部
        jedis.lpush("book","english","Chinese","Math");
        //LLEN key 获取列表长度  LRANGE key start stop 获取列表指定范围内的元素
        List<String> list = jedis.lrange("book",0,jedis.llen("book"));
        System.out.println("lpush:"+list);
        //LINDEX key index 通过索引获取列表中的元素
       for (long i = 0;i<jedis.llen("book");i++){
           System.out.println(jedis.lindex("book",i)+"--"+jedis.llen("book"));
       }
       //LPUSHX key value 将一个值插入到已存在的列表头部
        jedis.lpushx("book","physics");
        jedis.lpushx("book","physics");
       //LINSERT key BEFORE|AFTER pivot value在列表的元素前或者后插入元素
       jedis.linsert("book", BinaryClient.LIST_POSITION.AFTER ,"physics","biology");
        //LPOP key 移出并获取列表的第一个元素
        System.out.println("-------------");
        while (jedis.llen("book")>0){
            System.out.println(jedis.lpop("book"));
        }
    }
    /**
     * 功能描述: Redis 的 Set 是 String 类型的无序集合。集合成员是唯一的，这就意味着集合中不能出现重复的数据。
     * Redis 中集合是通过哈希表实现的，所以添加，删除，查找的复杂度都是 O(1)。
     * 集合中最大的成员数为 232 - 1 (4294967295, 每个集合可存储40多亿个成员)。
     * @Param: []
     * @Return: void
     * @Author: Administrator
     * @Date: 2020/1/6 15:59
     */
    public static void redisSet(){
        //SADD key member1 [member2]向集合添加一个或多个成员
        jedis.sadd("phone","华为","小米","vivo");
        //SCARD key获取集合的成员数
        //SMEMBERS key返回集合中的所有成员
        System.out.println("成员个数:"+jedis.scard("phone")+"--"+jedis.smembers("phone"));
        jedis.sadd("MI","小米","红米");
        //	SINTER key1 [key2]返回给定所有集合的交集
        System.out.println(jedis.sinter("phone","MI"));
        //	SDIFF key1 [key2]返回给定所有集合的差集
        System.out.println(jedis.sdiff("phone","MI"));
        //SISMEMBER key member判断 member 元素是否是集合 key 的成员
        System.out.println(jedis.sismember("phone","红米"));
    }
}
