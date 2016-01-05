package bolts;

import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

@SuppressWarnings("serial")
public class GroupingBolt extends BaseRichBolt {

	private OutputCollector collector;
	private final String[] companies = { "amazon", "google", "microsoft", "facebook", "twitter", "walmart", "target",
			"delta", "mcdonalds", "burger king" };

	@Override
	public void execute(Tuple tuple) {

		String tweet = tuple.getStringByField("tweet").toLowerCase();

		String company = "";

		for (String comp : this.companies) {

			if (tweet.contains(comp)) {
				company = comp;
				break;
			}
		}

		if (company != "") {
			this.collector.emit(new Values(tweet, company));
		} else {
			this.collector.emit(new Values(tweet, "none"));
		}

		this.collector.ack(tuple);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map map, TopologyContext context, OutputCollector collector) {

		this.collector = collector;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

		declarer.declare(new Fields("tweet", "company"));
	}
}
