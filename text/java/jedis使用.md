## Jedis使用的一些事项


+ 线程池的使用，config示例

	```
	 private JedisPoolConfig getConfig() {
        JedisPoolConfig configJedis = new JedisPoolConfig();
        configJedis.setMaxTotal(100);
        configJedis.setMaxWaitMillis(2000);
        configJedis.setMaxIdle(20);
        configJedis.setTestOnBorrow(true);
        return configJedis;
    }
	```
	
+ getResouce为null,在添加了config的前提还是会偶尔get到null的jedis,仅仅是出现少数时候，可加上多次get的情况

```
 public synchronized Jedis getJedis() {
        Jedis jedis = null;
        int x = 0;
        do {
            try {
                x ++;
                if (jedisPool != null) {
                    jedis = jedisPool.getResource();
                } else {
                    LoggerUtils.logErr(redisConfig.name + "-" + redisConfig.host + ":  Get Jedis Error: jedis pool is null!");
                }
            } catch (Exception e) {
                LoggerUtils.logErr(redisConfig.name + " : " + "Get jedis error in catch: " + this.getClass() + e);
            }
        } while (x < 4 && jedis == null);
        if (x == 4) {
            LoggerUtils.logErr(redisConfig.name + "-" + redisConfig.host + ": Can't get useable jedis !");
        }
        return jedis;
    }
```
