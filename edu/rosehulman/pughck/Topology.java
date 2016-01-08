package edu.rosehulman.pughck;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;

import bolts.GroupingBolt;
import bolts.HdfsOutBolt;
import bolts.StanfordNLPBolt;
import bolts.HdfsOutBoltSentiment;
import bolts.OpenNLPBolt;
import bolts.WriteBolt;
import bolts.WriteOpinionFinderBolt;
import spouts.TweetSpout;

public class Topology {

	public static void main(String[] args) throws Exception {

		Config conf = new Config();

		conf.setNumWorkers(5);
		conf.setMaxSpoutPending(5000);

		TopologyBuilder builder = new TopologyBuilder();

		BaseRichSpout spout = new TweetSpout("OgHA59vKcpBKqr92QsVyhGswD",
				"nWComrkhNlHYKVE2SjCb2D1roLzNog1NNDEh5s98c9i6KJJ6XT",
				"4041041357-rkWibnDMhQSwJD1g5iOCsJae2J56Ni4XGbbOVe9", "XaDM2EVfh3om2uWsUD6sWVCeOERFgyDqHQAb6FfwSUUix");

		builder.setSpout("tweetSpout", spout, 1);

		builder.setBolt("groupBolt", new GroupingBolt(), 5).shuffleGrouping("tweetSpout");

		// write locally
		builder.setBolt("writeBolt", new WriteBolt(), 5).fieldsGrouping("groupBolt", new Fields("company"));

		// write to hdfs
		builder.setBolt("hdfsBolt", new HdfsOutBolt(), 5).fieldsGrouping("groupBolt", new Fields("company"));

		builder.setBolt("OFBolt", new WriteOpinionFinderBolt(), 5).fieldsGrouping("groupBolt", new Fields("company"));

		// opinionfinder sentiment to hdfs every tweet
		/*
		 * builder.setBolt("sentimentBoltOpinionFinder", new
		 * OpinionFinderBolt(), 5).shuffleGrouping("groupBolt");
		 * builder.setBolt("hdfsBoltSentimentOpinionFinder", new
		 * HdfsOutBoltSentiment("opinionfinder"), 5)
		 * .fieldsGrouping("sentimentBoltOpinionFinder", new Fields("company"));
		 */

		// stanford sentiment to hdfs
		builder.setBolt("sentimentBoltStanford", new StanfordNLPBolt(), 5).shuffleGrouping("groupBolt");
		builder.setBolt("hdfsBoltSentimentStanfordNLP", new HdfsOutBoltSentiment("stanfordNLP"), 5)
				.fieldsGrouping("sentimentBoltStanford", new Fields("company"));

		// openNLP sentiment to hdfs
		builder.setBolt("sentimentBoltOpenNLP", new OpenNLPBolt(), 5).shuffleGrouping("groupBolt");
		builder.setBolt("hdfsBoltSentimentOpenNLP", new HdfsOutBoltSentiment("openNLP"), 5)
				.fieldsGrouping("sentimentBoltOpenNLP", new Fields("company"));

		StormSubmitter.submitTopology("topology", conf, builder.createTopology());
	}
}
