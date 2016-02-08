package stocks;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class GenerateFileRunner {

	static final String[] compNames = { "aflac", "alibaba", "allstate", "amazon", "american airlines",
			"american express", "apple", "aramark", "astrazeneca", "at&t", "aviva", "bank of america",
			"berkshire hathaway", "best buy", "blackrock", "boeing", "canon", "capital one", "caterpillar", "chevron",
			"cisco", "citigroup", "coke", "comcast", "costco", "cummins", "delta", "disney", "ebay", "eil lilly",
			"exelon", "expedia", "exxon mobile", "facebook", "fedex", "ford", "gap", "general electric",
			"general motors", "goldman sachs", "google", "halliburton", "home depot", "honda", "honeywell", "intel",
			"jc penny", "john deere", "johnson & johnson", "jpmorgan chase", "kohls", "kroger", "linkedin",
			"lockheed martin", "lowes", "marathon", "mastercard", "mcdonalds", "metlife", "microsoft", "monsanto",
			"morgan stanley", "netflix", "nike", "nvidia", "oracle", "pepsi", "pfizer", "procter & gamble",
			"progressive", "prudential", "qualcomm", "raytheon", "rockwell collins", "staples", "starbucks", "target",
			"tesla", "time warner", "toyota", "travelers", "twitter", "valero", "verizon", "visa", "walmart",
			"wells fargo", "whirlpool", "whole foods", "yahoo" };

	static final String[] compSyms = { "AFL", "BABA", "ALL", "AMZN", "AAL", "AXP", "AAPL", "ARMK", "AZN", "T", "AV",
			"BAC", "BRK-A", "BBY", "BLK", "BA", "CAJ", "COF", "CAT", "CVX", "CSCO", "C", "KO", "CMCSA", "COST", "CMI",
			"DAL", "DIS", "EBAY", "LLY", "EXC", "EXPE", "XOM", "FB", "FDX", "F", "GPS", "GE", "GM", "GS", "GOOG", "HAL",
			"HD", "HMC", "HON", "INTC", "JCP", "DE", "JNJ", "JMP", "KSS", "KR", "LNKD", "LMT", "LOW", "MRO", "MA",
			"MCD", "MET", "MSFT", "MON", "MS", "NFLX", "NKE", "NVDA", "ORCL", "PEP", "PFE", "PG", "PGR", "PRU", "QCOM",
			"RTN", "COL", "SPLS", "SBUX", "TGT", "TSLA", "TWX", "TM", "TRV", "TWTR", "VLO", "VZ", "V", "WMT", "WFC",
			"WHR", "WFM", "YHOO" };

	static Map<String, String> compMap;
	static Map<String, Double> exchangesMap;

	public static void main(String[] args) {

		if (args.length != 2 && args.length != 5) {
			System.out.println("Invalid number of parameters!");

			return;
		}

		createCompMap();

		String inputFile = args[0].trim();
		String outputFile = args[1].trim();

		Calendar date = Calendar.getInstance();
		if (args.length == 5) {
			date.set(Integer.parseInt(args[2].trim()), Integer.parseInt(args[3].trim()),
					Integer.parseInt(args[4].trim()));
		}

		createExchangeMap(date);

		GenerateFile gtf = new GenerateFile(inputFile, outputFile, date);
		try {
			gtf.execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createCompMap() {

		compMap = new HashMap<>();

		for (int i = 0; i < compNames.length && i < compSyms.length; i++) {
			compMap.put(compNames[i], compSyms[i]);
		}
	}

	private static void createExchangeMap(Calendar date) {

		exchangesMap = new HashMap<>();

		Stock stock = YahooFinance.get("^IXIC");
		HistoricalQuote quote = stock.getHistory(date, date, Interval.DAILY).get(0);
		double open = quote.getOpen().doubleValue();
		double close = quote.getClose().doubleValue();
		double change = (close - open) / open;
		exchangesMap.put("NMS", change);

		stock = YahooFinance.get("^NYA");
		quote = stock.getHistory(date, date, Interval.DAILY).get(0);
		open = quote.getOpen().doubleValue();
		close = quote.getClose().doubleValue();
		change = (close - open) / open;
		exchangesMap.put("NYQ", change);
	}
}
