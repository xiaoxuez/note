package test.tool;

import java.util.Map;

import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;

import redis.clients.jedis.Jedis;

public class RedisTestFunction implements Function {
	
	private long id;

	@Override
	public void prepare(Map conf, TridentOperationContext context) {
		id = context.getPartitionIndex();
	}

	@Override
	public void cleanup() {

	}

	@Override
	public void execute(TridentTuple tuple, TridentCollector collector) {
		System.out.println("   par = " + id);
		for (int i = 0; i < 50; i++) {
			getRedis();
		}
		
	}
	
	public void getRedis() {
		LocalRedisClient client = LocalRedisClient.getInstance();
		Jedis jedis = client.getJedis();
		System.out.println("  jedis == null: " + (jedis == null));
		jedis.get("s");
		client.returnResource(jedis);
	}

}
