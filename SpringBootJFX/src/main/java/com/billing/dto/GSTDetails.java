package com.billing.dto;

public class GSTDetails {

	private String name;

	private double rate;

	private double cgst;

	private double sgst;

	private double gstAmount;

	private String inclusiveFlag;

	private double taxableAmount;

	private double cgstPercent;

	private double sgstPercent;
	
	private double totalItemAmount;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public double getCgst() {
		return cgst;
	}

	public void setCgst(double cgst) {
		this.cgst = cgst;
	}

	public double getSgst() {
		return sgst;
	}

	public void setSgst(double sgst) {
		this.sgst = sgst;
	}

	public double getGstAmount() {
		return gstAmount;
	}

	public void setGstAmount(double gstAmount) {
		this.gstAmount = gstAmount;
	}

	public String getInclusiveFlag() {
		return inclusiveFlag;
	}

	public void setInclusiveFlag(String inclusiveFlag) {
		this.inclusiveFlag = inclusiveFlag;
	}

	public double getTaxableAmount() {
		return taxableAmount;
	}

	public void setTaxableAmount(double taxableAmount) {
		this.taxableAmount = taxableAmount;
	}

	public double getCgstPercent() {
		return rate/2;
	}

	public double getSgstPercent() {
		return rate/2;
	}

	public double getTotalItemAmount() {
		return totalItemAmount;
	}

	public void setTotalItemAmount(double totalItemAmount) {
		this.totalItemAmount = totalItemAmount;
	}
}
