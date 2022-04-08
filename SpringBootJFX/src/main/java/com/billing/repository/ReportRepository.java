package com.billing.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.billing.constants.AppConstants;
import com.billing.dto.CashReport;
import com.billing.dto.ConsolidatedReport;
import com.billing.dto.Customer;
import com.billing.dto.GraphDTO;
import com.billing.dto.ProfitLossData;
import com.billing.dto.ProfitLossDetails;
import com.billing.dto.StatusDTO;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Repository
public class ReportRepository {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	private static final Logger logger = LoggerFactory.getLogger(ReportRepository.class);

	private static final String GET_TOTAL_SALES_CASH_AMOUNT = "SELECT SUM(NET_SALES_AMOUNT) AS TOTAL_SALES_CASH_AMOUNT FROM CUSTOMER_BILL_DETAILS WHERE "
			+ "DATE(BILL_DATE_TIME) BETWEEN ? AND ? AND PAYMENT_MODE NOT IN ('PENDING')";

	private static final String GET_TOTAL_SALES_PENDING_AMOUNT = "SELECT SUM(NET_SALES_AMOUNT) AS TOTAL_SALES_PENDING_AMOUNT FROM CUSTOMER_BILL_DETAILS WHERE "
			+ "DATE(BILL_DATE_TIME) BETWEEN ? AND ? AND PAYMENT_MODE IN ('PENDING')";

	private static final String GET_TOTAL_SALES_RETURN_AMOUNT = "SELECT SUM(RETURN_TOTAL_AMOUNT) AS TOTAL_SALES_RETURN_AMOUNT FROM SALES_RETURN_DETAILS WHERE "
			+ "DATE(RETURN_DATE) BETWEEN ? AND ? AND PAYMENT_MODE NOT IN ('PENDING')";

	private static final String GET_TOTAL_SALES_RETURN_AMOUNT_REPORT = "SELECT SUM(RETURN_TOTAL_AMOUNT) AS TOTAL_SALES_RETURN_AMOUNT FROM SALES_RETURN_DETAILS WHERE "
			+ "DATE(RETURN_DATE) BETWEEN ? AND ?";

	private static final String GET_TOTAL_CUST_SETTLEMENT_AMOUNT = "SELECT SUM(DEBIT) AS TOTAL_CUST_SETTLEMENT_AMOUNT FROM CUSTOMER_PAYMENT_HISTORY WHERE "
			+ "DATE(TIMESTAMP) BETWEEN ? AND ? AND STATUS='DEBIT' AND NARRATION LIKE '%Settled up%'";

	private static final String GET_OPENING_CASH = "SELECT * FROM CASH_COUNTER WHERE DATE=?";

	private static final String GET_TOTAL_EXPENSES_AMOUNT = "SELECT SUM(AMOUNT) AS TOTAL_EXPENSE_AMOUNT FROM EXPENSE_DETAILS WHERE "
			+ "DATE(DATE) BETWEEN ? AND ?";

	private static final String GET_TOTAL_PURCHASE_AMOUNT = "SELECT SUM(TOTAL_AMOUNT) AS TOTAL_PURCHASE_AMOUNT FROM PURCHASE_ENTRY_DETAILS WHERE "
			+ "DATE(PURCHASE_ENTRY_DATE) BETWEEN ? AND ? ";

	private static final String GET_TOTAL_QTY_SOLD = "SELECT SUM(BILL_QUANTITY) AS TOTAL_QTY_SOLD FROM CUSTOMER_BILL_DETAILS WHERE "
			+ "DATE(BILL_DATE_TIME) BETWEEN ? AND ? ";

	private static final String GET_TOTAL_QTY_RETURNED = "SELECT SUM(TOTAL_QTY) AS TOTAL_QTY_RETURNED FROM SALES_RETURN_DETAILS WHERE "
			+ "DATE(RETURN_DATE) BETWEEN ? AND ? ";

	private static final String GET_OPENING_STOCK_VALUE = "SELECT * FROM OPENING_STOCK_VALUE WHERE DATE=?";

	private static final String GET_TOTAL_SALES_AMOUNT = "SELECT SUM(NET_SALES_AMOUNT) AS TOTAL_SALES_AMOUNT FROM CUSTOMER_BILL_DETAILS WHERE "
			+ "DATE(BILL_DATE_TIME) BETWEEN ? AND ?";

