import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

public class TillManagement {

	// - MARK: Main

	public static void main(String[] args) {

		final Map<Integer, Integer> amountInTill = new HashMap<>();
		amountInTill.put(50, 5);
		amountInTill.put(20, 5);
		amountInTill.put(10, 6);
		amountInTill.put(5, 12);
		amountInTill.put(2, 10);
		amountInTill.put(1, 10);

		List<List<String>> items = new ArrayList<>();
		List<List<String>> amountPaid = new ArrayList<>();
		String line = "";

		try {
			BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
			while((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				items.add(Arrays.asList(values[0]));	
				amountPaid.add(Arrays.asList(values[1]));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		int[] total = getAmountsFromLists(items);
		int[][] paidWith = getAmountsFromArray(amountPaid);

		processTransactions(amountInTill, total, paidWith);
	}

	// - MARK: functions

	public static int[] getAmountsFromLists(List<List<String>> rows) {
		int[] sums = new int[rows.size()];

		String regExp = "\\d+";
		Pattern p = Pattern.compile(regExp);

		int index = 0;
		for (List<String> row : rows) {
			int sum = 0;
			for (String str : row) {
				Matcher matcher = p.matcher(str);
				while (matcher.find()) {
					int number = Integer.parseInt(matcher.group());
					sum += number;
				}
			}
			sums[index++] = sum;
		}
		return sums;
	}

	public static int[][] getAmountsFromArray(List<List<String>> rows) {

		String regExp = "(\\d+)";
		Pattern p = Pattern.compile(regExp);
		int[][] amounts = new int[rows.size()][];

		for (int i = 0; i < rows.size(); i++) {
			List<String> row = rows.get(i);
			List<Integer> numberList = new ArrayList<>();

			for (String str : row) {
				Matcher matcher = p.matcher(str);
				while (matcher.find()) {
					int number = Integer.parseInt(matcher.group());
					numberList.add(number);
				}
			}
			amounts[i] = numberList.stream().mapToInt(Integer::intValue).toArray();
		}
		return amounts;
	}

	public static int calculateTotalAmountInTill(Map<Integer, Integer> amountInTill) {

		int totalAmountInTill = 0;
		for (int denomination : amountInTill.keySet()) {
			totalAmountInTill += denomination * amountInTill.get(denomination);
		}
		return totalAmountInTill;
	}

	public static Map<Integer, Integer> updateTill(Map<Integer, Integer> amountInTill, int[] paidWith, int transactionAmount) {

		int totalPaid = 0;
		Map<Integer, Integer> changeBreakdown = new HashMap<>();

		for (int denomination : paidWith) {
			totalPaid += denomination;
		}

		int remainingChange = totalPaid - transactionAmount;

		for (int i = 0; i < paidWith.length && remainingChange > 0; i++) {
			int denomination = paidWith[i];
			int count = amountInTill.getOrDefault(denomination, 0);

			while (remainingChange >= denomination && count > 0) {
				remainingChange -= denomination;
				count--;
				changeBreakdown.put(denomination, changeBreakdown.getOrDefault(denomination, 0) + 1);
			}
			count++;
			amountInTill.put(denomination, count);
		}
		return changeBreakdown;
	}

	public static void processTransactions(Map<Integer, Integer> amountInTill, int[] transactions, int[][] paidWith) {

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));

			writer.write("Till Start, Transaction Total, Paid, Change Total, Change Breakdown");
			writer.newLine();

			int totalAmountInTill = calculateTotalAmountInTill(amountInTill);
			writer.write("R" + totalAmountInTill + ", ");

			for (int i = 0; i < transactions.length; i++) {
				totalAmountInTill += transactions[i];

				int transactionAmount = transactions[i];
				int[] paidWithCustomer = paidWith[i]; 

				int totalPaid = 0;
				for (int denomination : paidWithCustomer) {
					totalPaid += denomination;
				}

				int changeTotal = totalPaid - transactionAmount;

				Map<Integer, Integer> changeBreakdown = updateTill(amountInTill, paidWithCustomer, transactionAmount);

				writer.write("R" + transactionAmount + ", R" + totalPaid + ", R" + changeTotal + ", ");
				writer.write("R");
				for (int denomination : changeBreakdown.keySet()) {
					int count = changeBreakdown.get(denomination);
					for (int j = 0; j < count; j++) {
						writer.write(String.valueOf(denomination));
						if (j < count - 1) {
							writer.write("-");
						}
					}
					if (denomination < changeBreakdown.keySet().stream().mapToInt(Integer::intValue).max().orElse(0)) {
						writer.write("-");
					}
				}

				if (i < transactions.length - 1) {
					writer.newLine();
					writer.write("R" + totalAmountInTill + ", ");
				}
			}
			writer.newLine();
			writer.write("R" + totalAmountInTill); 
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}



