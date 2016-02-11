package prediction;

public class PredictRunner {

	public static void main(String[] args) throws InterruptedException {

		Predict predict = new Predict("OpinionFinder",
				"C:/Users/pughck/Documents/Academics/'15-'16/CS Thesis/results/of/training.txt",
				"C:/Users/pughck/Documents/Academics/'15-'16/CS Thesis/results/of/testing.txt");

		String ofCorr = predict.correlation();
		String of = predict.predict();

		predict = new Predict("StanfordNLP",
				"C:/Users/pughck/Documents/Academics/'15-'16/CS Thesis/results/stanford/training.txt",
				"C:/Users/pughck/Documents/Academics/'15-'16/CS Thesis/results/stanford/testing.txt");

		String stanfordCorr = predict.correlation();
		String stanford = predict.predict();

		predict = new Predict("Apache OpenNLP",
				"C:/Users/pughck/Documents/Academics/'15-'16/CS Thesis/results/open/training.txt",
				"C:/Users/pughck/Documents/Academics/'15-'16/CS Thesis/results/open/testing.txt");

		String openCorr = predict.correlation();
		String open = predict.predict();

		Thread.sleep(100);

		System.out.println(ofCorr);
		System.out.println(of);
		System.out.println(stanfordCorr);
		System.out.println(stanford);
		System.out.println(openCorr);
		System.out.println(open);
	}
}
