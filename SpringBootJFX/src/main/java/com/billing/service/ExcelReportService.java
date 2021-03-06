package com.billing.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.CustomersReport;
import com.billing.dto.Expense;
import com.billing.dto.ExpenseReport;
import com.billing.dto.GSTR1Data;
import com.billing.dto.GSTR1Report;
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
import com.billing.utils.ExcelReportMapping;

@Service
public class ExcelReportService {

	@Autowired
	ExcelReportMapping excelReportMapping;

	private static final Logger logger = LoggerFactory.getLogger(ExcelReportService.class);

	// Sales Report
	public Workbook getSalesReportWorkBook(SalesReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Sales Report");
		try {
			excelReportMapping.setHeaderRowForSalesReport(sheet);
			int rowCount = 0;

			for (BillDetails bill : report.getBillList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addSalesReportRow(bill, row);
			}
			excelReportMapping.addTotalSalesReportRow(sheet, ++rowCount);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception:", e);
		}
		return workbook;
	}

	// Product Profit Report
	public Workbook getProductProfitReportWorkBook(ProductProfitReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Product Profit Report");
		try {
			excelReportMapping.setHeaderRowForProductProfit(sheet);
			int rowCount = 0;

			for (Product product : report.getProductList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addProductProfitRow(product, row);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception :", e);
		}
		return workbook;
	}

	// Stock Summary Report
	public Workbook getStockSummaryReportWorkBook(StockSummaryReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Stock Summary Report");
		try {
			excelReportMapping.setHeaderRowForStockSummary(sheet);
			int rowCount = 0;

			for (Product product : report.getProductList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addStockSummaryRow(product, row);
			}
			excelReportMapping.addTotalStockSummaryRow(sheet, ++rowCount);
		} catch (Exception e) {
			logger.error("Exception :", e);
			e.printStackTrace();
		}
		return workbook;
	}

	// Customer Report
	public Workbook getCustomersReportWorkBook(CustomersReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Customers Report");
		try {
			excelReportMapping.setHeaderRowForCustomers(sheet);
			int rowCount = 0;

			for (Customer cust : report.getCustomerList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addCustomersRow(cust, row);
			}
			excelReportMapping.addTotalCustomersRow(sheet, ++rowCount);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception :", e);
		}
		return workbook;
	}

	// Low Stock Summary Report
	public Workbook getLowStockSummaryReportWorkBook(LowStockSummaryReport lowStockSummaryReport, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Low Stock Summary Report");
		try {
			excelReportMapping.setHeaderRowForLowStockSummary(sheet);
			int rowCount = 0;

			for (Product p : lowStockSummaryReport.getProductList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addLowStockSummaryRow(p, row);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception :", e);
		}
		return workbook;
	}

	// Product Category Wise Stock Report
	public Workbook getCategoryWiseStockReportWorkBook(ProductCategoryWiseStockReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Product Category Wise Stock Report");
		try {
			excelReportMapping.setHeaderRowForCategoryWiseStock(sheet);
			int rowCount = 0;

			for (ProductCategory productCategory : report.getProductCategoryList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addCategoryWiseStockRow(productCategory, row);
			}
		} catch (Exception e) {
			logger.error("Exception :", e);
			e.printStackTrace();
		}
		return workbook;
	}

	// Sales Return Report
	public Workbook getSalesReturnReportWorkBook(SalesReturnReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Sales Return Report");
		try {
			excelReportMapping.setHeaderRowForSalesReturnReport(sheet);
			int rowCount = 0;

			for (ReturnDetails rd : report.getReturnList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addSalesReturnReportRow(rd, row);
			}
			excelReportMapping.addTotalSalesReturnReportRow(sheet, ++rowCount);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception:", e);
		}
		return workbook;
	}

	// Expense Report
	public Workbook getExpenseReportWorkBook(ExpenseReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Expense Report");
		try {
			excelReportMapping.setHeaderRowForExpenseReport(sheet);
			int rowCount = 0;

			for (Expense ex : report.getExpenseList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addExpenseReportRow(ex, row);
			}
			excelReportMapping.addTotalExpenseReportRow(sheet, ++rowCount);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception:", e);
		}
		return workbook;
	}

	// Suppliers Report
	public Workbook getSuppliersReportWorkBook(SuppliersReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Suppliers Report");
		try {
			excelReportMapping.setHeaderRowForSuppliers(sheet);
			int rowCount = 0;

			for (Supplier supplier : report.getSuppliersList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addSuppliersRow(supplier, row);
			}
			excelReportMapping.addTotalSuppliersRow(sheet, ++rowCount);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception:", e);
		}
		return workbook;
	}

	// GSTR1 Report
	public Workbook getGstr1ReportWorkBook(GSTR1Report report, Workbook workbook) {
		Sheet salesSheet = workbook.createSheet("Sales");
		Sheet salesReturnSheet = workbook.createSheet("Sales Return");
		try {
			excelReportMapping.setHeaderRowForGSTR1SalesReport(salesSheet,report);
			excelReportMapping.setHeaderRowForGSTR1SalesReturnReport(salesReturnSheet,report);
			int rowCount = 8;

			for (GSTR1Data rd : report.getInvoiceList()) {
				Row row = salesSheet.createRow(++rowCount);
				excelReportMapping.addGstr1SalesReportRow(rd, row);
			}
			excelReportMapping.addGstr1TotalSalesReportRow(salesSheet, ++rowCount);
			rowCount = 8;
			for (GSTR1Data rd : report.getSaleReturnList()) {
				Row row = salesReturnSheet.createRow(++rowCount);
				excelReportMapping.addGstr1SalesReturnReportRow(rd, row);
			}
			excelReportMapping.addGstr1TotalSalesReturnReportRow(salesReturnSheet, ++rowCount);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception:", e); 
		}
		return workbook;
	}

}

