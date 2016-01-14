package aggregate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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

public class Aggregate implements Runnable {

	private Map<String, Map<String, Integer>> results;

	private String basePath;

	public Aggregate(String basePath) {

		this.basePath = basePath;
	}

	@Override
	public void run() {

		try {
			aggregateResults();

			writeResults();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void aggregateResults() throws IOException {

		this.results = new HashMap<>();

		// determine and create document of files to analyze
		List<String> fileNames = new ArrayList<>();

		File[] dirs = new File(this.basePath).listFiles();
		for (File dir : dirs) {

			File[] files = new File(dir.getAbsolutePath()).listFiles();
			for (File file : files) {
				fileNames.add(file.getAbsolutePath());
			}
		}

		System.out.println(fileNames);

		for (String fileName : fileNames) {

			// read file and add to map
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null) {
				String sentiment = line.split("\t")[1].trim();

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
	}

	private void writeResults() throws IOException {

		final String hdfs = "hdfs://hadoop-01.csse.rose-hulman.edu:8020";
		final String sentimentResultsPath = "sentimentResults/";

		FileSystem fs = FileSystem.get(URI.create(hdfs), new Configuration());

		Writer writer = null;
		FSDataOutputStream out = null;

		for (String company : this.results.keySet()) {

			String localPath = this.basePath + sentimentResultsPath + company + ".txt";
			Path hdfsPath = new Path(this.basePath + sentimentResultsPath + company + ".txt");

			Files.createDirectories(Paths.get(this.basePath + sentimentResultsPath));
			writer = new BufferedWriter(new FileWriter(localPath, false));
			out = fs.create(hdfsPath);

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
