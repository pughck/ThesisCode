package stocks;

import java.util.HashMap;
import java.util.Map;

public class Constants {

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
			"WHR", "WFM", "YHOO", "FCAU", "LUV", "UAL" };

	static final Map<String, String> compMap = new HashMap<>();
	static final Map<String, String[]> relatedMap = new HashMap<>();

	static {

		for (int i = 0; i < compNames.length && i < compSyms.length; i++) {
			compMap.put(compNames[i], compSyms[i]);
		}

		relatedMap.put("FB", new String[] { "LNKD", "TWTR" });
		relatedMap.put("LNKD", new String[] { "FB", "TWTR" });
		relatedMap.put("TWTR", new String[] { "FB", "LNKD" });

		relatedMap.put("F", new String[] { "GM", "HON", "TM", "FCAU" });
		relatedMap.put("GM", new String[] { "F", "HON", "TM", "FCAU" });
		relatedMap.put("HON", new String[] { "F", "GM", "TM", "FCAU" });
		relatedMap.put("TM", new String[] { "F", "GM", "HON", "FCAU" });

		relatedMap.put("AAPL", new String[] { "CSCO", "GOOG", "INTC", "MSFT", "NVDA", "YHOO" });
		relatedMap.put("CSCO", new String[] { "AAPL", "GOOG", "INTC", "MSFT", "NVDA", "YHOO" });
		relatedMap.put("GOOG", new String[] { "AAPL", "CSCO", "INTC", "MSFT", "NVDA", "YHOO" });
		relatedMap.put("INTC", new String[] { "AAPL", "CSCO", "GOOG", "MSFT", "NVDA", "YHOO" });
		relatedMap.put("MSFT", new String[] { "AAPL", "CSCO", "GOOG", "INTC", "NVDA", "YHOO" });
		relatedMap.put("NVDA", new String[] { "AAPL", "CSCO", "GOOG", "INTC", "MSFT", "YHOO" });
		relatedMap.put("YHOO", new String[] { "AAPL", "CSCO", "GOOG", "INTC", "MSFT", "NVDA" });

		relatedMap.put("AFL", new String[] { "ALL", "AV", "MET", "PGR", "PRU", "TRV" });
		relatedMap.put("ALL", new String[] { "AFL", "AV", "MET", "PGR", "PRU", "TRV" });
		relatedMap.put("AV", new String[] { "AFL", "ALL", "MET", "PGR", "PRU", "TRV" });
		relatedMap.put("MET", new String[] { "AFL", "ALL", "AV", "PGR", "PRU", "TRV" });
		relatedMap.put("PGR", new String[] { "AFL", "ALL", "AV", "MET", "PRU", "TRV" });
		relatedMap.put("PRU", new String[] { "AFL", "ALL", "AV", "MET", "PGR", "TRV" });
		relatedMap.put("TRV", new String[] { "AFL", "ALL", "AV", "MET", "PGR", "PRU" });

		relatedMap.put("AAL", new String[] { "DAL", "LUV", "UAL" });
		relatedMap.put("DAL", new String[] { "AAL", "LUV", "UAL" });

		relatedMap.put("BAC", new String[] { "BLK", "COF", "C", "GS", "JMP", "MA", "WFC" });
		relatedMap.put("BLK", new String[] { "BAC", "COF", "C", "GS", "JMP", "MA", "WFC" });
		relatedMap.put("COF", new String[] { "BAC", "BLK", "C", "GS", "JMP", "MA", "WFC" });
		relatedMap.put("C", new String[] { "BAC", "BLK", "COF", "GS", "JMP", "MA", "WFC" });
		relatedMap.put("GS", new String[] { "BAC", "BLK", "COF", "C", "JMP", "MA", "WFC" });
		relatedMap.put("JMP", new String[] { "BAC", "BLK", "COF", "C", "GS", "MA", "WFC" });
		relatedMap.put("MA", new String[] { "BAC", "BLK", "COF", "C", "GS", "JMP", "WFC" });
		relatedMap.put("WFC", new String[] { "BAC", "BLK", "COF", "C", "GS", "JMP", "MA" });

		relatedMap.put("BA", new String[] { "CAT", "CMI", "LLY", "HON", "DE", "LMT", "COL", "TSLA", "WHR" });
		relatedMap.put("CAT", new String[] { "BA", "CMI", "LLY", "HON", "DE", "LMT", "COL", "TSLA", "WHR" });
		relatedMap.put("CMI", new String[] { "BA", "CAT", "LLY", "HON", "DE", "LMT", "COL", "TSLA", "WHR" });
		relatedMap.put("LLY", new String[] { "BA", "CAT", "CMI", "HON", "DE", "LMT", "COL", "TSLA", "WHR" });
		relatedMap.put("HON", new String[] { "BA", "CAT", "CMI", "LLY", "DE", "LMT", "COL", "TSLA", "WHR" });
		relatedMap.put("DE", new String[] { "BA", "CAT", "CMI", "LLY", "HON", "LMT", "COL", "TSLA", "WHR" });
		relatedMap.put("LMT", new String[] { "BA", "CAT", "CMI", "LLY", "HON", "DE", "COL", "TSLA", "WHR" });
		relatedMap.put("COL", new String[] { "BA", "CAT", "CMI", "LLY", "HON", "DE", "LMT", "TSLA", "WHR" });
		relatedMap.put("TSLA", new String[] { "BA", "CAT", "CMI", "LLY", "HON", "DE", "LMT", "COL", "WHR" });
		relatedMap.put("WHR", new String[] { "BA", "CAT", "CMI", "LLY", "HON", "DE", "LMT", "COL", "TSLA" });

		relatedMap.put("CVX", new String[] { "EXC", "XOM", "GE", "HAL", "VLO" });
		relatedMap.put("EXC", new String[] { "CVX", "XOM", "GE", "HAL", "VLO" });
		relatedMap.put("XOM", new String[] { "CVX", "EXC", "GE", "HAL", "VLO" });
		relatedMap.put("GE", new String[] { "CVX", "EXC", "XOM", "HAL", "VLO" });
		relatedMap.put("HAL", new String[] { "CVX", "EXC", "XOM", "GE", "VLO" });
		relatedMap.put("VLO", new String[] { "CVX", "EXC", "XOM", "GE", "HAL" });

		relatedMap.put("COST", new String[] { "JNJ", "PG", "TGT", "WMT" });
		relatedMap.put("JNJ", new String[] { "COST", "PG", "TGT", "WMT" });
		relatedMap.put("PG", new String[] { "COST", "JNJ", "TGT", "WMT" });
		relatedMap.put("TGT", new String[] { "COST", "JNJ", "PG", "WMT" });
		relatedMap.put("WMT", new String[] { "COST", "JNJ", "PG", "TGT" });

		relatedMap.put("T", new String[] { "CMCSA", "TWX", "VZ" });
		relatedMap.put("CMCSA", new String[] { "T", "TWX", "VZ" });
		relatedMap.put("TWX", new String[] { "T", "CMCSA", "VZ" });
		relatedMap.put("VZ", new String[] { "T", "CMCSA", "TWx" });

		relatedMap.put("GPS", new String[] { "JCP", "KSS", "NKE" });
		relatedMap.put("JCP", new String[] { "GPS", "KSS", "NKE" });
		relatedMap.put("KSS", new String[] { "GPS", "JCP", "NKE" });
		relatedMap.put("NKE", new String[] { "GPS", "JCP", "KSS" });
	}
}
