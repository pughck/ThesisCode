package bolts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

import topology.Topology;

@SuppressWarnings("serial")
public class WriteBoltSentiment extends BaseRichBolt {

	private final String basePath = "/tmp/stormOutputSentiment/";

	private OutputCollector collector;

	private String type;

	public WriteBoltSentiment(String type) {

		this.type = type;
	}

	@Override
	public void execute(Tuple tuple) {

		try {
			Writer writer = null;

			String comp = tuple.getStringByField("company");
			long time = Topology.getTime();

			String path = this.basePath + this.type + "/" + comp + "/" + time + ".txt";

			Files.createDirectories(Paths.get(this.basePath + this.type + "/" + comp));

			writer = new BufferedWriter(new FileWriter(path, true));

			String tweet = tuple.getStringByField("tweet") + "\t";

			String sentiment = tuple.getStringByField("sentiment") + "\n";

			writer.append(tweet + sentiment);
			writer.close();

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
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

		// not used
	}
}
