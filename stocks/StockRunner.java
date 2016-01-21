package stocks;

import java.math.BigDecimal;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class StockRunner {

	private static final String[] companies = { "ADS.DE", "AMZN", "DAL", "EXPE", "FB", "GOOG", "MCD", "MSFT", "NKE",
			"SBUX", "TGT", "TWTR", "WMT", "WEN", "YHOO" };

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
