package com.billing.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.BillDetails;
import com.billing.dto.ItemDetails;
import com.billing.dto.StatusDTO;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Service
public class InvoiceService {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

	private static final String INS_BILL_DETAILS = "INSERT INTO CUSTOMER_BILL_DETAILS (BILL_NUMBER,BILL_DATE_TIME,CUST_MOB_NO,CUST_NAME,NO_OF_ITEMS,"
			+ "BILL_QUANTITY,TOTAL_AMOUNT,BILL_TAX,GRAND_TOTAL,PAYMENT_MODE,"
			+ "BILL_DISCOUNT,BILL_DISC_AMOUNT,NET_SALES_AMOUNT,BILL_PURCHASE_AMT)"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String INS_BILL_ITEM_DETAILS = "INSERT INTO BILL_ITEM_DETAILS (BILL_NUMBER,ITEM_NUMBER,ITEM_NAME,ITEM_MRP,ITEM_RATE,"
			+ "ITEM_QTY,ITEM_AMOUNT,ITEM_PURCHASE_AMT) VALUES(?,?,?,?,?,?,?,?)";

	private static final String UPDATE_PRODUCT_STOCK = "UPDATE PRODUCT_DETAILS SET QUANTITY=QUANTITY-? WHERE PRODUCT_ID=?";

	private static final String SELECT_BILL_WITH_BILLNO_AND_DATE = "SELECT CBD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM CUSTOMER_BILL_DETAILS CBD,CUSTOMER_DETAILS CD WHERE CBD.BILL_NUMBER=? AND"
			+ " CBD.CUST_MOB_NO=CD.CUST_MOB_NO AND DATE(BILL_DATE_TIME) BETWEEN ? AND ?";

	private static final String SELECT_BILL_WITH_BILLNO = "SELECT CBD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM CUSTOMER_BILL_DETAILS CBD,CUSTOMER_DETAILS CD WHERE CBD.BILL_NUMBER=? AND"
			+ " CBD.CUST_MOB_NO=CD.CUST_MOB_NO";

	private static final String SELECT_ITEM_DETAILS = "SELECT BID.*,PD.PRODUCT_NAME FROM BILL_ITEM_DETAILS BID,PRODUCT_DETAILS PD WHERE BILL_NUMBER=? AND BID.ITEM_NUMBER=PD.PRODUCT_ID";

	private static final String NEW_BILL_NUMBER = "SELECT (MAX(BILL_NUMBER)+1) AS BILL_NO FROM CUSTOMER_BILL_DETAILS ";

