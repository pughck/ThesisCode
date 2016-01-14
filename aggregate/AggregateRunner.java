package aggregate;

public class AggregateRunner {

	public static void main(String[] args) {

		Thread stanford = new Thread(new Aggregate("/tmp/stormOutputSentiment/stanfordNLP/"));
		stanford.start();

		Thread openNLP = new Thread(new Aggregate("/tmp/stormOutputSentiment/openNLP/"));
		openNLP.start();
	}
}
