package test.tool;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {
	public static Logger logger = LoggerFactory.getLogger(RedisClient.class);
	public static String REDIS_NAME_MAIN = "phantom";
	public static String REDIS_NAME_LOCAL = "local";
	public static String REDIS_NAME_ROUTER = "router";
	/**
	 * 存将要实例化的redis client 的配置信息
	 */
	private static Map<String, RedisConfig> redisConfigs = new HashMap<String, RedisConfig>();
	static {
		addRedisConfig(REDIS_NAME_LOCAL, "127.0.0.1", "6379", "foobared", "0");
	}

	public static void addRedisConfig(String redisName, String host, String port, String password, String database) {
		if (!redisConfigs.containsKey(redisName)) {
			RedisConfig rc = new RedisConfig();
			rc.host = host;
			rc.port = Integer.valueOf(port);
			rc.password = password;
			rc.database = Integer.valueOf(database);
			logger.info("ini config  " + redisName);
			redisConfigs.put(redisName, rc);
		}
	}

	public static RedisConfig getRedisConfig(String redisName) {
		return redisConfigs.get(redisName);
	}

	protected JedisPool jedisPool = null;

	public void initJedisPool(RedisConfig redisConfig) {
		if (redisConfig != null) {
			String password = redisConfig.password.equals("") ? null : redisConfig.password;
			this.jedisPool = new JedisPool(getConfig(), redisConfig.host, redisConfig.port, 2000, password,
					redisConfig.database);
			logger.info("init redis " + redisConfig.host + "-" + redisConfig.port + "-" + redisConfig.password);
		} else {
			logger.error("redis config is null, please init redis config!!!");
		}
	}

	protected JedisPoolConfig getConfig() {
		JedisPoolConfig configJedis = new JedisPoolConfig();
		configJedis.setMaxTotal(20);
		configJedis.setMaxWaitMillis(2);
		configJedis.setMaxIdle(8);
		configJedis.setTestOnBorrow(true);
		return configJedis;
	}

	public synchronized Jedis getJedis() {
		Jedis jedis = null;
		try {
			if (jedisPool != null) {
				jedis = jedisPool.getResource();
			}
		} catch (Exception e) {
			logger.error("Get jedis error : " + this.getClass() + e);
		}
		return jedis;
	}

	public void returnResource(final Jedis jedis) {
		if (jedis != null && jedisPool != null) {
			jedisPool.returnResource(jedis);
		}
	}
}
