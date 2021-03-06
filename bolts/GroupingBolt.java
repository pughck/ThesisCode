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

	private final String[] companies = { "aflac", "alibaba", "allstate", "amazon", "american airlines",
			"american express", "apple", "aramark", "astrazeneca", "at&t", "aviva", "bank of america",
			"berkshire hathaway", "best buy", "blackrock", "boeing", "canon", "capital one", "caterpillar", "chevron",
			"cisco", "citigroup", "coke", "comcast", "costco", "cummins", "delta", "disney", "ebay", "eil lilly",
			"exelon", "expedia", "exxon mobile", "facebook", "fedex", "ford", "gap", "general electric",
			"general motors", "goldman sachs", "google", "halliburton", "home depot", "honda", "honeywell", "intel",
			"jc penny", "john deere", "johnson & johnson", "jpmorgan chase", "kohls", "kroger", "linkedin",
			"lockheed martin", "lowes", "marathon", "mastercard", "mcdonalds", "metlife", "microsoft", "monsanto",
			"morgan stanley", "netflix", "nike", "nvidia", "oracle", "pepsi", "pfizer", "procter & gamble",
			"progressive", "prudential", "qualcomm", "raytheon", "rockwell collins", "staples", "starbucks", "target",
			"tesla", "time warner", "toyota", "travelers", "twitter", "valero", "verizon", "visa", "walmart",
			"wells fargo", "whirlpool", "whole foods", "yahoo" };

	private OutputCollector collector;

	@Override
	public void execute(Tuple tuple) {

		String tweet = tuple.getStringByField("tweet").toLowerCase().replaceAll("\n", " ");

		for (String company : this.companies) {

			if (tweet.contains(company)) {
				this.collector.emit(new Values(tweet, company));
			}
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
