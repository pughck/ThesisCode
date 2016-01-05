package bolts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

@SuppressWarnings("serial")
public class HdfsOutBolt extends BaseRichBolt implements IHdfsBolt {

	private final String basePath = "/tmp/stormOutput/";
	private final String hdfs = "hdfs://hadoop-01.csse.rose-hulman.edu:8020";

	private FileSystem fs;
	private OutputCollector collector;

	@Override
	public void execute(Tuple tuple) {

		try {
			FSDataOutputStream out = null;

			String comp = tuple.getStringByField("company");
			long time = System.currentTimeMillis() / (1000 * 60 * 60); // every
																		// hour
			Path path = new Path(this.basePath + comp + "/", time + ".txt");
			try {
				this.fs.getFileStatus(path);
				out = this.fs.append(path);
			} catch (FileNotFoundException e) {
				out = this.fs.create(path);
			}

			String tweet = tuple.getStringByField("tweet").replaceAll("\n", " ") + "\n\n";

			out.write(tweet.getBytes());
			out.close();

			this.collector.ack(tuple);
		} catch (IOException e) {
			this.collector.fail(tuple);
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map map, TopologyContext context, OutputCollector collector) {

		this.collector = collector;
		try {
			this.fs = FileSystem.get(URI.create(this.hdfs), new Configuration());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

	}
}
