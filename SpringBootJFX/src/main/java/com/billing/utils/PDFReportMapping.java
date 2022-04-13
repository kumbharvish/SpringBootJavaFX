package com.billing.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.billing.constants.AppConstants;
import com.billing.dto.Barcode;
import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.CustomersReport;
import com.billing.dto.Expense;
import com.billing.dto.ExpenseReport;
import com.billing.dto.GSTDetails;
import com.billing.dto.GSTR1Data;
import com.billing.dto.GSTR1Report;
import com.billing.dto.ItemDetails;
import com.billing.dto.LowStockSummaryReport;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.dto.ProductCategoryWiseStockReport;
import com.billing.dto.ProductProfitReport;
import com.billing.dto.ReturnDetails;
import com.billing.dto.SalesReport;
import com.billing.dto.SalesReturnReport;
import com.billing.dto.StockSummaryReport;
import com.billing.dto.Supplier;
import com.billing.dto.SuppliersReport;

@Component
public class PDFReportMapping {

	@Autowired
	AppUtils appUtils;

	// Invoice Data Source
	public List<Map<String, ?>> getDatasourceForInvoice(BillDetails bill, String jasperName) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();

		double totalAmtForCashInvice = 0;
		for (ItemDetails item : bill.getItemDetails()) {
			totalAmtForCashInvice += item.getItemAmount();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Name", getProductPrintName(item, bill, jasperName));
			map.put("Qty", appUtils.getDecimalFormat(item.getQuantity()));
			map.put("Rate", appUtils.getDecimalFormat(item.getRate()));
			map.put("Amount", IndianCurrencyFormatting.applyFormatting(item.getAmount()));
			map.put("ItemAmt", IndianCurrencyFormatting.applyFormatting(item.getItemAmount()));
			map.put("HSN", item.getHsn() == null ? "" : item.getHsn());
			map.put("ItemGSTAmt", IndianCurrencyFormatting.applyFormatting(item.getGstDetails().getGstAmount()));
			map.put("ItemGSTPer", String.valueOf((int) item.getGstDetails().getRate()));
			map.put("ItemDiscAmt", IndianCurrencyFormatting.applyFormatting(item.getDiscountAmount()));
			map.put("ItemDiscPer", appUtils.getPercentValueForReport(item.getDiscountPercent()));

			map.put("BillNo", String.valueOf(bill.getBillNumber()));
			map.put("TotalQty", appUtils.getDecimalFormat(bill.getTotalQuantity()));
			map.put("NoOfItems", String.valueOf(bill.getNoOfItems()));
			map.put("TotalAmount", IndianCurrencyFormatting.applyFormatting(bill.getTotalAmount()));
			map.put("NetSalesAmount", IndianCurrencyFormatting.applyFormatting(bill.getNetSalesAmt()));
			map.put("DiscountPer", appUtils.getPercentValueForReport(bill.getDiscount()));
			map.put("DiscountAmount", IndianCurrencyFormatting.applyFormatting(bill.getDiscountAmt()));
			map.put("InvoiceDateTime", appUtils.getFormattedDateWithTime(bill.getTimestamp()));
			map.put("GSTAmount", IndianCurrencyFormatting.applyFormatting(bill.getGstAmount()));
			map.put("CGSTAmount", IndianCurrencyFormatting.applyFormatting(bill.getGstAmount() / 2));
			map.put("SGSTAmount", IndianCurrencyFormatting.applyFormatting(bill.getGstAmount() / 2));
			map.put("GSTInclusiveFlag", bill.getGstType());
			map.put("TotalAmountForCashInvoice", IndianCurrencyFormatting.applyFormatting(totalAmtForCashInvice));
			map.put("CustomerName", bill.getCustomerName());
			map.put("CustomerMobileNo", String.valueOf(bill.getCustomerMobileNo()));
			map.put("CustomerAddress", String.valueOf(bill.getCustomer().getAddress()));
			map.put("CustomerState", String.valueOf(bill.getCustomer().getState()));
			map.put("CustomerCity", String.valueOf(bill.getCustomer().getCustCity()));
			map.put("CustomerGstin", String.valueOf(bill.getCustomer().getGstin()));
			map.put("termsCondition", appUtils.getAppDataValues(AppConstants.TERMS_AND_CONDITION_FOR_INVOICE));
			map.put("SubTotalAmountTH", IndianCurrencyFormatting.applyFormatting(bill.getTotalAmount() + bill.getGstAmount()));
			dataSourceMaps.add(map);
		}

