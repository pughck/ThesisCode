package stocks;

import java.math.BigDecimal;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class StockRunner {

	private static final String[] companies = { "AFL", "BABA", "ALL", "AMZN", "AAL", "AXP", "AAPL", "ARMK", "AZN", "T",
			"AV", "BAC", "BRK.A", "BBY", "BLK", "BA", "CAJ", "COF", "CAT", "CVX", "CSCO", "C", "KO", "CMCSA", "COST",
			"CMI", "DAL", "DIS", "EBAY", "LLY", "EXC", "EXPE", "XOM", "FB", "FDX", "F", "GPS", "GE", "GM", "GS", "GOOG",
			"HAL", "HD", "HMC", "HON", "INTC", "JCP", "DE", "JNJ", "JMP", "KSS", "KR", "LNKD", "LMT", "LOW", "MRO",
			"MA", "MCD", "MET", "MSFT", "MON", "MS", "NFLX", "NKE", "NVDA", "ORCL", "PEP", "PFE", "PG", "PGR", "PRU",
			"QCOM", "RTN", "COL", "SPLS", "SBUX", "TGT", "TSLA", "TWX", "TM", "TRV", "TWTR", "VLO", "VZ", "V", "WMT",
			"WFC", "WHR", "WFM", "YHOO" };

	public static void main(String[] args) {

		Stock stock = YahooFinance.get("^IXIC");
		printInfo(stock);

		System.out.println("\n\n");

		Map<String, Stock> stocks = YahooFinance.get(companies);

		for (String company : companies) {
			Stock compStock = stocks.get(company);

			printInfo(compStock);

			System.out.println("\n\n");
		}
	}

	private static void printInfo(Stock stock) {

		BigDecimal price = stock.getQuote().getPrice();
		BigDecimal change = stock.getQuote().getChangeInPercent();

		System.out.println(price + "\t" + change + "\n");

		stock.print();
	}
}
