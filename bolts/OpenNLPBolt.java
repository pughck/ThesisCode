package bolts;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

@SuppressWarnings("serial")
public class OpenNLPBolt extends BaseRichBolt implements ISentimentBolt {

	private OutputCollector collector;

	private DoccatModel model;
	private DocumentCategorizerME categorizer;

	private Map<String, String> sentimentMap;

	@Override
	public void execute(Tuple tuple) {

		String tweet = tuple.getStringByField("tweet");

		double[] outcomes = this.categorizer.categorize(tweet);
		String category = this.categorizer.getBestCategory(outcomes);

		this.collector.emit(new Values(tweet, this.sentimentMap.get(category), tuple.getStringByField("company")));

		this.collector.ack(tuple);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void prepare(Map map, TopologyContext context, OutputCollector collector) {

		this.collector = collector;

		this.sentimentMap = new HashMap<>();
		this.sentimentMap.put("0", "negative");
		this.sentimentMap.put("1", "positive");

		final String trainingFile = "/tmp/trainingTweets.txt";

		// http://technobium.com/sentiment-analysis-using-opennlp-document-categorizer/
		InputStream dataIn = null;
		try {
			// TODO: real / better training file
			dataIn = new FileInputStream(trainingFile);

			ObjectStream lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
			ObjectStream sampleStream = new DocumentSampleStream(lineStream);

			// Specifies the minimum number of times a feature must be seen
			int cutoff = 2, trainingIterations = 30;

			this.model = DocumentCategorizerME.train("en", sampleStream, cutoff, trainingIterations);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (dataIn != null) {
				try {
					dataIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		this.categorizer = new DocumentCategorizerME(this.model);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

		declarer.declare(new Fields("tweet", "sentiment", "company"));

	}
}
