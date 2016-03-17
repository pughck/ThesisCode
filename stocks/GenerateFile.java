package stocks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class GenerateFile {

	private Map<String, Stock> stocks;

	private Map<String, Double> exchangesMap;
	private Map<String, Double> relatedDifMap;

	private String inputFile;

	private String outputFile;
	private String basicOutputFile;
	private String relatedOutputFile;

	private Calendar date;

	private boolean training;
	private int totalMin;

	public GenerateFile(String inputFile, String outputFile, Calendar date, boolean training, int totalMin) {

		this.inputFile = inputFile;

		this.outputFile = outputFile;

		this.basicOutputFile = this.outputFile.split("\\.")[0] + "-basic.txt";

		this.relatedOutputFile = this.outputFile.split("\\.")[0] + "-related.txt";

		this.date = date;

		this.stocks = YahooFinance.get(Constants.compSyms);

		createExchangeMap(this.date);

		createDifMap(this.date);

		this.training = training;
		this.totalMin = totalMin;
	}

	private void createExchangeMap(Calendar date) {

		this.exchangesMap = new HashMap<>();

		Stock stock = YahooFinance.get("^IXIC");
		HistoricalQuote quote = stock.getHistory(date, date, Interval.DAILY).get(0);
		double open = quote.getOpen().doubleValue();
		double close = quote.getClose().doubleValue();
		double change = (close - open) / open;
		this.exchangesMap.put("NMS", change);

		stock = YahooFinance.get("^NYA");
		quote = stock.getHistory(date, date, Interval.DAILY).get(0);
		open = quote.getOpen().doubleValue();
		close = quote.getClose().doubleValue();
		change = (close - open) / open;
		this.exchangesMap.put("NYQ", change);
	}

	private void createDifMap(Calendar date) {

		this.relatedDifMap = new HashMap<>();

		for (String comp : Constants.relatedMap.keySet()) {
			String[] related = Constants.relatedMap.get(comp);

			double total = 0;

			for (String relatedComp : related) {
				Stock stock = this.stocks.get(relatedComp);
				if (stock == null) {
					continue;
				}

				HistoricalQuote quote = stock.getHistory(this.date, this.date, Interval.DAILY).get(0);
				double open = quote.getOpen().doubleValue();
				double close = quote.getClose().doubleValue();
				double change = (close - open) / open;

				total += change;
			}

			this.relatedDifMap.put(comp, total / related.length);
		}
	}

	public void execute() throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(this.outputFile, this.training));
		BufferedWriter basicWriter = new BufferedWriter(new FileWriter(this.basicOutputFile, this.training));
		BufferedWriter relatedWriter = new BufferedWriter(new FileWriter(this.relatedOutputFile, this.training));

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));

		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] info = line.split("\t+");

			String company = info[0].trim();
			String symbol = Constants.compMap.get(company);
			if (symbol == null) {
				continue;
			}

			int positive = Integer.parseInt(info[1].trim());
			int neutral = Integer.parseInt(info[2].trim());
			int negative = Integer.parseInt(info[3].trim());
			int net = Integer.parseInt(info[4].trim());

			int total = positive + neutral + negative;
			if (total < this.totalMin || total == 0) {
				continue;
			}

			Stock stock = this.stocks.get(symbol);
			if (stock == null) {
				continue;
			}

			HistoricalQuote quote = stock.getHistory(this.date, this.date, Interval.DAILY).get(0);

			double open = quote.getOpen().doubleValue();
			double close = quote.getClose().doubleValue();
			double change = (close - open) / open;

			double exchangeChange = this.exchangesMap.get(stock.getStockExchange());

			double dif = change - exchangeChange;

			final String TAB = "\t", NEWLINE = "\n";
			StringBuilder builder = new StringBuilder().append(String.format("%.5f", dif)).append(TAB).append(positive)
					.append(TAB).append(neutral).append(TAB).append(negative).append(TAB).append(net).append(NEWLINE);
			writer.append(builder.toString());

			builder = new StringBuilder().append(String.format("%.5f", change)).append(TAB).append(positive).append(TAB)
					.append(neutral).append(TAB).append(negative).append(TAB).append(net).append(NEWLINE);
			basicWriter.append(builder.toString());

			if (this.relatedDifMap.containsKey(symbol)) {
				dif = change - this.relatedDifMap.get(symbol);

				builder = new StringBuilder().append(String.format("%.5f", dif)).append(TAB).append(positive)
						.append(TAB).append(neutral).append(TAB).append(negative).append(TAB).append(net)
						.append(NEWLINE);
				relatedWriter.append(builder.toString());
			}
		}

		reader.close();

		writer.close();
		basicWriter.close();
		relatedWriter.close();
	}
}
