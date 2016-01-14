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
import java.util.HashMap;
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

	private final String basePath = "/tmp/stormOutputSentiment/opinionfinder/";

	private OutputCollector collector;

	private long time;

	@Override
	public void execute(Tuple tuple) {

		try {
			Writer writer = null;

			String comp = tuple.getStringByField("company");

			// every 10 minutes
			long currentTime = System.currentTimeMillis() / (1000 * 60 * 10);

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

		this.time = System.currentTimeMillis() / (1000 * 60 * 30);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

		// not used
	}

	private class RunWriteAggregate implements Runnable {

		private final String command = "java -Xmx1g -classpath "
				+ "/tmp/opinionfinderv2.0/lib/weka.jar:/tmp/opinionfinderv2.0/lib/stanford-postagger.jar:/tmp/opinionfinderv2.0/opinionfinder.jar "
				+ "opin.main.RunOpinionFinder /tmp/docs.txt -d -m /tmp/opinionfinderv2.0/models/ -l /tmp/opinionfinderv2.0/lexicons/";

		private final String hdfs = "hdfs://hadoop-01.csse.rose-hulman.edu:8020";

		private FileSystem fs;

		private Map<String, Map<String, Integer>> results;

		private long time;

		public RunWriteAggregate(long time) {

			this.time = time;
		}

		@Override
		public void run() {

			final String sentimentPath = "_auto_anns/exp_polarity.txt";
			final String docList = "/tmp/docs.txt";

			this.results = new HashMap<>();
			// TODO: if file exists - read and pre-populate map

			// determine and create document of files to analyze
			List<String> fileNames = new ArrayList<>();

			File[] dirs = new File(WriteOpinionFinderBolt.this.basePath).listFiles();

			if (dirs == null) {
				return;
			}

			for (File dir : dirs) {
				// File[] files = new File(dir.getAbsolutePath()).listFiles();
				// for (File file : files) {
				// fileNames.add(file.getAbsolutePath());
				// }

				fileNames.add(dir.getAbsolutePath() + "/" + this.time + ".txt");
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
				Process p = Runtime.getRuntime().exec(this.command);

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
				this.fs = FileSystem.get(URI.create(this.hdfs), new Configuration());
				FSDataOutputStream out = null;

				for (String fileName : fileNames) {

					Path path = new Path(fileName);
					try {
						this.fs.getFileStatus(path);
						out = this.fs.append(path);
					} catch (FileNotFoundException e) {
						out = this.fs.create(path);
					}

					// read file
					BufferedReader reader = new BufferedReader(new FileReader(fileName + sentimentPath));
					String line;
					while ((line = reader.readLine()) != null) {
						String sentiment = line.split("\t")[1].trim();

						out.write((sentiment + "\n").getBytes());

						// add to map
						String company = fileName.split("/")[fileName.split("/").length - 2].trim();
						Map<String, Integer> companyResults = results.get(company);
						if (companyResults == null) {
							companyResults = new HashMap<String, Integer>();
							companyResults.put("negative", 0);
							companyResults.put("neutral", 0);
							companyResults.put("positive", 0);
						}
						companyResults.put(sentiment, companyResults.get(sentiment) + 1);
						this.results.put(company, companyResults);
					}

					reader.close();
				}

				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				writeResults();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void writeResults() throws IOException {

			final String sentimentResultsPath = "sentimentResults/";

			Writer writer = null;
			FSDataOutputStream out = null;

			for (String company : this.results.keySet()) {

				String localPath = WriteOpinionFinderBolt.this.basePath + sentimentResultsPath + company + ".txt";
				Path hdfsPath = new Path(
						WriteOpinionFinderBolt.this.basePath + sentimentResultsPath + company + ".txt");

				Files.createDirectories(
						Paths.get(WriteOpinionFinderBolt.this.basePath + sentimentResultsPath + company));
				writer = new BufferedWriter(new FileWriter(localPath, false));
				out = this.fs.create(hdfsPath);

				Map<String, Integer> companyResults = this.results.get(company);
				for (String sentimentKey : companyResults.keySet()) {
					String value = sentimentKey + "\t" + companyResults.get(sentimentKey) + "\n";

					writer.append(value);
					out.write(value.getBytes());
				}
			}

			writer.close();
			out.close();
		}
	}
}
