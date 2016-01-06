package bolts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

@SuppressWarnings("serial")
public class OpinionFinderBolt extends BaseRichBolt implements ISentimentBolt {

	private final String command = "java -Xmx1g -classpath "
			+ "/tmp/opinionfinderv2.0/lib/weka.jar:/tmp/opinionfinderv2.0/lib/stanford-postagger.jar:/tmp/opinionfinderv2.0/opinionfinder.jar "
			+ "opin.main.RunOpinionFinder /tmp/docs.txt -d -m /tmp/opinionfinderv2.0/models/ -l /tmp/opinionfinderv2.0/lexicons/";

	private OutputCollector collector;

	private Map<String, Integer> toNumSentiment;
	private Map<Integer, String> toSentiment;

	@Override
	public void execute(Tuple tuple) {

		String tweet = tuple.getStringByField("tweet");

		int sentiment;

		try {
			writeTweet(tweet);

			runOpinionFinder();

			sentiment = readSentimentResult();
		} catch (IOException | InterruptedException e) {
			this.collector.fail(tuple);
			e.printStackTrace();

			return;
		}

		this.collector.emit(new Values(tweet, this.toSentiment.get(sentiment), tuple.getStringByField("company")));

		this.collector.ack(tuple);
	}

	private void writeTweet(String tweet) throws IOException {

		Writer writer = new BufferedWriter(new FileWriter("/tmp/temp.txt"));
		writer.write(tweet + "\n");
		writer.close();
	}

	private void runOpinionFinder() throws InterruptedException, IOException {

		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
	}

	private int readSentimentResult() throws IOException {

		// this logic should be right
		double sentiment = 0;
		int count = 0;

		String line;

		BufferedReader reader = new BufferedReader(new FileReader("/tmp/temp.txt_auto_anns/exp_polarity.txt"));
		while ((line = reader.readLine()) != null) {
			sentiment += this.toNumSentiment.get(line.split("\t")[1].trim());
			count++;
		}
		reader.close();

		return (int) Math.round(sentiment / count);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map map, TopologyContext context, OutputCollector collector) {

		this.collector = collector;

		this.toNumSentiment = new HashMap<>();
		this.toNumSentiment.put("negative", -1);
		this.toNumSentiment.put("neutral", 0);
		this.toNumSentiment.put("positive", 1);

		this.toSentiment = new HashMap<>();
		this.toSentiment.put(-1, "negative");
		this.toSentiment.put(0, "neutral");
		this.toSentiment.put(1, "positive");

		try {
			Writer writer = new BufferedWriter(new FileWriter("/tmp/docs.txt"));
			writer.write("/tmp/temp.txt\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

		declarer.declare(new Fields("tweet", "sentiment", "company"));
	}
}
