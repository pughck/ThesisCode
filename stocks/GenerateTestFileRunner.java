package stocks;

import java.io.IOException;
import java.util.Calendar;

public class GenerateTestFileRunner {

	public static void main(String[] args) {

		if (args.length < 2) {
			System.err.println("Need at least totalMin arg and one date arg");

			return;
		}

		int totalMin = Integer.parseInt(args[0].trim());

		final String basePath = "C:/Users/pughck/Documents/Academics/'15-'16/CS Thesis/results/";

		for (int i = 1; i < args.length; i++) {
			String[] dateSplit = args[i].split("-");
			int year = Integer.parseInt(dateSplit[2].trim());
			int month = Integer.parseInt(dateSplit[0].trim()) - 1;
			int day = Integer.parseInt(dateSplit[1].trim());

			Calendar date = Calendar.getInstance();
			date.set(year, month, day);

			String inputFile = basePath + "stanford/" + args[i] + "/sentimentResults.txt";
			String outputFile = basePath + "stanford/" + args[i] + "/testing-" + totalMin + ".txt";

			GenerateFile gf = new GenerateFile(inputFile, outputFile, date, false, totalMin);
			try {
				gf.execute();
			} catch (IOException e) {
				e.printStackTrace();
			}

			inputFile = basePath + "open/" + args[i] + "/sentimentResults.txt";
			outputFile = basePath + "open/" + args[i] + "/testing-" + totalMin + ".txt";

			gf = new GenerateFile(inputFile, outputFile, date, false, totalMin);
			try {
				gf.execute();
			} catch (IOException e) {
				e.printStackTrace();
			}

			inputFile = basePath + "of/" + args[i] + "/sentimentResults.txt";
			outputFile = basePath + "of/" + args[i] + "/testing-" + totalMin + ".txt";

			gf = new GenerateFile(inputFile, outputFile, date, false, totalMin);
			try {
				gf.execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