	private static final String INS_OPENING_STOCK_VALUE = "INSERT INTO OPENING_STOCK_VALUE (DATE,AMOUNT) VALUES(?,?)";

	private static final String GET_CLOSING_STOCK_VALUE = "SELECT SUM(QUANTITY*PURCHASE_PRICE) AS CLOSING_STOCK_VALUE FROM PRODUCT_DETAILS";

	private static final String SETTLEMENT_CUST_INFO = "SELECT CD.CUST_MOB_NO,CD.CUST_NAME,CSH.DEBIT FROM CUSTOMER_PAYMENT_HISTORY CSH,CUSTOMER_DETAILS CD WHERE DATE(CSH.TIMESTAMP) BETWEEN ? AND ? AND CSH.STATUS='DEBIT' "
			+ "AND CSH.CUST_ID=CD.CUST_ID AND NARRATION LIKE '%Settled up%'";

	private static final String GET_PROFIT_AMT = "SELECT  SUM(NET_SALES_AMOUNT-BILL_PURCHASE_AMT) AS PROFIT FROM CUSTOMER_BILL_DETAILS  WHERE DATE(BILL_DATE_TIME) BETWEEN ? AND ?";

	private static final String GET_SALES_RETURN_PROFIT_AMT = "SELECT  SUM(RETURN_TOTAL_AMOUNT-RETURN_PURCHASE_AMT) AS NEGATIVE_PROFIT FROM SALES_RETURN_DETAILS  WHERE DATE(RETURN_DATE) BETWEEN ? AND ? AND RETURN_PURCHASE_AMT!=0";

	private static final String GET_TOTAL_EXP = "SELECT ED.CATEGORY,ED.AMOUNT FROM EXPENSE_DETAILS ED,APP_EXPENSE_TYPES ET WHERE DATE(DATE) BETWEEN ? and ? and ED.CATEGORY=ET.NAME AND ET.TYPE !='SAVINGS';";

	private static final String INS_OPENING_CASH = "INSERT INTO CASH_COUNTER " + "(DATE,AMOUNT)" + " VALUES(?,?)";

	private static final String UPDATE_OPENING_CASH = "UPDATE CASH_COUNTER SET AMOUNT=?" + " WHERE DATE=?";

	private static final String GET_PURCHASE_ENTRY_DISC_EXTRA_CHARGES_AMT = "SELECT  SUM(DISCOUNT_AMOUNT) AS DISCOUNT_AMOUNT , SUM(EXTRA_CHARGES) AS EXTRA_CHARGES FROM PURCHASE_ENTRY_DETAILS  WHERE DATE(PURCHASE_ENTRY_DATE) BETWEEN ? AND ? ";

	// Graphic Report

	private static final String PAYMENT_MODE_AMOUNT = "SELECT PAYMENT_MODE,SUM(NET_SALES_AMOUNT) AS TOTAL_AMT FROM CUSTOMER_BILL_DETAILS WHERE "
			+ "DATE(BILL_DATE_TIME) BETWEEN ? AND ?  GROUP BY PAYMENT_MODE ORDER BY SUM(NET_SALES_AMOUNT) DESC";

	private static final String DAILY_SALES_AMOUNT_REPORT = "SELECT STRFTIME('%d-%m-%Y',BILL_DATE_TIME) AS BILL_DATE,SUM(NET_SALES_AMOUNT) AS TOTAL_AMT,SUM(BILL_PURCHASE_AMT) AS TOTAL_PUR_AMT,COUNT(BILL_NUMBER) AS NO_OF_INVOICES FROM CUSTOMER_BILL_DETAILS "
			+ "GROUP BY STRFTIME('%d-%m-%Y',BILL_DATE_TIME) ORDER BY BILL_DATE_TIME DESC ;";

	private static final String MONTHLY_SALES_AMOUNT_REPORT = "SELECT STRFTIME('%m-%Y',BILL_DATE_TIME) AS BILL_DATE,SUM(NET_SALES_AMOUNT) AS TOTAL_AMT,SUM(BILL_PURCHASE_AMT) AS TOTAL_PUR_AMT FROM CUSTOMER_BILL_DETAILS "
			+ "GROUP BY STRFTIME('%m-%Y',BILL_DATE_TIME) ORDER BY BILL_DATE_TIME DESC ;";

