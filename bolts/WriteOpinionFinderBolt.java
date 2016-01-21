package bolts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

import topology.Topology;

@SuppressWarnings("serial")
public class WriteOpinionFinderBolt extends BaseRichBolt {

	private final String basePath = "/tmp/stormOutputSentiment/opinionfinder/";

	private OutputCollector collector;

	private long time;

	@Override
	public void execute(Tuple tuple) {

		try {
			Writer writer = null;

			String comp = tuple.getStringByField("company");
			long currentTime = Topology.getTime();

			if (currentTime != this.time) {
				Thread runAndWrite = new Thread(new RunWriteAggregate(this.time));
				runAndWrite.start();

				this.time = currentTime;
			}

			String path = this.basePath + comp + "/" + currentTime + ".txt";

			Files.createDirectories(Paths.get(this.basePath + comp));

			writer = new BufferedWriter(new FileWriter(path, true));

			String tweet = tuple.getStringByField("tweet") + "\n";

			writer.append(tweet);
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

		this.time = Topology.getTime();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

		// not used
	}

	private class RunWriteAggregate implements Runnable {

		private final String command = "java -Xmx1g -classpath "
				+ "/tmp/opinionfinderv2.0/lib/weka.jar:/tmp/opinionfinderv2.0/lib/stanford-postagger.jar:/tmp/opinionfinderv2.0/opinionfinder.jar "
				+ "opin.main.RunOpinionFinder /tmp/docs.txt -d -m /tmp/opinionfinderv2.0/models/ -l /tmp/opinionfinderv2.0/lexicons/";

		private long time;

		public RunWriteAggregate(long time) {

			this.time = time;
		}

		@Override
		public void run() {

			try {
				createDocList();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				runOpinionFinder();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		// determine and create document of files to analyze
		private void createDocList() throws IOException {

			final String docList = "/tmp/docs.txt";

			List<String> fileNames = new ArrayList<>();

			File[] dirs = new File(WriteOpinionFinderBolt.this.basePath).listFiles();

			if (dirs == null) {
				System.err.println("nothing here");
				return;
			}

			for (File dir : dirs) {
				fileNames.add(dir.getAbsolutePath() + "/" + this.time + ".txt");
			}

			Writer writer = new BufferedWriter(new FileWriter(docList));
			for (String fileName : fileNames) {
				writer.write(fileName + "\n");
			}

			writer.close();
		}

		// run opinionfinder
		private void runOpinionFinder() throws IOException, InterruptedException {

			Process p = Runtime.getRuntime().exec(this.command);

			BufferedReader errors = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String error;
			while ((error = errors.readLine()) != null) {
				System.err.println(error);
			}

			p.waitFor();
		}
	}
}
