package prediction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.mllib.linalg.Vector;

import scala.Serializable;

public class Predict implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String NEWLINE = "\n";

	private String type;

	private String trainingPath;
	private String testingPath;

	private List<Double> valuesList;
	private List<Double> netList;

	public Predict(String type, String trainingPath, String testingPath) {

		this.type = type;

		this.trainingPath = trainingPath;
		this.testingPath = testingPath;
	}

	public String correlation() {

		try {
			populateCorrelationLists(this.testingPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JavaSparkContext jsc = new JavaSparkContext(new SparkConf().setAppName("stock training").setMaster("local"));

		JavaDoubleRDD values = jsc.parallelizeDoubles(this.valuesList);
		JavaDoubleRDD nets = jsc.parallelizeDoubles(this.netList);

		double correlation = Statistics.corr(nets.srdd(), values.srdd());

		jsc.close();

		StringBuilder builder = new StringBuilder().append(NEWLINE).append(this.type).append(NEWLINE).append(NEWLINE)
				.append("Correlation: " + String.format("%.5f", correlation)).append(NEWLINE);

		return builder.toString();
	}

	private void populateCorrelationLists(String path) throws IOException {

		this.valuesList = new ArrayList<>();
		this.netList = new ArrayList<>();

		BufferedReader reader = new BufferedReader(new FileReader(path));

		String line;
		while ((line = reader.readLine()) != null) {

			String[] values = line.split("\t+");

			double value = Double.parseDouble(values[0].trim());
			double net = Double.parseDouble(values[4].trim());

			this.valuesList.add(value);
			this.netList.add(net);
		}

		reader.close();
	}

	public String predict() {

		List<LabeledPoint> trainingSetList = null;
		List<LabeledPoint> testingSetList = null;

		try {
			trainingSetList = createLPList(this.trainingPath);

			testingSetList = createLPList(this.testingPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JavaSparkContext jsc = new JavaSparkContext(new SparkConf().setAppName("stock training").setMaster("local"));

		JavaRDD<LabeledPoint> training = jsc.parallelize(trainingSetList);

		final NaiveBayesModel model = NaiveBayes.train(training.rdd(), 1.0);

		int correct = 0;
		int incorrect = 0;
		double magDif = 0;

		for (LabeledPoint lp : testingSetList) {
			double actual = lp.label();
			double predicted = model.predict(lp.features());

			if (Math.signum(actual) == Math.signum(predicted)) {
				correct++;
			} else {
				incorrect++;
			}

			magDif += Math.pow(predicted - actual, 2);
		}

		jsc.close();

		StringBuilder builder = new StringBuilder().append("Correct sign: ").append(correct).append(NEWLINE)
				.append("Incorrect sign: ").append(incorrect).append(NEWLINE).append(NEWLINE)
				.append("Magnitude differene: ").append(String.format("%.5f", magDif)).append(NEWLINE);

		return builder.toString();
	}

	private List<LabeledPoint> createLPList(String path) throws IOException {

		List<LabeledPoint> lPoints = new ArrayList<>();

		BufferedReader reader = new BufferedReader(new FileReader(path));

		String line;
		while ((line = reader.readLine()) != null) {

			String[] values = line.split("\t+");

			double value = Double.parseDouble(values[0].trim());

			int positive = Integer.parseInt(values[1].trim());
			int neutral = Integer.parseInt(values[2].trim());
			int negative = Integer.parseInt(values[3].trim());

			Vector features = new DenseVector(new double[] { positive, neutral, negative });

			LabeledPoint lp = new LabeledPoint(value, features);

			lPoints.add(lp);
		}

		reader.close();

		return lPoints;
	}
}