		return dataSourceMaps;
	}

	private String getProductPrintName(ItemDetails item, BillDetails bill, String jasperName) {
		String printName = "";
		if (appUtils.getAppDataValues(AppConstants.SHOW_CATEGORY_NAME_ON_INVOICE).equalsIgnoreCase("N")) {
			if (item.getPrintName() != null && !item.getPrintName().equalsIgnoreCase("")) {
				printName = item.getPrintName().toUpperCase();
			} else {
				printName = item.getItemName().toUpperCase();
			}
		} else {
			printName = item.getCategoryName().toUpperCase() + " # " + item.getItemNo();
		}
		// For Thermal Print append product wise discount in product name
		if (item.getDiscountPercent() > 0 && bill.getDiscount() == 0 && jasperName.contains("_TH_")) {
			printName = printName + " - DISC " + IndianCurrencyFormatting.applyFormatting(item.getDiscountAmount())
					+ " @ " + appUtils.getPercentValueForReport(item.getDiscountPercent()) + "%";
		}
		return printName;
	}

	// Sub Report Mapping
	public List<Map<String, ?>> getDataSourceForSubReports(BillDetails bill, HashMap<String, Object> headersMap,
			String jasperName) {
		List<Map<String, ?>> dataSourceMapsSubReport = new ArrayList<Map<String, ?>>();
		HashMap<String, Double> dataMap = new LinkedHashMap<>();
		HashMap<String, GSTDetails> dataMapGST = new LinkedHashMap<>();
		
		if (AppConstants.IT_A4_TAX_3.equalsIgnoreCase(jasperName)
				|| AppConstants.IT_A4_TAX_5.equalsIgnoreCase(jasperName)) {
			Map<String, Object> subreportMap = new HashMap<String, Object>();
			subreportMap.put("netSalesAmount", IndianCurrencyFormatting.applyFormatting(bill.getNetSalesAmt()));
			subreportMap.put("subTotalAmount", IndianCurrencyFormatting.applyFormatting(bill.getTotalAmount()));
			subreportMap.put("storeName", headersMap.get("StoreName"));
			dataSourceMapsSubReport.add(subreportMap);
		}
		if (AppConstants.IT_A4_TAX_4.equalsIgnoreCase(jasperName)) {
			Map<String, Object> subreportMap = new HashMap<String, Object>();
			subreportMap.put("netSalesAmount", IndianCurrencyFormatting.applyFormatting(bill.getNetSalesAmt()));
			subreportMap.put("subTotalAmount", IndianCurrencyFormatting.applyFormatting(bill.getTotalAmount()));
			subreportMap.put("storeName", headersMap.get("StoreName"));
			subreportMap.put("discountAmount", IndianCurrencyFormatting.applyFormatting(bill.getDiscountAmt()));
			subreportMap.put("discountPer", appUtils.getPercentValueForReport(bill.getDiscount()));
			dataSourceMapsSubReport.add(subreportMap);

		} else {
			if (jasperName.contains("_TH_")) {
				for (ItemDetails item : bill.getItemDetails()) {
					GSTDetails gst = item.getGstDetails();
					GSTDetails gstNew = new GSTDetails();
					gstNew.setRate(gst.getRate());
					String rate = String.valueOf(gst.getRate()).replace(".0", "");
					if (dataMapGST.containsKey(rate)) {
						GSTDetails gstNewDetails = dataMapGST.get(rate);
						gstNewDetails.setGstAmount(gst.getGstAmount() + gstNewDetails.getGstAmount());
						gstNewDetails.setCgst(gst.getCgst()+gstNewDetails.getCgst());
						gstNewDetails.setSgst(gst.getSgst()+gstNewDetails.getSgst());
						gstNewDetails.setTaxableAmount(gst.getTaxableAmount()+ gstNewDetails.getTaxableAmount());
						gstNewDetails.setTotalItemAmount(gstNewDetails.getTotalItemAmount() + gst.getTaxableAmount() + gst.getGstAmount());
						dataMapGST.put(rate, gstNewDetails);
					} else {
						gstNew.setGstAmount(gst.getGstAmount());
						gstNew.setCgst(gst.getCgst());
						gstNew.setSgst(gst.getSgst());
						gstNew.setTaxableAmount(gst.getTaxableAmount());
						gstNew.setTotalItemAmount(gst.getTaxableAmount() + gst.getGstAmount());
						dataMapGST.put(rate, gstNew);
					}
				}
				double totalTaxableAmt = 0;
				double totalCgst = 0;
				double totalSgst = 0;
				double totalItemAmount = 0;
				for (String rate : dataMapGST.keySet()) {
					Map<String, Object> subreportMap = new HashMap<String, Object>();
					subreportMap.put("gstRate", rate);
					GSTDetails gst = dataMapGST.get(rate);
					totalTaxableAmt = totalTaxableAmt + gst.getTaxableAmount();
					totalCgst = totalCgst + gst.getCgst();
					totalSgst = totalSgst + gst.getSgst();
					totalItemAmount = totalItemAmount + gst.getTotalItemAmount();
					subreportMap.put("taxableAmount", appUtils.getDecimalFormat(gst.getTaxableAmount()));
					subreportMap.put("cgst", appUtils.getDecimalFormat(gst.getCgst()));
					subreportMap.put("sgst", appUtils.getDecimalFormat(gst.getSgst()));
					subreportMap.put("totalItemAmount", appUtils.getDecimalFormat(gst.getTotalItemAmount()));
					subreportMap.put("netSalesAmount", IndianCurrencyFormatting.applyFormatting(bill.getNetSalesAmt()));
					subreportMap.put("subTotalAmount", IndianCurrencyFormatting.applyFormatting(bill.getTotalAmount()));
					subreportMap.put("storeName", headersMap.get("StoreName"));
					subreportMap.put("discountAmount", IndianCurrencyFormatting.applyFormatting(bill.getDiscountAmt()));
					subreportMap.put("discountPer", appUtils.getPercentValueForReport(bill.getDiscount()));
					subreportMap.put("termsCondition", appUtils.getAppDataValues(AppConstants.TERMS_AND_CONDITION_FOR_INVOICE));
					subreportMap.put("totalTaxableAmt", IndianCurrencyFormatting.applyFormatting(totalTaxableAmt));
					subreportMap.put("totalCgst", IndianCurrencyFormatting.applyFormatting(totalCgst));
					subreportMap.put("totalSgst", IndianCurrencyFormatting.applyFormatting(totalSgst));
					subreportMap.put("totalAmount", IndianCurrencyFormatting.applyFormatting(totalItemAmount));
					dataSourceMapsSubReport.add(subreportMap);
				}
			} else {
				for (ItemDetails item : bill.getItemDetails()) {
					GSTDetails gst = item.getGstDetails();
					String keySgst = "SGST@" + gst.getSgstPercent() + "%";
					String keyCgst = "CGST@" + gst.getCgstPercent() + "%";
					// SGST
					if (dataMap.containsKey(keySgst)) {
						Double amount = dataMap.get(keySgst);
						dataMap.put(keySgst, gst.getSgst() + amount);
					} else {
						dataMap.put(keySgst, gst.getSgst());
					}
					// CGST
					if (dataMap.containsKey(keyCgst)) {
						Double amount = dataMap.get(keyCgst);
						dataMap.put(keyCgst, gst.getCgst() + amount);
					} else {
						dataMap.put(keyCgst, gst.getCgst());
					}
				}
				for (String rate : dataMap.keySet()) {
					Map<String, Object> subreportMap = new HashMap<String, Object>();
					subreportMap.put("gstRate", rate);
					subreportMap.put("gstAmount", appUtils.getDecimalFormat(dataMap.get(rate)));
					subreportMap.put("netSalesAmount", IndianCurrencyFormatting.applyFormatting(bill.getNetSalesAmt()));
					subreportMap.put("subTotalAmount", IndianCurrencyFormatting.applyFormatting(bill.getTotalAmount()));
					subreportMap.put("storeName", headersMap.get("StoreName"));
					subreportMap.put("discountAmount", IndianCurrencyFormatting.applyFormatting(bill.getDiscountAmt()));
					subreportMap.put("discountPer", appUtils.getPercentValueForReport(bill.getDiscount()));
					dataSourceMapsSubReport.add(subreportMap);
				}
			}
			
		}
		return dataSourceMapsSubReport;
	}

	// Sub Report Mapping
	public List<Map<String, ?>> getDataSourceForSubReportTC(BillDetails bill, HashMap<String, Object> headersMap,
			String jasperName) {
		List<Map<String, ?>> dataSourceMapsSubReport = new ArrayList<Map<String, ?>>();
		HashMap<String, GSTDetails> dataMap = new LinkedHashMap<>();

		if (AppConstants.IT_A4_TAX_3.equalsIgnoreCase(jasperName)
				|| AppConstants.IT_A4_TAX_4.equalsIgnoreCase(jasperName)
				|| AppConstants.IT_A4_TAX_5.equalsIgnoreCase(jasperName)) {
			for (ItemDetails item : bill.getItemDetails()) {
				GSTDetails gstSGST = new GSTDetails();
				GSTDetails gstCGST = new GSTDetails();

				GSTDetails gst = item.getGstDetails();
				String keySgst = "SGST:" + gst.getSgstPercent() + "%";
				gstSGST.setName(keySgst);
				String keyCgst = "CGST:" + gst.getCgstPercent() + "%";
				gstCGST.setName(keyCgst);

				// SGST
				if (dataMap.containsKey(keySgst)) {
					GSTDetails data = dataMap.get(keySgst);
					data.setGstAmount(data.getSgst() + gst.getSgst());
					data.setTaxableAmount(data.getTaxableAmount() + gst.getTaxableAmount() / 2);
				} else {
					GSTDetails data = new GSTDetails();
					data.setGstAmount(gst.getSgst());
					data.setTaxableAmount(gst.getTaxableAmount() / 2);
					data.setName("SGST");
					data.setRate(gst.getRate());
					dataMap.put(keySgst, data);
				}
				// CGST
				if (dataMap.containsKey(keyCgst)) {
					GSTDetails data = dataMap.get(keyCgst);
					data.setGstAmount(data.getCgst() + gst.getCgst());
					data.setTaxableAmount(data.getTaxableAmount() + gst.getTaxableAmount() / 2);
				} else {
					GSTDetails data = new GSTDetails();
					data.setGstAmount(gst.getCgst());
					data.setTaxableAmount(gst.getTaxableAmount() / 2);
					data.setName("CGST");
					data.setRate(gst.getRate());
					dataMap.put(keyCgst, data);
				}
			}

			for (String key : dataMap.keySet()) {
				Map<String, Object> subreportMap = new HashMap<String, Object>();
				GSTDetails details = dataMap.get(key);
				subreportMap.put("taxType", details.getName());
				subreportMap.put("gstRate", appUtils.getPercentValueForReport(details.getRate() / 2) + "%");
				subreportMap.put("taxAmount", IndianCurrencyFormatting.applyFormatting(details.getGstAmount()));
				subreportMap.put("taxableAmount", IndianCurrencyFormatting.applyFormatting(details.getTaxableAmount()));
				subreportMap.put("amountInWords",
						NumberToWords.convertNumberToWords(new BigDecimal(bill.getNetSalesAmt()), true, true));
				subreportMap.put("termsCondition",
						appUtils.getAppDataValues(AppConstants.TERMS_AND_CONDITION_FOR_INVOICE));
				dataSourceMapsSubReport.add(subreportMap);
			}
		} else {
			Map<String, Object> subreportMap = new HashMap<String, Object>();
			subreportMap.put("amountInWords",
					NumberToWords.convertNumberToWords(new BigDecimal(bill.getNetSalesAmt()), true, true));
			subreportMap.put("termsCondition", appUtils.getAppDataValues(AppConstants.TERMS_AND_CONDITION_FOR_INVOICE));
			dataSourceMapsSubReport.add(subreportMap);
		}

		return dataSourceMapsSubReport;

	}

	// Product Profit Report
	public List<Map<String, ?>> getDatasourceForProductProfitReport(ProductProfitReport report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (Product product : report.getProductList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Name", product.getProductName());
			map.put("Category", product.getProductCategory());
			map.put("Qty", appUtils.getDecimalFormat(product.getQuantity()));
			map.put("ProfitAmount", IndianCurrencyFormatting.applyFormatting(product.getProfit()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Stock Summary Report
	public List<Map<String, ?>> getDatasourceForStockSummaryReport(StockSummaryReport report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (Product product : report.getProductList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ProductName", product.getProductName());
			map.put("PurchasePrice", appUtils.getDecimalFormat(product.getPurcasePrice()));
			map.put("Qty", appUtils.getDecimalFormat(product.getQuantity()));
			map.put("SalePrice", IndianCurrencyFormatting.applyFormatting(product.getProductMRP()));
			map.put("StockValue", IndianCurrencyFormatting.applyFormatting(product.getStockValueAmount()));
			map.put("TotalStockValue", report.getTotalStockValue());
			map.put("TotalQty", report.getTotalStockQty());
			map.put("Date", appUtils.getTodaysDateForUser());
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Customer Report
	public List<Map<String, ?>> getDatasourceForCustomersReport(CustomersReport report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (Customer cust : report.getCustomerList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("MobileNo", String.valueOf(cust.getCustMobileNumber()));
			map.put("Name", cust.getCustName());
			map.put("City", cust.getCustCity());
			map.put("Email", cust.getCustEmail());
			map.put("PendingAmount", IndianCurrencyFormatting.applyFormatting(cust.getBalanceAmt()));
			map.put("TotalPendingAmount", report.getTotalPendingAmount());
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Low Stock Summary Report
	public List<Map<String, ?>> getDatasourceForLowStockSummaryReport(LowStockSummaryReport lowStockSummaryReport) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (Product item : lowStockSummaryReport.getProductList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ProductName", item.getProductName());
			map.put("StockValue", IndianCurrencyFormatting.applyFormatting(item.getStockValueAmount()));
			map.put("Qty", appUtils.getDecimalFormat(item.getQuantity()));
			map.put("Category", item.getProductCategory());
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Category Wise Stock Report
	public List<Map<String, ?>> getDatasourceForCategoryWiseStockReport(ProductCategoryWiseStockReport report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (ProductCategory pc : report.getProductCategoryList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Category", pc.getCategoryName());
			map.put("Qty", appUtils.getDecimalFormat(pc.getCategoryStockQty()));
			map.put("StockValue", IndianCurrencyFormatting.applyFormatting(pc.getCategoryStockAmount()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Sales Report
	public List<Map<String, ?>> getDatasourceForSalesReport(SalesReport salesReport) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (BillDetails bill : salesReport.getBillList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("DateRange", "From  " + appUtils.getFormattedDateForDatePicker(salesReport.getFromDate()) + "  To  "
					+ appUtils.getFormattedDateForDatePicker(salesReport.getToDate()));
			map.put("InvoiceNo", String.valueOf(bill.getBillNumber()));
			map.put("CustomerName", bill.getCustomerName());
			map.put("Qty", appUtils.getDecimalFormat(bill.getTotalQuantity()));
			map.put("GstAmount", IndianCurrencyFormatting.applyFormatting(bill.getGstAmount()));
			map.put("InvoiceAmount", IndianCurrencyFormatting.applyFormatting(bill.getNetSalesAmt()));
			map.put("InvoiceDate", appUtils.getFormattedDateForReport(bill.getTimestamp()));
			map.put("TotalSaleAmount", IndianCurrencyFormatting.applyFormattingWithCurrency(salesReport.getTotalAmt()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Sales Return Report
	public List<Map<String, ?>> getDatasourceForSalesReturnReport(SalesReturnReport report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (ReturnDetails bill : report.getReturnList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("DateRange", "From  " + appUtils.getFormattedDateForDatePicker(report.getFromDate()) + "  To  "
					+ appUtils.getFormattedDateForDatePicker(report.getToDate()));
			map.put("ReturnNo", String.valueOf(bill.getReturnNumber()));
			map.put("InvoiceNo", String.valueOf(bill.getInvoiceNumber()));
			map.put("CustomerName", bill.getCustomerName());
			map.put("Qty", appUtils.getDecimalFormat(bill.getTotalQuantity()));
			map.put("ReturnAmount", IndianCurrencyFormatting.applyFormatting(bill.getTotalReturnAmount()));
			map.put("ReturnDate", appUtils.getFormattedDateForReport(bill.getTimestamp()));
			map.put("TotalSaleReturnAmount",
					IndianCurrencyFormatting.applyFormattingWithCurrency(report.getTotalReturnAmount()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Expense Report
	public List<Map<String, ?>> getDatasourceForExpenseReport(ExpenseReport report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (Expense expense : report.getExpenseList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("DateRange", "From  " + appUtils.getFormattedDateForDatePicker(report.getFromDate()) + "  To  "
					+ appUtils.getFormattedDateForDatePicker(report.getToDate()));
			map.put("ExpenseCategory", expense.getCategory());
			map.put("Description", expense.getDescription());
			map.put("Date", appUtils.getFormattedDateForReport(expense.getDate()));
			map.put("TotalExpenseAmount", report.getTotalExpenaseAmount());
			map.put("Amount", IndianCurrencyFormatting.applyFormatting(expense.getAmount()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Suppliers Report
	public List<Map<String, ?>> getDatasourceForSuppliersReport(SuppliersReport report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (Supplier supplier : report.getSuppliersList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("MobileNo", String.valueOf(supplier.getSupplierMobile()));
			map.put("Name", supplier.getSupplierName());
			map.put("City", supplier.getCity());
			map.put("Email", supplier.getEmailId());
			map.put("BalanceAmount", IndianCurrencyFormatting.applyFormatting(supplier.getBalanceAmount()));
			map.put("TotalBalanceAmount", report.getTotalBalanceAmount());
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// GSTR1 Report
	public List<Map<String, ?>> getDatasourceForGSTR1Report(GSTR1Report report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		// Calculate Totals
		double totalInvoiceValue = 0;
		double totalTaxableValue = 0;
		double totalCgst = 0;
		for (GSTR1Data bill : report.getInvoiceList()) {
			totalInvoiceValue = totalInvoiceValue + bill.getInvoiceTotalAmount();
			totalCgst = totalCgst + bill.getCgst();
			totalTaxableValue = totalTaxableValue + bill.getTaxableValue();
		}
		report.setTotalCGST(totalCgst);
		report.setTotalSGST(totalCgst);
		report.setTotalTaxableValue(totalTaxableValue);
		report.setTotalInvoiceValue(totalInvoiceValue);

		// Sales
		for (GSTR1Data bill : report.getInvoiceList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("DateRange", "From  " + appUtils.getFormattedDateForDatePicker(report.getFromDate()) + "  To  "
					+ appUtils.getFormattedDateForDatePicker(report.getToDate()));
			map.put("InvoiceNo", String.valueOf(bill.getInvoiceNo()));
			map.put("PartyName", bill.getPartyName());
			map.put("Rate", appUtils.getDecimalFormat(bill.getGstRate()));
			map.put("CGST", IndianCurrencyFormatting.applyFormatting(bill.getCgst()));
			map.put("SGST", IndianCurrencyFormatting.applyFormatting(bill.getSgst()));
			map.put("InvoiceValue", IndianCurrencyFormatting.applyFormatting(bill.getInvoiceTotalAmount()));
			map.put("InvoiceDate", appUtils.getFormattedDateForReport(bill.getInvoiceDate()));
			map.put("TaxableValue", IndianCurrencyFormatting.applyFormattingWithCurrency(bill.getTaxableValue()));

			map.put("TotalValue", IndianCurrencyFormatting.applyFormattingWithCurrency(report.getTotalInvoiceValue()));
			map.put("TotalTaxableValue",
					IndianCurrencyFormatting.applyFormattingWithCurrency(report.getTotalTaxableValue()));
			map.put("TotalCGST", IndianCurrencyFormatting.applyFormattingWithCurrency(report.getTotalCGST()));
			map.put("TotalSGST", IndianCurrencyFormatting.applyFormattingWithCurrency(report.getTotalSGST()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// GSTR1 Sales Return Sub-Report
	public List<Map<String, ?>> getDatasourceForGSTR1SalesRetunReport(GSTR1Report report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		// Calculate Totals
		double totalInvoiceValue = 0;
		double totalTaxableValue = 0;
		double totalCgst = 0;
		for (GSTR1Data bill : report.getSaleReturnList()) {
			totalInvoiceValue = totalInvoiceValue + bill.getInvoiceTotalAmount();
			totalCgst = totalCgst + bill.getCgst();
			totalTaxableValue = totalTaxableValue + bill.getTaxableValue();
		}
		report.setTotalCGST(totalCgst);
		report.setTotalSGST(totalCgst);
		report.setTotalTaxableValue(totalTaxableValue);
		report.setTotalInvoiceValue(totalInvoiceValue);

		// Sales
		for (GSTR1Data bill : report.getSaleReturnList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("DateRange", "From  " + appUtils.getFormattedDateForDatePicker(report.getFromDate()) + "  To  "
					+ appUtils.getFormattedDateForDatePicker(report.getToDate()));
			map.put("InvoiceNo", String.valueOf(bill.getInvoiceNo()));
			map.put("ReturnNo", String.valueOf(bill.getReturnNo()));
			map.put("PartyName", bill.getPartyName());
			map.put("Rate", appUtils.getDecimalFormat(bill.getGstRate()));
			map.put("CGST", IndianCurrencyFormatting.applyFormatting(bill.getCgst()));
			map.put("SGST", IndianCurrencyFormatting.applyFormatting(bill.getSgst()));
			map.put("InvoiceValue", IndianCurrencyFormatting.applyFormatting(bill.getInvoiceTotalAmount()));
			map.put("InvoiceDate", appUtils.getFormattedDateForReport(bill.getInvoiceDate()));
			map.put("ReturnDate", appUtils.getFormattedDateForReport(bill.getReturnDate()));
			map.put("TaxableValue", IndianCurrencyFormatting.applyFormattingWithCurrency(bill.getTaxableValue()));

			map.put("TotalValue", IndianCurrencyFormatting.applyFormattingWithCurrency(report.getTotalInvoiceValue()));
			map.put("TotalTaxableValue",
					IndianCurrencyFormatting.applyFormattingWithCurrency(report.getTotalTaxableValue()));
			map.put("TotalCGST", IndianCurrencyFormatting.applyFormattingWithCurrency(report.getTotalCGST()));
			map.put("TotalSGST", IndianCurrencyFormatting.applyFormattingWithCurrency(report.getTotalSGST()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Barcode Data Source
	public List<Map<String, ?>> getDatasourceForBarcode(Barcode barcode, int noOfLabels, int startPosition) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();

		for (int i = 1; i < startPosition; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ProductName", "");
			dataSourceMaps.add(map);
		}
		String printName = "";
		if (appUtils.getAppDataValues(AppConstants.SHOW_CATEGORY_NAME_ON_INVOICE).equalsIgnoreCase("N")) {
			if (barcode.getPrintName() != null && !barcode.getPrintName().equalsIgnoreCase("")) {
				printName = barcode.getPrintName().toUpperCase();
			} else {
				printName = barcode.getProductName().toUpperCase();
			}
		} else {
			printName = barcode.getCategoryName().toUpperCase() + " # " + barcode.getProductCode();
		}

		for (int i = 1; i <= noOfLabels; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ProductName", printName);
			map.put("Barcode", barcode.getBarcode());
			map.put("Price", appUtils.getDecimalFormat(barcode.getPrice()));
			map.put("MRP", appUtils.getDecimalFormat(barcode.getMrp()));
			map.put("AmountLabel", barcode.getAmountLabel());
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}
}
