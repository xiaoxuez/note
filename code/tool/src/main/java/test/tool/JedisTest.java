package test.tool;

import java.net.URL;
import java.util.Arrays;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisTest {
	private static JedisPool jedisPool = null;
	private static String host = "115.29.9.156";
	private static int port = 6379;
	private static String password = "dFyF9yygzfDeGZYQZCvJEHjwuejFEFMdrmSrtJzW";

	public static JedisPoolConfig getConfig() {
		JedisPoolConfig configJedis = new JedisPoolConfig();
		;
		configJedis.setMaxTotal(8);
		configJedis.setMaxWaitMillis(2);
		configJedis.setMaxIdle(8);
		configJedis.setTestOnBorrow(true);
		return configJedis;
	}

	public static synchronized void initPool() {
		if (jedisPool == null) {
			jedisPool = new JedisPool(getConfig(), host, port, 2000, password);
//			jedisPool = new JedisPool("redis://114.55.237.238:6379/1");
		}
	}

	/**
	 * 同步获取Jedis实例
	 * 使用完毕之后应该归还给jedisPool
	 * @return Jedis
	 */
	public synchronized static Jedis getJedis() {
		if (jedisPool == null) {
			initPool();
		}
		Jedis jedis = null;
		try {
			if (jedisPool != null) {
				jedis = jedisPool.getResource();
			}
		} catch (Exception e) {
		} finally {
			// returnResource(jedis);
		}
		return jedis;
	}

	public static void returnResource(final Jedis jedis) {
		if (jedis != null && jedisPool != null) {
			jedisPool.returnResource(jedis);
		}
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		JedisPool pool =  new JedisPool("redis://:foobared@114.55.24.147:6379");
		Jedis jedis = pool.getResource();

//		System.out.println(" before adjust : " + jedis.hget("airguru_house:11419", "vent_volumn") + " + " + jedis.hget("airguru_house:11419", "vent_adjust"));
//		jedis.publish("router_online", ": 1}}");
//
//		Thread.sleep(1000);

		System.out.println(" after 1s adjust : " + jedis.hget("airguru_house:11419", "vent_volumn") + " + " + jedis.hget("airguru_house:11419", "vent_adjust"));


//		Thread.sleep(1000 * 60);
//		System.out.println(" after 1m adjust : " + jedis.hget("airguru_house:11419", "vent_volumn") + " + " + jedis.hget("airguru_house:11419", "vent_adjust"));

//		System.out.println(jedis.smembers("production_tcp_connections"));
	}
	
	public static void loadLuaScript() {
		// load redis lua script into script cache
//		URL url = Resources.getResource("SlidingWindowRateLimit.lua");
//		String slidingWindowRateLimitLua = Resources.toString(url, Charsets.UTF_8);
		Jedis jedis = jedisPool.getResource();
		String sha1 = jedis.scriptLoad("string");
		jedis.eval(sha1, Arrays.asList("key1", "key2"), Arrays.asList("key1", "key2"));
		jedisPool.returnResource(jedis);
	}
}
