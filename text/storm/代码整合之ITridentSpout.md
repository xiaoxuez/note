#代码整合之ITridentSpout

主要示例代码出自第三章。
#### 实现ITridentSpout的类 -- DiagnosisEventSpout
	public class DiagnosisEventSpout implements ITridentSpout<Long>{
		private static final long serialVersionUID = 1L;
		
		BatchCoordinator<Long> coordinator = new DefaultCoordinator();
		
		Emitter<Long> emitter = new DiagnosisEventEmitter();
		public BatchCoordinator<Long> getCoordinator(String txStateId, Map conf,
				TopologyContext context) {
			return coordinator;
		}
	
		public Emitter<Long> getEmitter(String txStateId, Map conf,
				TopologyContext context) {
			return emitter;
		}
	
		public Map getComponentConfiguration() {
			return null;
		}
	
		public Fields getOutputFields() {
			return new Fields("event");
		}
		
		
spout并没有真正发射tuple，只是将这项工作分配给BatchCoordinator和Emitter，Emitter负责发送tuple，BatchCoordinator负责管理批次和元数据，Emitter需要依靠元数据来恰当地进行批次的数据重放，所以实现ITridentSpout接口需要重写的方法是，提供BatchCoordinator和Emitter，并且声明发射的tuple包含的字段。*getComponentConfiguration方法应该配置信息，但见到过的代码暂时都是直接返回null,关于怎么用，有哪些配置信息，暂时就未知了*


#### 实现BatchCoordinator的类 -- DefaultCoordinator
	public static class DefaultCoordinator implements ITridentSpout.BatchCoordinator<Long>, Serializable {
		    private static final long serialVersionUID = 1L;
		    private static final Logger LOG = LoggerFactory.getLogger(DefaultCoordinator.class);
	
		    public Long initializeTransaction(long l, Long aLong, Long x1) {
		        LOG.info("Initializing Transaction [" + l + "]");
		        return null;
		    }
	
		    public void success(long l) {
		        LOG.info("Successful Transaction [" + l + "]");
		    }
	
		    public boolean isReady(long l) {
		        return true;
		    }
	
		    public void close() {
	
		    }
		}
		
		
BatchCoordinator是一个泛型类，这个泛型类是重放一个batch所需要的元数据。本例中spout发送的是随机事件，因此元数据可以忽略。在实际系统中，元数据可能包含组成了这个batch的消息或者对象的标识符，通过这个信息，非透明型和事务型spout可以实现约定，确保batch的内容不出现重复，在事务型spout中，batch的内容不会出现变化，BatchCoordinator类作为一个Storm Bolt运行在一个单线程中，Storm会在ZooKeeper中持久化存储这个元数据，当事务处理完成时会通知到对应的coordinator。*不太明白重放batch所需要的元数据的意义*

	在实际系统中，元数据可能包含组成了这个batch的消息或者对象的标识符，通过这个信息，非透明型和事务型spout可以实现约定，确保batch的内容不出现重复，在事务型spout中，batch的内容不会出现变化
	
*这句话倒过来说也是不太明白的地方，倒过来说就是元数据被忽略了之后，那么当前spout类型会受到影响吗？答案是不会的，因为在第7章中也是忽略了元数据，但一直强调说使用了事务spout, 但是自己有点不能理解*

#### 实现Emitter的类 -- DiagnosisEventEmitter
	public static class DiagnosisEventEmitter implements Emitter<Long>, Serializable{
		private static final long serialVersionUID = 1L;
		// 线程安全的原子操作Integer的类
		AtomicInteger successfulTransactions = new AtomicInteger(0);
		//参数包括事务信息，batch元数据，和用来发射的collector
		public void emitBatch(TransactionAttempt tx, Long coordinatorMeta, TridentCollector collector) {
			/**
			 * 数据产生
			 */
			for (int i = 0; i < 10000; i++) {
	            List<Object> events = new ArrayList<Object>();
	            double lat = new Double(-30 + (int) (Math.random() * 75));
	            double lng = new Double(-120 + (int) (Math.random() * 70));
	            long time = System.currentTimeMillis();

	            String diag = new Integer(320 + (int) (Math.random() * 7)).toString();
	            DiagnosisEvent event = new DiagnosisEvent(lat, lng, time, diag);
	            events.add(event);
	            collector.emit(events);
	            //发射tuple，这里是将对象封装成独立的字段
	        }
			
		}

		public void success(TransactionAttempt tx) {
			successfulTransactions.incrementAndGet();
		}

		public void close() {
			
		}
		
	}
Emitter只有一个功能，将tuple打包发射出去。 此处留意的是，tuple中所有的元素都必须是可序列化的。