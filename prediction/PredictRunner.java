package prediction;

public class PredictRunner {

	public static void main(String[] args) throws InterruptedException {

		if (args.length < 2) {
			System.err.println("Need at least totalMin arg and one date arg");

			return;
		}

		int totalMin = Integer.parseInt(args[0].trim());

		final String basePath = "C:/Users/pughck/Documents/Academics/'15-'16/CS Thesis/results/";

		for (int i = 1; i < args.length; i++) {
			Predict predict = new Predict(basePath + "of/training-" + totalMin + ".txt",
					basePath + "of/" + args[i] + "/testing-" + totalMin + ".txt",
					basePath + "of/results-" + totalMin + ".txt");
			predict.predict();

			predict = new Predict(basePath + "stanford/training-" + totalMin + ".txt",
					basePath + "stanford/" + args[i] + "/testing-" + totalMin + ".txt",
					basePath + "stanford/results-" + totalMin + ".txt");
			predict.predict();

			predict = new Predict(basePath + "open/training-" + totalMin + ".txt",
					basePath + "open/" + args[i] + "/testing-" + totalMin + ".txt",
					basePath + "open/results-" + totalMin + ".txt");
			predict.predict();
		}
	}
}
