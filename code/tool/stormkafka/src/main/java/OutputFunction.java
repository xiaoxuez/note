import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;

import java.util.Iterator;
import java.util.Map;

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
		Iterator iterator = tuple.iterator();
		while (iterator.hasNext()) {
			Object object= iterator.next();
			System.out.print(object + " ");
		}
		System.out.println();


	}

}
