package bolts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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

		private List<String> fileNames;
		private Map<String, Map<String, Integer>> results;

		private long time;

		public RunWriteAggregate(long time) {

			this.time = time;
		}

		@Override
		public void run() {

			this.fileNames = new ArrayList<>();
			this.results = new HashMap<>();

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

			try {
				readResults();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				writeResults();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// determine and create document of files to analyze
		private void createDocList() throws IOException {

			final String docList = "/tmp/docs.txt";

			File[] dirs = new File(WriteOpinionFinderBolt.this.basePath).listFiles();

			if (dirs == null) {
				System.err.println("nothing here");
				return;
			}

			for (File dir : dirs) {
				this.fileNames.add(dir.getAbsolutePath() + "/" + this.time + ".txt");
			}

			System.out.println(this.fileNames);

			Writer writer = new BufferedWriter(new FileWriter(docList));
			for (String fileName : this.fileNames) {
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

		// read opinionfinder results and add to map
		private void readResults() throws IOException {

			final String sentimentPath = "_auto_anns/exp_polarity.txt";

			for (String fileName : this.fileNames) {

				// read file
				BufferedReader reader = new BufferedReader(new FileReader(fileName + sentimentPath));
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
					String sentiment = line.split("\t")[1].trim();

					// add to map
					String company = fileName.split("/")[fileName.split("/").length - 2].trim();
					Map<String, Integer> companyResults = this.results.get(company);
					if (companyResults == null) {
						companyResults = new HashMap<>();
						companyResults.put("negative", 0);
						companyResults.put("neutral", 0);
						companyResults.put("positive", 0);
					}
					companyResults.put(sentiment, companyResults.get(sentiment) + 1);
					this.results.put(company, companyResults);
				}

				reader.close();
			}

			System.out.println(this.results);
		}

		private void writeResults() throws IOException {

			final String hdfs = "hdfs://hadoop-01.csse.rose-hulman.edu:8020";
			final String sentimentResultsPath = "sentimentResults/";

			FileSystem fs = FileSystem.get(URI.create(hdfs), new Configuration());

			Writer writer = null;
			FSDataOutputStream out = null;

			for (String company : this.results.keySet()) {

				String localPath = WriteOpinionFinderBolt.this.basePath + sentimentResultsPath + company + ".txt";
				Path hdfsPath = new Path(
						WriteOpinionFinderBolt.this.basePath + sentimentResultsPath + company + ".txt");

				// populate map if needed
				File compResults = new File(localPath);
				if (compResults.exists()) {
					populateMap(localPath, company);
				} else {
					Files.createDirectories(Paths.get(WriteOpinionFinderBolt.this.basePath + sentimentResultsPath));
				}

				writer = new BufferedWriter(new FileWriter(compResults, false));
				out = fs.create(hdfsPath);

				Map<String, Integer> companyResults = this.results.get(company);
				for (String sentimentKey : companyResults.keySet()) {
					String value = sentimentKey + "\t" + companyResults.get(sentimentKey) + "\n";

					writer.append(value);
					out.write(value.getBytes());
				}

				writer.close();
				out.close();
			}
		}

		private void populateMap(String path, String company) throws IOException {

			System.out.println("populating map");

			BufferedReader reader = new BufferedReader(new FileReader(path));

			String line;
			while ((line = reader.readLine()) != null) {
				String[] lineContents = line.split("\t");

				String sentiment = lineContents[0].trim();
				int value = Integer.parseInt(lineContents[1].trim());

				Map<String, Integer> companyResults = this.results.get(company);
				if (companyResults == null) {
					companyResults = new HashMap<String, Integer>();
					companyResults.put("negative", 0);
					companyResults.put("neutral", 0);
					companyResults.put("positive", 0);
				}
				companyResults.put(sentiment, companyResults.get(sentiment) + value);
				this.results.put(company, companyResults);

				this.results.put(company, companyResults);
			}

			reader.close();
		}
	}
}
