Redis安装

1.链接：https://github.com/microsoftarchive/redis/releases

2.下载.msi或者.zip

3.安装或解压：如果是.msi直接点击安装，.zip进行解压不需要安装。

4..msi安装：要勾选Add the Redis installation folder to the PATH environment variable 添加Redis目录到环境变量中。Add an ecxeption to the Windows Firewall  防火墙例外

5.配置：在msi安装目录下或者zip解压文件下找到redis.windows-service.conf文件，用记事本打开，找到含有requirepass字样的地方，追加一行requirepass 密码

6.启动：Windows+R，输入services.msc 打开服务，找到Redis，如果未启动，则手动启动

7.测试：在安装目录下按住Shift键+鼠标右键，选择在此处打开命令窗口  输入redis-cli并回车,如果显示正确端口号，则表示服务正确启动

进行密码验证：auth 密码  如果ok表示验证通过。如果报:ERR Client sent AUTH, but no password is set   ,则需要配置下密码:CONFIG SET requirepass "12345"。如果报ERR invalid password，那就是密码和文件中不一样，重新确定好输入。

8.测试读写：输入 set key "redis test" 回车，用来保存一个键值。输入get key,获取刚才保存的键值

java连接Redis配置
 <!-- https://mvnrepository.com/artifact/redis.clients/jedis -->
    <dependency>
      <groupId>redis.clients</groupId>
      <artifactId>jedis</artifactId>
      <version>2.9.0</version>
    </dependency>
    
    public class RedisConfig {

    private static Jedis jedis;

    public static Jedis getJedis() {
        //连接到本地redis服务器
        jedis = new Jedis("127.0.0.1",6379);
//        验证用户密码
        jedis.auth("12345");
        //查看服务是否运行
        System.out.println("服务正在运行: "+jedis.ping());
        return jedis;
    }
}
JedisPoolConfig参数配置
maxActive：控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted。

maxIdle：控制一个pool最多有多少个状态为idle(空闲)的jedis实例；

whenExhaustedAction：表示当pool中的jedis实例都被allocated完时，pool要采取的操作；默认有三种。
WHEN_EXHAUSTED_FAIL --> 表示无jedis实例时，直接抛出NoSuchElementException；
WHEN_EXHAUSTED_BLOCK --> 则表示阻塞住，或者达到maxWait时抛出JedisConnectionException；
WHEN_EXHAUSTED_GROW --> 则表示新建一个jedis实例，也就说设置的maxActive无用；

maxWait：表示当borrow一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；

testOnBorrow：在borrow一个jedis实例时，是否提前进行alidate操作；如果为true，则得到的jedis实例均是可用的；

testOnReturn：在return给pool时，是否提前进行validate操作；

testWhileIdle：如果为true，表示有一个idle object evitor线程对idle  object进行扫描，如果validate失败，此object会被从pool中drop掉；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义；

timeBetweenEvictionRunsMillis：表示idle object evitor两次扫描之间要sleep的毫秒数；

numTestsPerEvictionRun：表示idle object evitor每次扫描的最多的对象数；

minEvictableIdleTimeMillis：表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义；

softMinEvictableIdleTimeMillis：在minEvictableIdleTimeMillis基础上，加入了至少minIdle个对象已经在pool里面了。如果为-1，evicted不会根据idle   time驱逐任何对象。如果minEvictableIdleTimeMillis>0，则此项设置无意义，且只有在timeBetweenEvictionRunsMillis大于0时才有意义；

lifo：borrowObject返回对象时，是采用DEFAULT_LIFO（last in first out，即类似cache的最频繁使用队列），如果为False，则表示FIFO队列；

其中JedisPoolConfig对一些参数的默认设置如下：
testWhileIdle=true
minEvictableIdleTimeMills=60000
timeBetweenEvictionRunsMillis=30000
numTestsPerEvictionRun=-1
连接池
public class RedisPol {

    private static String host = "127.0.0.1";//服务器ip地址
    private static String passWord = "12345";//redis服务器密码
    private static int port = 6379;//服务器端口号

//    maxActive：控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；如果赋值为-1，则表示不限制；
//    如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted（耗尽）。
    private static Integer MAX_TOTAL  = 8;
//    maxIdle：控制一个pool最多有多少个状态为idle(空闲)的jedis实例；
    private static Integer MAX_IDLE  = 8;
//    maxWait：表示当borrow一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
    private static Integer maxWait = 10000;
    private static Integer TIMEOUT = 10000;
    //在borrow(用)一个jedis实例时，是否提前进行validate(验证)操作；如果为true，则得到的jedis实例均是可用的
    private static Boolean TEST_ON_BORROW = true;

    private  static JedisPool jedisPool  = null;
    //初始化redis连接池
    static {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            //设置最大连接数
            config.setMaxTotal(MAX_TOTAL);
            //设置最大空闲连接个数
            config.setMaxIdle(MAX_IDLE);
            //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
            config.setMaxWaitMillis(maxWait);
            config.setTestOnBorrow(TEST_ON_BORROW);
            jedisPool = new JedisPool(config,host,port,TIMEOUT,passWord);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //获取jedis实例
    public synchronized static Jedis getJedis(){
        try {
            if(jedisPool != null){
                Jedis jedis = jedisPool.getResource();
                return jedis;
            }else{
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

//    private static Jedis jedis = RedisConfig.getJedis();
    private static Jedis jedis = RedisPol.getJedis();

    public static void main(String[] args) {
//        string类型
        redisString();
        redisHash();
        redisList();
        redisSet();
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