	// Add Opening Cash
	public StatusDTO addOpeningCash(double amount) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(INS_OPENING_CASH);
			stmt.setString(1, appUtils.getTodaysDateForDB());
			stmt.setDouble(2, amount);

			int i = stmt.executeUpdate();
			if (i > 0) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	// Update Opening Cash
	public StatusDTO updateOpeningCash(double amount, String date) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(UPDATE_OPENING_CASH);
			stmt.setDouble(1, amount);
			stmt.setString(2, date);

			int i = stmt.executeUpdate();
			if (i > 0) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	// Get Total Amount of Sales except pending bills
	public List<CashReport> getCashCounterDetails(String fromDate, String toDate) {
		Connection conn = null;
		List<CashReport> cashCounterList = new ArrayList<CashReport>();
		Double closingBalance = 0.00;

		try {
			conn = dbUtils.getConnection();
			// Opening Cash
			PreparedStatement stmt = conn.prepareStatement(GET_OPENING_CASH);
			stmt.setString(1, fromDate);
			ResultSet rsOC = stmt.executeQuery();
			if (rsOC.next()) {
				CashReport cashTotalSales = new CashReport();
				cashTotalSales.setDescription("OPENING_CASH");
				cashTotalSales.setCreditAmount(rsOC.getDouble("AMOUNT"));
				closingBalance = rsOC.getDouble("AMOUNT");
				cashTotalSales.setClosingBalance(closingBalance);
				cashCounterList.add(cashTotalSales);
			}
			stmt.close();
			// Total Sales Amount
			PreparedStatement stmt2 = conn.prepareStatement(GET_TOTAL_SALES_CASH_AMOUNT);
			stmt2.setString(1, fromDate);
			stmt2.setString(2, toDate);
			ResultSet rs = stmt2.executeQuery();
			if (rs.next()) {
				CashReport cashTotalSales = new CashReport();
				cashTotalSales.setDescription("TOTAL_SALES_AMOUNT");
				cashTotalSales.setCreditAmount(rs.getDouble("TOTAL_SALES_CASH_AMOUNT"));
				closingBalance += rs.getDouble("TOTAL_SALES_CASH_AMOUNT");
				cashTotalSales.setClosingBalance(closingBalance);
				cashCounterList.add(cashTotalSales);
			}
			stmt2.close();
			// Total Sales Return Amount
			PreparedStatement stmt3 = conn.prepareStatement(GET_TOTAL_SALES_RETURN_AMOUNT);
			stmt3.setString(1, fromDate);
			stmt3.setString(2, toDate);
			ResultSet rs2 = stmt3.executeQuery();
			if (rs2.next()) {
				CashReport cashTotalSalesReturn = new CashReport();
				cashTotalSalesReturn.setDescription("TOTAL_SALES_RETURN_AMOUNT");
				cashTotalSalesReturn.setDebitAmount(rs2.getDouble("TOTAL_SALES_RETURN_AMOUNT"));
				cashTotalSalesReturn.setClosingBalance(closingBalance - rs2.getDouble("TOTAL_SALES_RETURN_AMOUNT"));
				closingBalance -= rs2.getDouble("TOTAL_SALES_RETURN_AMOUNT");
				cashCounterList.add(cashTotalSalesReturn);
			}
			stmt3.close();
			// Total Expenses Amount
			PreparedStatement stmt4 = conn.prepareStatement(GET_TOTAL_EXPENSES_AMOUNT);
			stmt4.setString(1, fromDate);
			stmt4.setString(2, toDate);
			ResultSet rs3 = stmt4.executeQuery();
			if (rs3.next()) {
				CashReport cashTotalExpense = new CashReport();
				cashTotalExpense.setDescription("TOTAL_EXPENSE_AMOUNT");
				cashTotalExpense.setDebitAmount(rs3.getDouble("TOTAL_EXPENSE_AMOUNT"));
				cashTotalExpense.setClosingBalance(closingBalance - rs3.getDouble("TOTAL_EXPENSE_AMOUNT"));
				closingBalance -= rs3.getDouble("TOTAL_EXPENSE_AMOUNT");
				cashCounterList.add(cashTotalExpense);
			}
			stmt4.close();
			// Total Customer Settlement Amount
			PreparedStatement stmt5 = conn.prepareStatement(GET_TOTAL_CUST_SETTLEMENT_AMOUNT);
			stmt5.setString(1, fromDate);
			stmt5.setString(2, toDate);
			ResultSet rs4 = stmt5.executeQuery();
			if (rs4.next()) {
				CashReport cashTotalCustSettlement = new CashReport();
				cashTotalCustSettlement.setDescription("TOTAL_CUST_SETTLEMENT_AMOUNT");
				cashTotalCustSettlement.setCreditAmount(rs4.getDouble("TOTAL_CUST_SETTLEMENT_AMOUNT"));
				cashTotalCustSettlement
						.setClosingBalance(closingBalance + rs4.getDouble("TOTAL_CUST_SETTLEMENT_AMOUNT"));
				closingBalance += rs4.getDouble("TOTAL_CUST_SETTLEMENT_AMOUNT");
				cashCounterList.add(cashTotalCustSettlement);
			}
			stmt5.close();
			CashReport total = new CashReport();
			total.setDescription("TOTAL");
			double totalCredit = 0;
			double totalDebit = 0;
			for (CashReport cash : cashCounterList) {
				if (cash.getCreditAmount() > 0) {
					totalCredit += cash.getCreditAmount();
				}
				if (cash.getDebitAmount() > 0) {
					totalDebit += cash.getDebitAmount();
				}
			}
			total.setCreditAmount(totalCredit);
			total.setDebitAmount(totalDebit);
			total.setClosingBalance(totalCredit - totalDebit);
			cashCounterList.add(total);

			// System.out.println(cashCounterList);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(null, conn);
		}
		return cashCounterList;
	}

