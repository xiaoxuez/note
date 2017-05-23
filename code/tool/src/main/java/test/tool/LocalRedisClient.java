package test.tool;

public class LocalRedisClient extends RedisClient {
	
	public static String WEATHER_TABLE = "airguru_weather";
	private static LocalRedisClient instance;

	private LocalRedisClient() {
		this.initJedisPool(RedisClient.getRedisConfig(RedisClient.REDIS_NAME_LOCAL));
	}

	public static synchronized LocalRedisClient getInstance() {
		if (instance == null) {
			instance = new LocalRedisClient();
		}
		return instance;
	}
}
