package storm;

import java.util.Map;

import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;

public class OutputFunction implements Function {

	@Override
	public void prepare(Map conf, TridentOperationContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {

	}

	@Override
	public void execute(TridentTuple tuple, TridentCollector collector) {
		String content = tuple.getString(0);
		System.out.println(content);
	}

}
