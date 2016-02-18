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
			boolean results = readResults();

			if (results) {
				writeResults();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// read opinionfinder results and add to map
	private boolean readResults() throws IOException {

		final String sentimentFile = "/exp_polarity.txt";

		Map<String, List<String>> sentimentFiles = new HashMap<>();

		File[] compDirs = new File(this.basePath).listFiles();
		if (compDirs == null) {
			return false;
		}

		// get sentiment files
		for (File cDir : compDirs) {

			if (!cDir.isDirectory()) {
				continue;
			}

			String company = cDir.getAbsolutePath().replaceAll(this.basePath, "").trim();

			File[] sentDirs = new File(cDir.getAbsolutePath()).listFiles();

			List<String> paths = sentimentFiles.get(company);
			if (paths == null) {
				paths = new ArrayList<>();
			}

			for (File sDir : sentDirs) {
				if (sDir.isDirectory()) {
					String path = sDir.getAbsolutePath() + sentimentFile;
					paths.add(path);

					sentimentFiles.put(company, paths);
				}
			}
		}

		this.results = new HashMap<>();

		// read sentiment files and make results map
		for (String company : sentimentFiles.keySet()) {

			List<String> paths = sentimentFiles.get(company);

			for (String path : paths) {

				// read file
				BufferedReader reader = new BufferedReader(new FileReader(path));
				String line;
				while ((line = reader.readLine()) != null) {
					String sentiment = line.split("\t")[1].trim();

					// add to map
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
		}

		return true;
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
