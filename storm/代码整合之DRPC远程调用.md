#代码整合之DRPC远程调用
[storm DRPC官方文档](http://storm.apache.org/releases/1.0.1/Distributed-RPC.html)  
[storm DRPC翻译文档](http://weyo.me/pages/techs/storm-drpc-basis/)

DRPC server 负责接收 RPC 请求，并将该请求发送到 Storm 中运行的 Topology，等待接收 Topology 发送的处理结果，并将该结果返回给发送请求的客户端。通俗一点的解释就是，Topology在定义的时候就定义成DRPC类型的(Stream为DRPCStream,其Spout为DRPCSpout,客户端就是一个Spout),当Topology发布后，客户端调用时Spout输出。

### 配置

在集群其他机器上添加配置:

	drpc.servers:
	  - "localhost"
	  - "otherdrpcservers"
	  
### 启动服务

	storm drpc

### 定义 DRPC topology

在第六章的示例人工智能中，使用了DRPC远程调用。这里便以人工智能的例子来举例(com.broad.game.DrpcTopology)。示例的功能是输入为某种棋盘状态，
输出为下一步最好的落子位置。

##### 本地模式
	 final LocalCluster cluster = new LocalCluster();
	 final Config conf = new Config();

     LocalDRPC client = new LocalDRPC();
     TridentTopology drpcTopology = new TridentTopology();

     drpcTopology.newDRPCStream("drpc", client)
            .each(new Fields("args"), new ArgsFunction(), new Fields("gamestate"))
            .each(new Fields("gamestate"), new GenerateBoards(), new Fields("children"))
            .each(new Fields("children"), new ScoreFunction(), new Fields("board", "score", "player"))
            .groupBy(new Fields("gamestate"))
            .aggregate(new Fields("board", "score"), new FindBestMove(), new Fields("bestMove"))
            .project(new Fields("bestMove"));

    cluster.submitTopology("drpcTopology", conf, drpcTopology.build());

    Board board = new Board();
    board.board[1][1] = "O";
    board.board[2][2] = "X";
    board.board[0][1] = "O";
    board.board[0][0] = "X";
    
    client.execute("drpc", board.toKey());

##### 远程模式

使用***DRPCStream***。

	final Config conf = new Config();
	TridentTopology drpcTopology = new TridentTopology();
	drpcTopology.newDRPCStream("drpc")
			.each(new Fields("args"), new ArgsFunction(), new Fields("gamestate"))
			.each(new Fields("gamestate"), new GenerateBoards(), new Fields("children"))
			.each(new Fields("children"), new ScoreFunction(), new Fields("board", "score", "player"))
			.groupBy(new Fields("gamestate"))
			.aggregate(new Fields("board", "score"), new FindBestMove(), new Fields("bestMove"))
			.project(new Fields("bestMove"));
	StormSubmitter.submitTopology("drpc-test", conf, drpcTopology.build());
	

通过调用newDRPCStream("drpc")会产生componentId为drpc的DRPCSpout,客户端调用的函数名应与这个componentId相同，在 ***DRPC server 启动完成***、***DPRC Topology 提交到 Storm 集群运行***之后，就可以启动 DRPC 客户端。以下是客户端调用的代码

	public class TestDrpc {
		public static void main(String[] args) throws TException, DRPCExecutionException {
			DRPCClient client = new DRPCClient("192.168.5.121", 3772);
			Board board = new Board();
			board.board[1][1] = "O";
			board.board[2][2] = "X";
			board.board[0][1] = "O";
			board.board[0][0] = "X";
			String result = client.execute("drpc", board.toKey());
			System.out.println("Drpc    " + result);
		}
	}
	
+ new DRPCClient("192.168.5.121", 3772)的参数1为服务地址，应属于配置的drpc.servers
+ client.execute("drpc", board.toKey())执行的函数名应为对应DRPCSpout的componentId，传递的参数和函数的返回值目前只能为String类型。返回值为Topology的计算结果

### 适用场景
目前根据可请求Topology并且返回结果的特性，感觉可将某些可抽象模块化出来的一些计算式、工具类写成DRPC类型的Topology。