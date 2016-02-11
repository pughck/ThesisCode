package stocks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class GenerateFile {

	private String inputFile;
	private String outputFile;

	private Calendar date;

	public GenerateFile(String inputFile, String outputFile, Calendar date) {

		this.inputFile = inputFile;
		this.outputFile = outputFile;

		this.date = date;
	}

	public void execute() throws IOException {

		Map<String, Stock> stocks = YahooFinance.get(GenerateFileRunner.compSyms);

		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));

		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] info = line.split("\t+");

			String company = info[0].trim();
			String symbol = GenerateFileRunner.compMap.get(company);
			if (symbol == null) {
				continue;
			}

			int positive = Integer.parseInt(info[1].trim());
			int neutral = Integer.parseInt(info[2].trim());
			int negative = Integer.parseInt(info[3].trim());
			int net = Integer.parseInt(info[4].trim());

			Stock stock = stocks.get(symbol);
			if (stock == null) {
				continue;
			}

			HistoricalQuote quote = stock.getHistory(this.date, this.date, Interval.DAILY).get(0);

			double open = quote.getOpen().doubleValue();
			double close = quote.getClose().doubleValue();
			double change = (close - open) / open;

			double exchangeChange = GenerateFileRunner.exchangesMap.get(stock.getStockExchange());

			double dif = change - exchangeChange;

			final String TAB = "\t", NEWLINE = "\n";
			StringBuilder builder = new StringBuilder().append(String.format("%.5f", dif)).append(TAB).append(positive)
					.append(TAB).append(neutral).append(TAB).append(negative).append(TAB).append(net).append(NEWLINE);

			writer.append(builder.toString());
		}

		reader.close();
		writer.close();
	}
}
