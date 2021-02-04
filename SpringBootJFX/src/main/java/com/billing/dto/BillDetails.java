package com.billing.dto;

import java.util.List;

public class BillDetails {
	
	private int billNumber;
	
	private String timestamp;
	
	private long customerMobileNo;
	
	private String customerName;
	
	private List<ItemDetails> itemDetails;
	
	private List<ItemDetails> copyItemDetails;
	
	private int noOfItems;
	
	private double totalQuantity;
	
	private double totalAmount;
	
	private String 	paymentMode;
	
	private double discount;
	
	private double discountAmt;
	
	private double netSalesAmt;
	
	private double purchaseAmt;
	
	private String gstType;
	
	private double gstAmount;
	
	private String createdBy;
	
	private String lastUpdated;
	
	private double copyNetSalesAmt;
	
	private long copyCustMobile;
	
	private String copyPaymode;
	
	private boolean isItemsEdited;
	
	private double profitAmount;
	
	private Customer customer;

	public int getBillNumber() {
		return billNumber;
	}

	public void setBillNumber(int billNumber) {
		this.billNumber = billNumber;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public long getCustomerMobileNo() {
		return customerMobileNo;
	}

	public void setCustomerMobileNo(long customerMobileNo) {
		this.customerMobileNo = customerMobileNo;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public List<ItemDetails> getItemDetails() {
		return itemDetails;
	}

	public void setItemDetails(List<ItemDetails> itemDetails) {
		this.itemDetails = itemDetails;
	}

	public int getNoOfItems() {
		return noOfItems;
	}

	public void setNoOfItems(int noOfItems) {
		this.noOfItems = noOfItems;
	}

	public double getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(double totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public double getDiscountAmt() {
		return discountAmt;
	}

	public void setDiscountAmt(double discountAmt) {
		this.discountAmt = discountAmt;
	}

	public double getNetSalesAmt() {
		return netSalesAmt;
	}

	public void setNetSalesAmt(double netSalesAmt) {
		this.netSalesAmt = netSalesAmt;
	}

	public double getPurchaseAmt() {
		return purchaseAmt;
	}

	public void setPurchaseAmt(double purchaseAmt) {
		this.purchaseAmt = purchaseAmt;
	}

	public String getGstType() {
		return gstType;
	}

	public void setGstType(String gstType) {
		this.gstType = gstType;
	}

	public double getGstAmount() {
		return gstAmount;
	}

	public void setGstAmount(double gstAmount) {
		this.gstAmount = gstAmount;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public List<ItemDetails> getCopyItemDetails() {
		return copyItemDetails;
	}

	public void setCopyItemDetails(List<ItemDetails> copyItemDetails) {
		this.copyItemDetails = copyItemDetails;
	}

	public double getCopyNetSalesAmt() {
		return copyNetSalesAmt;
	}

	public void setCopyNetSalesAmt(double copyNetSalesAmt) {
		this.copyNetSalesAmt = copyNetSalesAmt;
	}

	public boolean isItemsEdited() {
		return isItemsEdited;
	}

	public void setItemsEdited(boolean isItemsEdited) {
		this.isItemsEdited = isItemsEdited;
	}

	public long getCopyCustMobile() {
		return copyCustMobile;
	}

	public void setCopyCustMobile(long copyCustMobile) {
		this.copyCustMobile = copyCustMobile;
	}

	public String getCopyPaymode() {
		return copyPaymode;
	}

	public void setCopyPaymode(String copyPaymode) {
		this.copyPaymode = copyPaymode;
	}

	public double getProfitAmount() {
		return netSalesAmt-purchaseAmt;
	}

	public void setProfitAmount(double profitAmount) {
		this.profitAmount = profitAmount;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}
