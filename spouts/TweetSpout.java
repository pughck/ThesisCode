package spouts;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

@SuppressWarnings("serial")
public class TweetSpout extends BaseRichSpout {

	private SpoutOutputCollector collector;
	private TwitterStream stream;
	private LinkedBlockingQueue<String> queue;

	private String key;
	private String secret;
	private String token;
	private String accessSecret;

	public TweetSpout(String key, String secret, String token, String accessSecret) {

		this.key = key;
		this.secret = secret;
		this.token = token;
		this.accessSecret = accessSecret;
	}

	@Override
	public void nextTuple() {

		String next = this.queue.poll();
		if (next == null) {
			Utils.sleep(100);
			return;
		}

		this.collector.emit(new Values(next));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map map, TopologyContext context, SpoutOutputCollector collector) {

		this.queue = new LinkedBlockingQueue<String>(1000);
		this.collector = collector;

		ConfigurationBuilder config = new ConfigurationBuilder().setOAuthConsumerKey(this.key)
				.setOAuthConsumerSecret(this.secret).setOAuthAccessToken(this.token)
				.setOAuthAccessTokenSecret(this.accessSecret);

		TwitterStreamFactory tsf = new TwitterStreamFactory(config.build());

		this.stream = tsf.getInstance();
		this.stream.addListener(new TweetListener());

		this.stream.sample("en");
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

		declarer.declare(new Fields("tweet"));
	}

	private class TweetListener implements StatusListener {

		@Override
		public void onStatus(Status status) {

			TweetSpout.this.queue.offer(status.getText());
		}

		@Override
		public void onException(Exception e) {

			e.printStackTrace();
		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice sdn) {

			// not used
		}

		@Override
		public void onScrubGeo(long arg0, long arg1) {

			// not used
		}

		@Override
		public void onStallWarning(StallWarning warning) {

			// not used
		}

		@Override
		public void onTrackLimitationNotice(int arg0) {

			// not used
		}
	}
}
