package bolts;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

@SuppressWarnings("serial")
public class StanfordNLPBolt extends BaseRichBolt implements ISentimentBolt {

	private OutputCollector collector;

	private StanfordCoreNLP pipeline;
	private Map<Integer, String> sentimentMap;

	@Override
	public void execute(Tuple tuple) {

		String tweet = tuple.getStringByField("tweet");

		// http://rahular.com/twitter-sentiment-analysis/
		int mainSentiment = 0, longest = 0;

		Annotation annotation = pipeline.process(tweet);

		for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
			Tree tree = sentence.get(SentimentAnnotatedTree.class);
			int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
			String partText = sentence.toString();
			if (partText.length() > longest) {
				mainSentiment = sentiment;
				longest = partText.length();
			}
		}

		this.collector.emit(new Values(tweet, this.sentimentMap.get(mainSentiment), tuple.getStringByField("company")));

		this.collector.ack(tuple);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map map, TopologyContext context, OutputCollector collector) {

		this.collector = collector;

		this.sentimentMap = new HashMap<>();
		this.sentimentMap.put(0, "negative");
		this.sentimentMap.put(1, "negative");
		this.sentimentMap.put(2, "neutral");
		this.sentimentMap.put(3, "positive");
		this.sentimentMap.put(4, "positive");

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");

		this.pipeline = new StanfordCoreNLP(props);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

		declarer.declare(new Fields("tweet", "sentiment", "company"));
	}
}