	// Get Opening Cash
	public Double getOpeningCash(String date) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Double openingCashAmount = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_OPENING_CASH);
			stmt.setString(1, date);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				openingCashAmount = rs.getDouble("AMOUNT");
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return openingCashAmount;
	}

	// Get Monthly Report
	public ConsolidatedReport getConsolidatedReport(String fromDate, String toDate) {

		ConsolidatedReport report = new ConsolidatedReport();
		Connection conn = null;
		int totalQtySold = 0;
		conn = dbUtils.getConnection();
		try {
			// Total Sales Pending Amount
			PreparedStatement stmt = conn.prepareStatement(GET_TOTAL_SALES_CASH_AMOUNT);
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
			ResultSet rs0 = stmt.executeQuery();
			if (rs0.next()) {
				report.setTotalSalesCashAmt(rs0.getDouble("TOTAL_SALES_CASH_AMOUNT"));
			}
			stmt.close();
			// Total Sales Cash Amount
			PreparedStatement stmt2 = conn.prepareStatement(GET_TOTAL_SALES_PENDING_AMOUNT);
			stmt2.setString(1, fromDate);
			stmt2.setString(2, toDate);
			ResultSet rs = stmt2.executeQuery();
			if (rs.next()) {
				report.setTotalSalesPendingAmt(rs.getDouble("TOTAL_SALES_PENDING_AMOUNT"));
			}
			stmt2.close();
			// Total Sales Return Amount
			PreparedStatement stmt3 = conn.prepareStatement(GET_TOTAL_SALES_RETURN_AMOUNT_REPORT);
			stmt3.setString(1, fromDate);
			stmt3.setString(2, toDate);
			ResultSet rs2 = stmt3.executeQuery();
			if (rs2.next()) {
				report.setTotalSalesReturnAmt(rs2.getDouble("TOTAL_SALES_RETURN_AMOUNT"));
			}
			stmt3.close();
			// Total Expenses Amount
			PreparedStatement stmt4 = conn.prepareStatement(GET_TOTAL_EXPENSES_AMOUNT);
			stmt4.setString(1, fromDate);
			stmt4.setString(2, toDate);
			ResultSet rs3 = stmt4.executeQuery();
			if (rs3.next()) {
				report.setTotalExpensesAmt(rs3.getDouble("TOTAL_EXPENSE_AMOUNT"));
			}
			stmt4.close();
			// Total Customer Settlement Amount
			PreparedStatement stmt5 = conn.prepareStatement(GET_TOTAL_CUST_SETTLEMENT_AMOUNT);
			stmt5.setString(1, fromDate);
			stmt5.setString(2, toDate);
			ResultSet rs4 = stmt5.executeQuery();
			if (rs4.next()) {
				report.setTotalCustSettlementAmt(rs4.getDouble("TOTAL_CUST_SETTLEMENT_AMOUNT"));
			}
			stmt5.close();
			// Total Purchase Amount
			PreparedStatement stmt6 = conn.prepareStatement(GET_TOTAL_PURCHASE_AMOUNT);
			stmt6.setString(1, fromDate);
			stmt6.setString(2, toDate);
			ResultSet rs5 = stmt6.executeQuery();
			if (rs5.next()) {
				report.setTotalPurchaseAmt(rs5.getDouble("TOTAL_PURCHASE_AMOUNT"));
			}
			stmt6.close();
			// Total Purchase Amount
			PreparedStatement stmt7 = conn.prepareStatement(GET_TOTAL_PURCHASE_AMOUNT);
			stmt7.setString(1, fromDate);
			stmt7.setString(2, toDate);
			ResultSet rs6 = stmt7.executeQuery();
			if (rs6.next()) {
				report.setTotalPurchaseAmt(rs6.getDouble("TOTAL_PURCHASE_AMOUNT"));
			}
			stmt7.close();
			// Total Quantity sold
			PreparedStatement stmt8 = conn.prepareStatement(GET_TOTAL_QTY_SOLD);
			stmt8.setString(1, fromDate);
			stmt8.setString(2, toDate);
			ResultSet rs7 = stmt8.executeQuery();
			if (rs7.next()) {
				totalQtySold = rs7.getInt("TOTAL_QTY_SOLD");
			}
			stmt8.close();
			// Total Quantity returned
			PreparedStatement stmt9 = conn.prepareStatement(GET_TOTAL_QTY_RETURNED);
			stmt9.setString(1, fromDate);
			stmt9.setString(2, toDate);
			ResultSet rs8 = stmt9.executeQuery();
			if (rs8.next()) {
				report.setTotalQtySold(totalQtySold - rs8.getInt("TOTAL_QTY_RETURNED"));
			}
			stmt9.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(null, conn);
		}
		System.out.println(report);
		return report;

	}

	// Get Profit Loss Statement

	/*
	 * public ProfitLossDetails getProfitLossStatment(String fromDate, String
	 * toDate) {
	 * 
	 * ProfitLossDetails report = new ProfitLossDetails(); Connection conn = null;
	 * PreparedStatement stmt = null; conn = dbUtils.getConnection(); double
	 * openingStockAmt = 0.0; double salesAmt = 0.0; double closingStockAmt = 0.0;
	 * double purchasesAmt = 0.0; double expensesAmt = 0.0; double totalDebit = 0.0;
	 * double totalCredit = 0.0;
	 * 
	 * try { System.out.println(fromDate + "  " + toDate); // Debits
	 * List<ProfitLossData> debit = new ArrayList<ProfitLossData>(); // Opening
	 * Stock Amount ProfitLossData openingStockValue = new ProfitLossData();
	 * openingStockValue.setDescription(AppConstants.OPENING_STOCK); openingStockAmt
	 * = getOpeningStockValue(fromDate); if (openingStockAmt == 0) { return null; }
	 * openingStockValue.setAmount(openingStockAmt); debit.add(openingStockValue);
	 * // Expenses ProfitLossData expenses = new ProfitLossData();
	 * expenses.setDescription(AppConstants.EXPESES); stmt =
	 * conn.prepareStatement(GET_TOTAL_EXPENSES_AMOUNT); stmt.setString(1,
	 * fromDate); stmt.setString(2, toDate); ResultSet rs3 = stmt.executeQuery(); if
	 * (rs3.next()) { expensesAmt = rs3.getDouble("TOTAL_EXPENSE_AMOUNT");
	 * expenses.setAmount(expensesAmt); } debit.add(expenses);
	 * 
	 * // Purchases ProfitLossData purchases = new ProfitLossData();
	 * purchases.setDescription(AppConstants.PURCHASES);
	 * 
	 * stmt = conn.prepareStatement(GET_TOTAL_PURCHASE_AMOUNT); stmt.setString(1,
	 * fromDate); stmt.setString(2, toDate); ResultSet rs5 = stmt.executeQuery(); if
	 * (rs5.next()) { purchasesAmt = rs5.getDouble("TOTAL_PURCHASE_AMOUNT");
	 * purchases.setAmount(purchasesAmt); } debit.add(purchases);
	 * report.setDebit(debit); // Credits List<ProfitLossData> credit = new
	 * ArrayList<ProfitLossData>(); // Sales ProfitLossData sales = new
	 * ProfitLossData(); double salesAmount = 0.0; double salesReturnAmt = 0.0;
	 * sales.setDescription(AppConstants.SALES_REPORT); stmt =
	 * conn.prepareStatement(GET_TOTAL_SALES_AMOUNT); stmt.setString(1, fromDate);
	 * stmt.setString(2, toDate); ResultSet rs = stmt.executeQuery(); if (rs.next())
	 * { salesAmount = rs.getDouble("TOTAL_SALES_AMOUNT"); } stmt =
	 * conn.prepareStatement(GET_TOTAL_SALES_RETURN_AMOUNT_REPORT);
	 * stmt.setString(1, fromDate); stmt.setString(2, toDate); ResultSet rs2 =
	 * stmt.executeQuery(); if (rs2.next()) { salesReturnAmt =
	 * rs2.getDouble("TOTAL_SALES_RETURN_AMOUNT"); } salesAmt = salesAmount -
	 * salesReturnAmt; sales.setAmount(salesAmt);
	 * 
	 * // Closing Stock Value ProfitLossData closingStock = new ProfitLossData();
	 * closingStock.setDescription(AppConstants.CLOSING_STOCK); double
	 * closingStockValue = 0.0; if
	 * (toDate.toString().equals(appUtils.getCurrentTimestamp())) { stmt =
	 * conn.prepareStatement(GET_CLOSING_STOCK_VALUE); ResultSet rs7 =
	 * stmt.executeQuery(); if (rs7.next()) { closingStockValue =
	 * appUtils.getDecimalRoundUp2Decimal(rs7.getDouble("CLOSING_STOCK_VALUE"));
	 * closingStockAmt = closingStockValue; } } else { // Add logic to get opening
	 * stock for todate+1 days opening stock value DateTime dateTime = new
	 * DateTime(toDate); java.util.Date onePlusDay = dateTime.plusDays(1).toDate();
	 * System.out.println("One Plus To Date : " + onePlusDay); closingStockValue =
	 * getOpeningStockValue(appUtils.getDBFormattedDate(new
	 * Date(onePlusDay.getTime()))); closingStockAmt = closingStockValue; }
	 * closingStock.setAmount(closingStockValue); credit.add(closingStock);
	 * credit.add(sales); report.setCredit(credit); // Totals totalDebit =
	 * expensesAmt + openingStockAmt + purchasesAmt; totalCredit = closingStockAmt +
	 * salesAmt; report.setTotalCredit(totalCredit);
	 * report.setTotalDebit(totalDebit); if (totalDebit < totalCredit) {
	 * report.setNetProfit(totalCredit - totalDebit); } if (totalCredit <
	 * totalDebit) { report.setNetLoss(totalDebit - totalCredit); }
	 * System.out.println(report); } catch (Exception e) { e.printStackTrace();
	 * logger.error("Exception : ", e); } finally { DBUtils.closeConnection(stmt,
	 * conn); } return report; }
	 */

	public ProfitLossDetails getProfitLossReport(String fromDate, String toDate) {
		// Debits
		List<ProfitLossData> debit = new ArrayList<ProfitLossData>();
		ProfitLossDetails report = new ProfitLossDetails();
		Connection conn = null;
		double totalDebit = 0.0;
		double totalCredit = 0.0;
		try {
			conn = dbUtils.getConnection();
			// Expense Types
			PreparedStatement stmt = conn.prepareStatement(GET_TOTAL_EXP);
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
			ResultSet rs3 = stmt.executeQuery();
			while (rs3.next()) {
				ProfitLossData expenses = new ProfitLossData();
				expenses.setDescription(rs3.getString("CATEGORY"));
				expenses.setAmount(rs3.getDouble("AMOUNT"));
				totalDebit += rs3.getDouble("AMOUNT");
				debit.add(expenses);
			}
			stmt.close();
			// Minus Sales Return Profit amount
			double salesReturnProfit = 0;
			PreparedStatement stmt1 = conn.prepareStatement(GET_SALES_RETURN_PROFIT_AMT);
			stmt1.setString(1, fromDate);
			stmt1.setString(2, toDate);
			ResultSet rs5 = stmt1.executeQuery();
			if (rs5.next()) {
				salesReturnProfit = rs5.getDouble("NEGATIVE_PROFIT");
			}
			stmt1.close();
			// Purchase Entry Extra Charges and Discount Amount
			double extraCharges = 0;
			double discountAmount = 0;

			PreparedStatement stmt3 = conn.prepareStatement(GET_PURCHASE_ENTRY_DISC_EXTRA_CHARGES_AMT);
			stmt3.setString(1, fromDate);
			stmt3.setString(2, toDate);
			ResultSet rs6 = stmt3.executeQuery();
			if (rs6.next()) {
				discountAmount = rs6.getDouble("DISCOUNT_AMOUNT");
				extraCharges = rs6.getDouble("EXTRA_CHARGES");
			}
			stmt3.close();
			ProfitLossData peExtraCharges = new ProfitLossData();
			peExtraCharges.setDescription(AppConstants.PURCHASE_ENTRY_EXTRA_CHARGES);
			peExtraCharges.setAmount(extraCharges);

			ProfitLossData peDiscAmount = new ProfitLossData();
			peDiscAmount.setDescription("Purchase Entry Discount Amount");
			peDiscAmount.setAmount(discountAmount);

			debit.add(peExtraCharges);
			report.setDebit(debit);
			// Profit
			List<ProfitLossData> credit = new ArrayList<ProfitLossData>();
			double profitAmt = 0;
			PreparedStatement stmt2 = conn.prepareStatement(GET_PROFIT_AMT);
			stmt2.setString(1, fromDate);
			stmt2.setString(2, toDate);
			ResultSet rs = stmt2.executeQuery();
			ProfitLossData profit = new ProfitLossData();
			profit.setDescription(AppConstants.PROFIT);
			if (rs.next()) {
				profitAmt = rs.getDouble("PROFIT");
			}
			stmt2.close();
			profit.setAmount(profitAmt - salesReturnProfit);
			totalCredit = profitAmt - salesReturnProfit + discountAmount;
			totalDebit += extraCharges;
			credit.add(profit);
			credit.add(peDiscAmount);
			report.setCredit(credit);
			// Totals
			report.setTotalCredit(totalCredit);
			report.setTotalDebit(totalDebit);
			if (totalDebit < totalCredit) {
				report.setNetProfit(totalCredit - totalDebit);
			}
			if (totalCredit < totalDebit) {
				report.setNetLoss(totalDebit - totalCredit);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(null, conn);
		}

		return report;
	}

	// Get Opening Stock Value
	private Double getOpeningStockValue(String date) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Double openingCashAmount = 0.0;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_OPENING_STOCK_VALUE);
			stmt.setString(1, date);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				openingCashAmount = rs.getDouble("AMOUNT");
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return openingCashAmount;
	}

	// Get Stock Value Amount
	private Double getStockValueAmount() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Double stockValueAmt = 0.0;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_CLOSING_STOCK_VALUE);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				stockValueAmt = rs.getDouble("CLOSING_STOCK_VALUE");
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return stockValueAmt;
	}

	// Add Opening Stock Value
	private StatusDTO insertOpeningStockValue(String date, double amount) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(INS_OPENING_STOCK_VALUE);
			stmt.setString(1, date);
			stmt.setDouble(2, amount);

			int i = stmt.executeUpdate();
			if (i > 0) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setException(e.getMessage());
			status.setStatusCode(-1);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	// Add Opening Stock Value recurrsive logic
	/*
	 * private StatusDTO addOpeningStockAmount(String date, double amount) {
	 * StatusDTO status = new StatusDTO(-1); Double openingStock =
	 * getOpeningStockValue(date); System.out.println("Day Opening Value :" +
	 * openingStock); if (openingStock == 0.0) { insertOpeningStockValue(date,
	 * amount); DateTime dateTime = new DateTime(date); java.util.Date oneMinusDay =
	 * dateTime.minusDays(1).toDate();
	 * System.out.println("Checking for Previous Day :+" + oneMinusDay);
	 * addOpeningStockAmount(appUtils.getDBFormattedDate(new
	 * Date(oneMinusDay.getTime())), amount); } return status;
	 * 
	 * }
	 */
	/*
	 * private StatusDTO doRecurrsiveInsertOpeningAmount() { StatusDTO status = new
	 * StatusDTO(-1); double stockValueAmount = getStockValueAmount();
	 * System.out.println("Stock Value Amount : " + stockValueAmount);
	 * addOpeningStockAmount(appUtils.getCurrentTimestamp(),
	 * appUtils.getDecimalRoundUp2Decimal(stockValueAmount)); return status; }
	 */

	// Get Stock Value Amount
	public List<Customer> getSettledCustomerList(String date) {
		List<Customer> customerList = new ArrayList<Customer>();
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SETTLEMENT_CUST_INFO);
			stmt.setString(1, date);
			stmt.setString(2, date);
			System.out.println(date);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Customer cust = new Customer();
				cust.setCustMobileNumber(rs.getLong("CUST_MOB_NO"));
				cust.setCustName(rs.getString("CUST_NAME"));
				cust.setAmount(rs.getDouble("DEBIT"));
				customerList.add(cust);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return customerList;
	}

	// Graphic Reports
	// Get Payment mode wise collection
	public List<GraphDTO> getPaymentModeAmounts(String fromDate, String toDate) {
		List<GraphDTO> list = new ArrayList<GraphDTO>();
		Connection conn = null;
		PreparedStatement stmt = null;
		GraphDTO graph = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(PAYMENT_MODE_AMOUNT);
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				graph = new GraphDTO();
				graph.setPaymentMode(rs.getString("PAYMENT_MODE"));
				graph.setTotalAmount(rs.getDouble("TOTAL_AMT"));

				list.add(graph);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return list;
	}

	// Get Daily Sales Amount & Profit Amount Bar Graph
	public List<GraphDTO> getDailySalesReport() {
		List<GraphDTO> list = new ArrayList<GraphDTO>();
		Connection conn = null;
		PreparedStatement stmt = null;
		GraphDTO graph = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(DAILY_SALES_AMOUNT_REPORT);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				graph = new GraphDTO();
				graph.setDate(rs.getString("BILL_DATE"));
				graph.setTotalCollection((int) rs.getDouble("TOTAL_AMT"));
				graph.setTotalPurchaseAmt((int) rs.getDouble("TOTAL_PUR_AMT"));
				graph.setNoOfInvoicesMade(rs.getInt("NO_OF_INVOICES"));
				list.add(graph);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return list;
	}

	// Get Monthly Sales Amount & Profit Amount Bar Graph
	public List<GraphDTO> getMonthlySalesReport() {
		List<GraphDTO> list = new ArrayList<GraphDTO>();
		Connection conn = null;
		PreparedStatement stmt = null;
		GraphDTO graph = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(MONTHLY_SALES_AMOUNT_REPORT);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				graph = new GraphDTO();
				graph.setDate(rs.getString("BILL_DATE"));
				graph.setTotalCollection((int) rs.getDouble("TOTAL_AMT"));
				graph.setTotalPurchaseAmt((int) rs.getDouble("TOTAL_PUR_AMT"));

				list.add(graph);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return list;
	}

	public Map<String, Double> getDashboardReportValues() {
		Map<String, Double> map = new HashMap<>();
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement("SELECT SUM(QUANTITY*SELL_PRICE) AS STOCK_VALUE FROM PRODUCT_DETAILS");
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				map.put("STOCK_VALUE", rs.getDouble("STOCK_VALUE"));
			}
			rs.close();
			stmt.close();
			stmt = conn.prepareStatement("SELECT SUM(BALANCE_AMOUNT) AS TO_PAY FROM SUPPLIER_DETAILS");
			rs = stmt.executeQuery();
			if (rs.next()) {
				map.put("TO_PAY", rs.getDouble("TO_PAY"));
			}
			rs.close();
			stmt.close();
			stmt = conn.prepareStatement("SELECT SUM(BALANCE_AMOUNT) AS TO_COLLECT FROM CUSTOMER_DETAILS");
			rs = stmt.executeQuery();
			if (rs.next()) {
				map.put("TO_COLLECT", rs.getDouble("TO_COLLECT"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return map;
	}

}
