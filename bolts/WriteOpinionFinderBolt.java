package bolts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
public class WriteOpinionFinderBolt extends BaseRichBolt {

	private final String command = "java -Xmx1g -classpath "
			+ "/tmp/opinionfinderv2.0/lib/weka.jar:/tmp/opinionfinderv2.0/lib/stanford-postagger.jar:/tmp/opinionfinderv2.0/opinionfinder.jar "
			+ "opin.main.RunOpinionFinder /tmp/docs.txt -d -m /tmp/opinionfinderv2.0/models/ -l /tmp/opinionfinderv2.0/lexicons/";

	private final String basePath = "/tmp/stormOutput/of/";

	private OutputCollector collector;

	private long time;

	@Override
	public void execute(Tuple tuple) {

		try {
			Writer writer = null;

			String comp = tuple.getStringByField("company");
			long time = System.currentTimeMillis() / (1000 * 60 * 30); // every
																		// 30
																		// minutes

			if (time != this.time) {
				Thread runAndWrite = new Thread(new RunAndWrite());
				runAndWrite.start();

				this.time = time;
			}

			String path = this.basePath + comp + "/" + time + ".txt";

			Files.createDirectories(Paths.get(this.basePath + comp));

			writer = new BufferedWriter(new FileWriter(path, true));

			String tweet = tuple.getStringByField("tweet").replaceAll("\n", " ") + "\n";

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

		this.time = System.currentTimeMillis() / (1000 * 60 * 30);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

		// not used
	}

	private class RunAndWrite implements Runnable {

		@Override
		public void run() {

			final String hdfs = "hdfs://hadoop-01.csse.rose-hulman.edu:8020";
			final String sentimentPath = "_auto_anns/exp_polarity.txt";
			final String docList = "/tmp/docs.txt";

			// determine and create document of files to analyze
			List<String> fileNames = new ArrayList<>();

			File[] dirs = new File(WriteOpinionFinderBolt.this.basePath).listFiles();
			for (File dir : dirs) {
				File[] files = new File(dir.getAbsolutePath()).listFiles();
				for (File file : files) {
					fileNames.add(file.getAbsolutePath());
				}
			}

			try {
				Writer writer = new BufferedWriter(new FileWriter(docList));
				for (String fileName : fileNames) {
					writer.write(fileName + "\n");
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// run opinionfinder
			try {
				Process p = Runtime.getRuntime().exec(command);

				BufferedReader errors = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String error;
				while ((error = errors.readLine()) != null) {
					System.out.println(error);
				}

				p.waitFor();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

			// read opinionfinder results and write to hdfs
			try {
				FileSystem fs = FileSystem.get(URI.create(hdfs), new Configuration());
				FSDataOutputStream out = null;

				for (String fileName : fileNames) {

					Path path = new Path(fileName);
					try {
						fs.getFileStatus(path);
						out = fs.append(path);
					} catch (FileNotFoundException e) {
						out = fs.create(path);
					}

					// read file
					BufferedReader reader = new BufferedReader(new FileReader(fileName + sentimentPath));
					String line;
					while ((line = reader.readLine()) != null) {
						String sentiment = line.split("\t")[1].trim() + "\n";
						out.write(sentiment.getBytes());
					}
					reader.close();
				}

				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