	// Save Bill Details
	public StatusDTO saveBillDetails(BillDetails bill) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO staus = new StatusDTO();
		try {
			if (bill != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(INS_BILL_DETAILS);
				stmt.setInt(1, bill.getBillNumber());
				stmt.setString(2, appUtils.getCurrentTimestamp());
				stmt.setLong(3, bill.getCustomerMobileNo());
				stmt.setString(4, bill.getCustomerName());
				stmt.setInt(5, bill.getNoOfItems());
				stmt.setDouble(6, bill.getTotalQuantity());
				stmt.setDouble(7, bill.getTotalAmount());
				stmt.setDouble(8, bill.getTax());
				stmt.setDouble(9, bill.getGrandTotal());
				stmt.setString(10, bill.getPaymentMode());
				stmt.setDouble(11, bill.getDiscount());
				stmt.setDouble(12, bill.getDiscountAmt());
				stmt.setDouble(13, bill.getNetSalesAmt());
				stmt.setDouble(14, bill.getPurchaseAmt());
				int i = stmt.executeUpdate();
				if (i > 0) {
					staus.setStatusCode(0);
					saveBillItemDetails(bill.getItemDetails());
					updateProductStock(bill.getItemDetails());
					System.out.println("Bill Details Saved !");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			staus.setStatusCode(-1);
			staus.setException(e.getMessage());
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return staus;
	}

	// Save Bill Details
	public boolean saveBillItemDetails(List<ItemDetails> itemList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;

		try {
			if (!itemList.isEmpty()) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(INS_BILL_ITEM_DETAILS);
				for (ItemDetails item : itemList) {
					stmt.setInt(1, item.getBillNumber());
					stmt.setInt(2, item.getItemNo());
					stmt.setString(3, item.getItemName());
					stmt.setDouble(4, item.getMRP());
					stmt.setDouble(5, item.getRate());
					stmt.setDouble(6, item.getQuantity());
					stmt.setDouble(7, item.getAmount());
					stmt.setDouble(8, item.getPurchasePrice());
					stmt.addBatch();
				}

				int batch[] = stmt.executeBatch();
				conn.commit();
				if (batch.length == itemList.size()) {
					flag = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}

	// Update Product Stock
	public boolean updateProductStock(List<ItemDetails> itemList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;

		try {
			if (!itemList.isEmpty()) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(UPDATE_PRODUCT_STOCK);
				for (ItemDetails item : itemList) {
					stmt.setInt(2, item.getItemNo());
					stmt.setDouble(1, item.getQuantity());
					stmt.addBatch();
				}
				int batch[] = stmt.executeBatch();
				conn.commit();
				if (batch.length == itemList.size()) {
					flag = true;
					System.out.println("Product Stock  updated");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}

	// Get Bill Details
	public List<BillDetails> getBillDetails(String fromDate, String toDate, Long customerMobile) {
		Connection conn = null;
		PreparedStatement stmt = null;
		BillDetails billDetails = null;
		List<BillDetails> billDetailsList = new ArrayList<BillDetails>();
		StringBuilder SELECT_BILL_DETAILS = new StringBuilder(
				"SELECT CBD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM CUSTOMER_BILL_DETAILS CBD,CUSTOMER_DETAILS CD WHERE DATE(CBD.BILL_DATE_TIME) BETWEEN ? AND ?  AND CBD.CUST_MOB_NO=CD.CUST_MOB_NO ");

		String ORDER_BY_CLAUSE = "ORDER BY CBD.BILL_DATE_TIME DESC";
		String CUSTOMER_MOB_QEUERY = " AND CBD.CUST_MOB_NO LIKE ? ";
		try {
			if (fromDate == null) {
				fromDate = "1947-01-01";
			}
			if (toDate == null) {
				toDate = appUtils.getCurrentTimestamp();
			}
			if (customerMobile != null) {
				SELECT_BILL_DETAILS.append(CUSTOMER_MOB_QEUERY);
			}
			SELECT_BILL_DETAILS.append(ORDER_BY_CLAUSE);
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SELECT_BILL_DETAILS.toString());
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
			if (customerMobile != null) {
				stmt.setString(3, "%" + customerMobile + "%");
			}
			System.out.println("SELECT_BILL_DETAILS " + SELECT_BILL_DETAILS);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				billDetails = new BillDetails();
				billDetails.setBillNumber(rs.getInt("BILL_NUMBER"));
				billDetails.setTimestamp(rs.getString("BILL_DATE_TIME"));
				billDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
				billDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
				billDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				billDetails.setTotalQuantity(rs.getDouble("BILL_QUANTITY"));
				billDetails.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
				billDetails.setTax(rs.getDouble("BILL_TAX"));
				billDetails.setGrandTotal(rs.getDouble("GRAND_TOTAL"));
				billDetails.setPaymentMode(rs.getString("PAYMENT_MODE"));
				billDetails.setDiscount(rs.getDouble("BILL_DISCOUNT"));
				billDetails.setDiscountAmt(rs.getDouble("BILL_DISC_AMOUNT"));
				billDetails.setNetSalesAmt(rs.getDouble("NET_SALES_AMOUNT"));
				billDetails.setPurchaseAmt(rs.getDouble("BILL_PURCHASE_AMT"));

				billDetailsList.add(billDetails);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return billDetailsList;
	}

	// Get Item Details for Bill Number
	public List<ItemDetails> getItemDetails(int billNumber) {
		List<ItemDetails> itemDetailsList = new ArrayList<ItemDetails>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ItemDetails itemDetails = null;

		conn = dbUtils.getConnection();
		try {
			stmt = conn.prepareStatement(SELECT_ITEM_DETAILS);

			stmt.setInt(1, billNumber);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				itemDetails = new ItemDetails();
				itemDetails.setItemNo(rs.getInt("ITEM_NUMBER"));
				itemDetails.setItemName(rs.getString("PRODUCT_NAME"));
				itemDetails.setMRP(rs.getDouble("ITEM_MRP"));
				itemDetails.setRate(rs.getDouble("ITEM_RATE"));
				itemDetails.setQuantity(rs.getDouble("ITEM_QTY"));

				itemDetailsList.add(itemDetails);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}

		return itemDetailsList;
	}

	// Get Bill Details for Bill Number with Date Range
	public BillDetails getBillDetailsOfBillNumberWithinDateRange(Integer billNumber, Date fromDate, Date toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		BillDetails billDetails = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SELECT_BILL_WITH_BILLNO_AND_DATE);
			stmt.setInt(1, billNumber);
			stmt.setDate(2, fromDate);
			stmt.setDate(3, toDate);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				billDetails = new BillDetails();
				billDetails.setBillNumber(rs.getInt("BILL_NUMBER"));
				billDetails.setTimestamp(rs.getString("BILL_DATE_TIME"));
				billDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
				billDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
				billDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				billDetails.setTotalQuantity(rs.getDouble("BILL_QUANTITY"));
				billDetails.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
				billDetails.setTax(rs.getDouble("BILL_TAX"));
				billDetails.setGrandTotal(rs.getDouble("GRAND_TOTAL"));
				billDetails.setPaymentMode(rs.getString("PAYMENT_MODE"));
				billDetails.setDiscount(rs.getDouble("BILL_DISCOUNT"));
				billDetails.setDiscountAmt(rs.getDouble("BILL_DISC_AMOUNT"));
				billDetails.setNetSalesAmt(rs.getDouble("NET_SALES_AMOUNT"));
				billDetails.setPurchaseAmt(rs.getDouble("BILL_PURCHASE_AMT"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return billDetails;
	}

	// Get Bill Details for Bill Number
	public BillDetails getBillDetailsOfBillNumber(Integer billNumber) {
		Connection conn = null;
		PreparedStatement stmt = null;
		BillDetails billDetails = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SELECT_BILL_WITH_BILLNO);
			stmt.setInt(1, billNumber);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				billDetails = new BillDetails();
				billDetails.setBillNumber(rs.getInt("BILL_NUMBER"));
				billDetails.setTimestamp(rs.getString("BILL_DATE_TIME"));
				billDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
				billDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
				billDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				billDetails.setTotalQuantity(rs.getDouble("BILL_QUANTITY"));
				billDetails.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
				billDetails.setTax(rs.getDouble("BILL_TAX"));
				billDetails.setGrandTotal(rs.getDouble("GRAND_TOTAL"));
				billDetails.setPaymentMode(rs.getString("PAYMENT_MODE"));
				billDetails.setDiscount(rs.getDouble("BILL_DISCOUNT"));
				billDetails.setDiscountAmt(rs.getDouble("BILL_DISC_AMOUNT"));
				billDetails.setNetSalesAmt(rs.getDouble("NET_SALES_AMOUNT"));
				billDetails.setPurchaseAmt(rs.getDouble("BILL_PURCHASE_AMT"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return billDetails;
	}

	public Integer getNewBillNumber() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Integer newBillNumber = 0;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(NEW_BILL_NUMBER);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				newBillNumber = rs.getInt("BILL_NO");
				if (newBillNumber == 0) {
					newBillNumber = 1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return newBillNumber;
	}

}