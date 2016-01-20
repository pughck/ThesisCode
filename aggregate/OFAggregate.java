package aggregate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

public class OFAggregate implements Runnable {

	private Map<String, Map<String, Integer>> results;

	private String basePath;

	public OFAggregate(String basePath) {

		this.basePath = basePath;
	}

	@Override
	public void run() {

		try {
			boolean results = aggregateResults();

			if (results) {
				writeResults();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean aggregateResults() throws IOException {

		final String sentimentResults = "sentimentResults/";

		this.results = new HashMap<>();

		List<String> fileNames = new ArrayList<>();

		File[] files = new File(this.basePath + sentimentResults).listFiles();

		if (files == null) {
			System.err.println("nothing here");

			return false;
		}

		for (File file : files) {
			fileNames.add(file.getAbsolutePath());
		}

		for (String fileName : fileNames) {

			String company = fileName.split("/")[fileName.split("/").length - 1].trim().replace(".txt", "");
			Map<String, Integer> companyResults = new HashMap<>();

			// read file and add to map
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null) {
				String sentiment = line.split("\t")[0].trim();
				int value = Integer.parseInt(line.split("\t")[1].trim());

				companyResults.put(sentiment, value);
				this.results.put(company, companyResults);
			}

			reader.close();
		}

		return !fileNames.isEmpty();
	}

	private void writeResults() throws IOException {

		final String hdfs = "hdfs://hadoop-01.csse.rose-hulman.edu:8020";
		final String sentimentResultsPath = "sentimentResults.txt";

		final String header = "COMP\t\tPOS\tNEU\tNEG\tNET\n";

		FileSystem fs = FileSystem.get(URI.create(hdfs), new Configuration());

		Writer writer = null;
		FSDataOutputStream out = null;

		String localPath = this.basePath + sentimentResultsPath;
		Path hdfsPath = new Path(this.basePath + sentimentResultsPath);

		Files.createDirectories(Paths.get(this.basePath));
		writer = new BufferedWriter(new FileWriter(localPath, false));

		writer.append(header);

		try {
			fs.getFileStatus(hdfsPath);
			out = fs.append(hdfsPath);
		} catch (FileNotFoundException e) {
			out = fs.create(hdfsPath);

			out.write(header.getBytes());
		}

		for (String company : this.results.keySet()) {

			Map<String, Integer> companyResults = this.results.get(company);

			int positive = companyResults.get("positive");
			int neutral = companyResults.get("neutral");
			int negative = companyResults.get("negative");

			int net = 2 * positive + neutral - 2 * negative;

			String firstDelim = (company.length() >= 8) ? "\t" : "\t\t";

			String value = company + firstDelim + positive + "\t" + neutral + "\t" + negative + "\t" + net + "\n";

			writer.append(value);
			out.write(value.getBytes());
		}

		writer.close();
		out.close();
	}
}
