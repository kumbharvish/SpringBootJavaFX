package com.billing.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.GSTDetails;
import com.billing.dto.ItemDetails;
import com.billing.dto.Product;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.CustomerService;
import com.billing.service.InvoiceService;
import com.billing.service.PrinterService;
import com.billing.service.ProductService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.AutoCompleteTextField;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Controller
public class CreateInvoiceController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(CreateInvoiceController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	CustomerService customerService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductService productService;

	@Autowired
	InvoiceService invoiceService;

	@Autowired
	PrinterService printerService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private SortedSet<String> customerEntries;

	private HashMap<String, Customer> customerMap;

	private SortedSet<String> productEntries;

	private HashMap<String, Product> productMap;

	private HashMap<Long, Product> productMapWithBarcode;

	ObservableList<Product> productTableData;

	private boolean isGSTInclusive = false;

	@FXML
	private DatePicker dpInvoiceDate;

	@FXML
	private TextField txtInvoiceNumber;

	@FXML
	private Label lblInvoiceDateErrMsg;

	@FXML
	private RadioButton rbBarcode;

	@FXML
	private RadioButton rbItemName;

	@FXML
	private AutoCompleteTextField txtCustomer;

	@FXML
	private AutoCompleteTextField txtItemName;

	@FXML
	private TextField txtItemBarcode;

	@FXML
	private Label lblCustomerErrMsg;

	@FXML
	private Label lblItemNameErrMsg;

	@FXML
	private TextField txtUnit;

	@FXML
	private TextField txtRate;

	@FXML
	private Label lblRateErrMsg;

	@FXML
	private TextField txtQuantity;

	@FXML
	private Label lblQuantityErrMsg;

	@FXML
	private TextField txtAmount;

	@FXML
	private Label lblNoItemError;

	@FXML
	private TableView<Product> tableView;

	@FXML
	private TableColumn<Product, String> tcItemName;

	@FXML
	private TableColumn<Product, String> tcUnit;

	@FXML
	private TableColumn<Product, String> tcQuantity;

	@FXML
	private TableColumn<Product, String> tcRate;

	@FXML
	private TableColumn<Product, String> tcDiscount;

	@FXML
	private TableColumn<Product, String> tcDiscountAmount;

	@FXML
	private TableColumn<Product, String> tcAmount;

	@FXML
	private TableColumn<Product, String> tcCGST;

	@FXML
	private TableColumn<Product, String> tcSGST;

	@FXML
	private TableColumn<Product, String> tcSGSTPercent;

	@FXML
	private TableColumn<Product, String> tcCGSTPercent;

	@FXML
	private ComboBox<String> cbPaymentModes;

	@FXML
	private TextField txtNoOfItems;

	@FXML
	private TextField txtTotalQty;

	@FXML
	private TextField txtSubTotal;

	@FXML
	private TextField txtDiscountPercent;

	@FXML
	private TextField txtDiscountAmt;

	@FXML
	private TextField txtNetSalesAmount;

	@FXML
	private TextField txtGstAmount;

	@FXML
	private TextField txtGstType;

	@FXML
	private CheckBox cbPrintOnSave;

	@FXML
	private Button btnSave;

	@Override
	public void initialize() {

		if ("Y".equalsIgnoreCase(appUtils.getAppDataValues("GST_INCLUSIVE"))) {
			isGSTInclusive = true;
			txtGstType.setText("Inclusive");
		} else {
			txtGstType.setText("Exclusive");
		}

		cbPrintOnSave.setSelected(appUtils.isTrue(appUtils.getAppDataValues("INVOICE_PRINT_ON_SAVE")));

		productTableData = FXCollections.observableArrayList();
		ToggleGroup radioButtonGroup = new ToggleGroup();
		rbBarcode.setToggleGroup(radioButtonGroup);
		rbItemName.setToggleGroup(radioButtonGroup);
		if ("BARCODE".equals(appUtils.getAppDataValues("INVOICE_PRODUCT_SEARCH_BY"))) {
			rbBarcode.setSelected(true);
		} else {
			rbItemName.setSelected(true);
		}
		dpInvoiceDate.setValue(LocalDate.now());
		dpInvoiceDate.setDayCellFactory(this::getDateCell);
		dpInvoiceDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			isDirty.set(true);
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpInvoiceDate.setValue(today);
				isDirty.set(true);
			}
		});
		txtItemName.managedProperty().bind(txtItemName.visibleProperty());
		txtItemName.visibleProperty().bind(rbItemName.selectedProperty());
		txtItemBarcode.managedProperty().bind(txtItemBarcode.visibleProperty());
		txtItemBarcode.visibleProperty().bind(rbBarcode.selectedProperty());

		// Error Messages
		lblItemNameErrMsg.managedProperty().bind(lblItemNameErrMsg.visibleProperty());
		lblItemNameErrMsg.visibleProperty().bind(lblItemNameErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblQuantityErrMsg.managedProperty().bind(lblQuantityErrMsg.visibleProperty());
		lblQuantityErrMsg.visibleProperty().bind(lblQuantityErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblCustomerErrMsg.managedProperty().bind(lblCustomerErrMsg.visibleProperty());
		lblCustomerErrMsg.visibleProperty().bind(lblCustomerErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblRateErrMsg.managedProperty().bind(lblRateErrMsg.visibleProperty());
		lblRateErrMsg.visibleProperty().bind(lblRateErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblInvoiceDateErrMsg.managedProperty().bind(lblInvoiceDateErrMsg.visibleProperty());
		lblInvoiceDateErrMsg.visibleProperty()
				.bind(lblInvoiceDateErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblNoItemError.managedProperty().bind(lblNoItemError.visibleProperty());
		lblNoItemError.visibleProperty().bind(lblNoItemError.textProperty().length().greaterThanOrEqualTo(1));

		populatePaymentModes();
		setTableCellFactories();
		getCustomerNameList();
		getProductNameList();
		txtCustomer.createTextField(customerEntries, () -> {
			setNewFocus();
			lblCustomerErrMsg.setText("");
			isDirty.set(true);
		});
		txtItemName.createTextField(productEntries, () -> setProductDetails());
		txtDiscountPercent.setText("0.0");
		txtInvoiceNumber.setText(String.valueOf(invoiceService.getNewInvoiceNumber()));
		// Force Number Listner
		txtQuantity.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		txtRate.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		txtDiscountPercent.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		txtItemBarcode.textProperty().addListener(appUtils.getForceNumberListner());
		tableView.setItems(productTableData);
		btnSave.disableProperty().bind(isDirty.not());
		// Register textfield listners

		cbPaymentModes.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					cbPaymentModes.show();
				}
			}
		});

		cbPaymentModes.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				isDirty.set(true);
			}
		});

		txtQuantity.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					if (!txtItemName.getText().equals("")) {
						addRecordToTable(productMap.get(txtItemName.getText()));
					}
				}
			}
		});

		txtQuantity.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				txtAmount.setText("");
				if (!txtQuantity.getText().equals("") && !ke.getCode().equals(KeyCode.PERIOD)
						&& !ke.getCode().equals(KeyCode.DECIMAL)) {
					setTxtAmount();
				}
			}
		});

		txtItemBarcode.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					if (!txtItemBarcode.getText().equals("")) {
						clearItemErrorFields();
						setProductDetailsWithBarCode(Long.valueOf(txtItemBarcode.getText().trim()));
						txtItemBarcode.setText("");
						setNewFocus();
					}
				}
			}
		});

		txtRate.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				txtAmount.setText("");
				if (!txtRate.getText().equals("") && !ke.getCode().equals(KeyCode.PERIOD)
						&& !ke.getCode().equals(KeyCode.DECIMAL)) {
					setTxtAmount();
				}
			}
		});

		rbBarcode.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				txtItemBarcode.requestFocus();
				resetItemFields();
			}
		});
		rbItemName.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				txtItemName.requestFocus();
				resetItemFields();
			}
		});

		productTableData.addListener(new ListChangeListener<Product>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends Product> c) {
				isDirty.set(true);
				updateInvoiceAmount();
				lblNoItemError.setText("");
			}
		});

		txtDiscountPercent.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (!txtDiscountPercent.getText().equals("") && !ke.getCode().equals(KeyCode.PERIOD)
						&& !ke.getCode().equals(KeyCode.DECIMAL)) {
					updateDiscount();
					isDirty.set(true);
				}
			}
		});

		txtDiscountPercent.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null || newValue.equals("")) {
				txtDiscountPercent.setText("0.0");
				isDirty.set(true);
			}
		});

		tableView.setOnMouseClicked((MouseEvent event) -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				// Show pop up with Edit and Remove Row option
				if (!productTableData.isEmpty() && null != tableView.getSelectionModel().getSelectedItem()) {
					getTableRowEditDeletePopup();
				}

			}
		});

	}

	private void setTxtAmount() {
		if (!txtRate.getText().equals("") && !txtQuantity.getText().equals("")) {
			Double pRate = Double.parseDouble(txtRate.getText());
			Double pQty = Double.parseDouble(txtQuantity.getText());
			Double pAmount = pQty * pRate;
			txtAmount.setText(appUtils.getDecimalFormat(pAmount));
			lblRateErrMsg.setText("");
		}
	}

	protected void addRecordToTable(Product product) {
		if (validateInvoiceItem(product)) {
			updateDiscountAllPerToProduct(product);
			product.setTableDispAmount(Double.valueOf(txtAmount.getText()));
			product.setTableDispRate(Double.valueOf(txtRate.getText()));
			product.setTableDispQuantity(Double.valueOf(txtQuantity.getText()));
			product.setGstDetails(appUtils.getGSTDetails(product));
			productTableData.add(product);
			resetItemFields();
			setNewFocus();
		}
	}

	// Set Product details for BarCode
	protected void setProductDetailsWithBarCode(Long productBarCode) {
		Product product = productMapWithBarcode.get(productBarCode);
		if (product == null) {
			lblItemNameErrMsg.setText("Product not preset for Barcode : " + productBarCode);
		} else {
			if (product.getQuantity() >= 1) {
				if (!updateRow(product)) {
					updateDiscountAllPerToProduct(product);
					product.setTableDispQuantity(1.0);
					// Total quantity - 1
					product.setQuantity(product.getQuantity() - 1);
					product.setTableDispAmount(product.getSellPrice());
					product.setTableDispRate(product.getSellPrice());
					product.setGstDetails(appUtils.getGSTDetails(product));
					productTableData.add(product);
				}
			} else {
				lblQuantityErrMsg.setText("Available stock is : " + appUtils.getDecimalFormat(product.getQuantity()));
				alertHelper.beep();
			}
		}

	}

	private void updateDiscountAllPerToProduct(Product product) {
		if (!txtDiscountPercent.getText().equals("") && Double.valueOf(txtDiscountPercent.getText()) > 0) {
			product.setDiscount(Double.valueOf(txtDiscountPercent.getText()));
		}
	}

	private boolean updateRow(Product product) {
		boolean isUpdated = false;
		for (int idx = 0; idx < productTableData.size(); idx++) {
			Product tableProduct = productTableData.get(idx);
			if (tableProduct.getProductCode() == product.getProductCode()) {
				updateDiscountAllPerToProduct(product);
				double newQty = tableProduct.getTableDispQuantity() + 1;
				double rate = tableProduct.getTableDispRate();
				double newAmt = rate * newQty;
				tableProduct.setTableDispQuantity(newQty);
				tableProduct.setTableDispAmount(newAmt);
				product.setQuantity(product.getQuantity() - 1);
				product.setGstDetails(appUtils.getGSTDetails(product));
				tableView.refresh();
				isUpdated = true;
				updateInvoiceAmount();
			}
		}
		return isUpdated;
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

	private void setTableCellFactories() {
		tcItemName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
		tcUnit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMeasure()));
		tcQuantity.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getTableDispQuantity())));
		tcRate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getTableDispRate())));
		tcAmount.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getTableAmountShowValue())));
		tcDiscountAmount.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getDiscountAmount())));
		tcCGST.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getCgst())));
		tcSGST.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getSgst())));
		tcCGSTPercent.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getCgstPercent())));
		tcSGSTPercent.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSgstPercent())));

		tcDiscount.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getDiscount())));

		tcItemName.getStyleClass().add("character-cell");
		tcQuantity.getStyleClass().add("numeric-cell");
		tcUnit.getStyleClass().add("character-cell");
		tcRate.getStyleClass().add("numeric-cell");
		tcAmount.getStyleClass().add("numeric-cell");
		tcDiscountAmount.getStyleClass().add("numeric-cell");
		tcCGST.getStyleClass().add("numeric-cell");
		tcDiscount.getStyleClass().add("numeric-cell");
		tcCGSTPercent.getStyleClass().add("numeric-cell");
		tcSGSTPercent.getStyleClass().add("numeric-cell");
		tcSGST.getStyleClass().add("numeric-cell");

	}

	public void populatePaymentModes() {
		for (String mode : appUtils.getPaymentModes()) {
			cbPaymentModes.getItems().add(mode);
		}
		cbPaymentModes.getSelectionModel().select(0);
	}

	public void getCustomerNameList() {
		customerEntries = new TreeSet<String>();
		customerMap = new HashMap<String, Customer>();
		for (Customer cust : customerService.getAll()) {
			customerEntries.add(cust.getCustMobileNumber() + " : " + cust.getCustName());
			customerMap.put(cust.getCustMobileNumber() + " : " + cust.getCustName(), cust);
		}
	}

	public void getProductNameList() {
		productEntries = new TreeSet<String>();
		productMap = new HashMap<String, Product>();
		productMapWithBarcode = new HashMap<Long, Product>();
		for (Product p : productService.getAll()) {
			productEntries.add(p.getProductName());
			productMap.put(p.getProductName(), p);
			productMapWithBarcode.put(p.getProductBarCode(), p);
		}
	}

	private void setProductDetails() {
		if (!txtItemName.getText().equals("")) {
			Product product = productMap.get(txtItemName.getText());
			txtQuantity.setText("");
			if (product != null) {
				txtUnit.setText(product.getMeasure());
				txtRate.setText(appUtils.getDecimalFormat(product.getSellPrice()));
				txtQuantity.requestFocus();
				clearItemErrorFields();
			}
		}
	}

	@FXML
	void onCloseAction(ActionEvent event) {
		if (shouldClose()) {
			closeTab();
		}
	}

	@FXML
	void onRefereshCommand(ActionEvent event) {
		getProductNameList();
		getCustomerNameList();
		txtCustomer.createTextField(customerEntries, () -> setNewFocus());
		txtItemName.createTextField(productEntries, () -> setProductDetails());
	}

	@FXML
	void onCashHelpCommand(ActionEvent event) {
		if (!txtNetSalesAmount.getText().equals("")) {
			getCashHelpPopup();
		}
	}

	@FXML
	void onGSTDetailsCommand(ActionEvent event) {
		getGSTDetailsPopUp();
	}

	@FXML
	void onSaveCommand(ActionEvent event) {
		if (!saveData()) {
			return;
		}
	}

	@Override
	public boolean shouldClose() {
		if (isDirty.get()) {
			ButtonType response = appUtils.shouldSaveUnsavedData(currentStage);
			if (response == ButtonType.CANCEL) {
				return false;
			}

			if (response == ButtonType.YES) {
				return saveData();
			}
		}
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtCustomer.requestFocus();
	}

	@Override
	public boolean loadData() {
		isDirty.set(false);
		return true;
	}

	@Override
	public void setMainWindow(Stage stage) {
		currentStage = stage;
	}

	@Override
	public void setTabPane(TabPane pane) {
		this.tabPane = pane;
	}

	@Override
	public void setUserDetails(UserDetails user) {
		userDetails = user;
	}

	@Override
	public boolean saveData() {
		Boolean saveStatus = true;
		if (!validateInput()) {
			alertHelper.beep();
			return false;
		}
		BillDetails bill = prepareBillDetails();
		StatusDTO status = invoiceService.add(bill);
		if (status.getStatusCode() != 0) {
			saveStatus = false;
		}
		if (saveStatus) {
			alertHelper.showSuccessNotification("Invoice saved successfully");
			// Reset Invoice UI Fields
			resetFields();
			// Print Invoice
			if (cbPrintOnSave.isSelected()) {
				printerService.printInvoice(bill);
			}
		} else {
			alertHelper.showErrorNotification("Error occured while saving invoice");
		}
		return saveStatus;
	}

	private BillDetails prepareBillDetails() {
		BillDetails bill = new BillDetails();
		bill.setBillNumber(Integer.valueOf(txtInvoiceNumber.getText()));
		Customer cust = customerMap.get(txtCustomer.getText());
		bill.setCustomerMobileNo(cust.getCustMobileNumber());
		bill.setCustomerName(cust.getCustName());
		bill.setCustomer(cust);
		// Prepare Item List
		bill.setItemDetails(prepareItemList());
		bill.setTotalAmount(Double.valueOf(IndianCurrencyFormatting.removeFormatting(txtSubTotal.getText())));
		bill.setNoOfItems(Integer.valueOf(txtNoOfItems.getText()));
		bill.setTotalQuantity(Double.valueOf(txtTotalQty.getText()));
		bill.setDiscount(Double.valueOf(txtDiscountPercent.getText()));
		bill.setDiscountAmt(Double.valueOf(IndianCurrencyFormatting.removeFormatting(txtDiscountAmt.getText())));
		bill.setPaymentMode(cbPaymentModes.getSelectionModel().getSelectedItem());
		bill.setNetSalesAmt(
				Double.valueOf(IndianCurrencyFormatting.removeFormattingWithCurrency(txtNetSalesAmount.getText())));

		String invoiceDate = dpInvoiceDate.getValue().format(appUtils.getDateTimeFormatter());
		String invoiceTime = appUtils.getCurrentTime();

		bill.setTimestamp(invoiceDate + " " + invoiceTime);
		bill.setPurchaseAmt(getBillPurchaseAmount());
		bill.setGstType(txtGstType.getText());
		bill.setGstAmount(Double.valueOf(IndianCurrencyFormatting.removeFormatting(txtGstAmount.getText())));
		bill.setCreatedBy(userDetails.getFirstName() + " " + userDetails.getLastName());
		return bill;
	}

	private double getBillPurchaseAmount() {
		double billPurchaseAmount = 0.0;
		for (Product p : productTableData) {
			billPurchaseAmount = billPurchaseAmount + (p.getPurcasePrice() * p.getTableDispQuantity());
		}
		return billPurchaseAmount;
	}

	private List<ItemDetails> prepareItemList() {
		List<ItemDetails> itemList = new ArrayList<>();
		for (Product p : productTableData) {
			ItemDetails item = new ItemDetails();
			item.setBillNumber(Integer.valueOf(txtInvoiceNumber.getText()));
			item.setGstDetails(p.getGstDetails());
			item.setItemName(p.getProductName());
			item.setItemNo(p.getProductCode());
			item.setRate(p.getTableDispRate());
			item.setQuantity(p.getTableDispQuantity());
			item.setMRP(p.getSellPrice());
			item.setPurchasePrice(p.getPurcasePrice());
			item.setDiscountPercent(p.getDiscount());
			item.setDiscountAmount(p.getDiscountAmount());
			item.setUnit(p.getMeasure());
			item.setHsn(p.getHsn());
			itemList.add(item);
		}
		return itemList;
	}

	@Override
	public void invalidated(Observable observable) {
		isDirty.set(true);
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		boolean valid = true;
		clearInvoiceErrorFields();

		final LocalDate date = dpInvoiceDate.getValue();
		if (date == null) {
			lblInvoiceDateErrMsg.setText("Invoice Date not specified!");
			valid = false;
			return valid;
		} else if (date.isAfter(LocalDate.now())) {
			lblInvoiceDateErrMsg.setText("Invoice Date can't be later than todays date :" + appUtils.getTodaysDate());
			valid = false;
			return valid;
		}
		int custLength = txtCustomer.getText().trim().length();
		if (custLength == 0) {
			lblCustomerErrMsg.setText("Please select customer");
			txtCustomer.requestFocus();
			valid = false;
			return valid;
		} else if (null == customerMap.get(txtCustomer.getText())) {
			lblCustomerErrMsg.setText("No customer matches this name");
			txtCustomer.requestFocus();
			valid = false;
			return valid;
		} else if (customerMap.get(txtCustomer.getText()).getBalanceAmt() < 0) {
			lblCustomerErrMsg.setText("Customer balance is negative. Please check customer payment history");
			txtCustomer.requestFocus();
			valid = false;
			return valid;
		} else {
			lblCustomerErrMsg.setText("");
		}

		if (productTableData.size() == 0) {
			lblNoItemError.setText("Please add product to invoice");
			txtItemName.requestFocus();
			valid = false;
			return valid;
		} else {
			lblNoItemError.setText("");
		}

		return valid;
	}

	private void resetFields() {
		resetItemFields();
		resetOrignalProductDiscount();
		productTableData.clear();
		dpInvoiceDate.setValue(LocalDate.now());
		txtDiscountPercent.setText("0.0");
		txtInvoiceNumber.setText(String.valueOf(invoiceService.getNewInvoiceNumber()));
		txtCustomer.clear();
		txtCustomer.requestFocus();
		txtNoOfItems.clear();
		txtTotalQty.clear();
		txtSubTotal.clear();
		txtDiscountAmt.clear();
		cbPaymentModes.getSelectionModel().select(0);
		txtGstAmount.clear();
		txtNetSalesAmount.clear();
		isDirty.set(false);
		getProductNameList();
		txtItemName.createTextField(productEntries, () -> setProductDetails());

	}

	private void resetOrignalProductDiscount() {
		for (Product p : productTableData) {
			p.setDiscount(p.getOrignalDiscount());
		}
	}

	private void resetItemFields() {
		txtItemName.clear();
		txtItemBarcode.clear();
		txtAmount.clear();
		txtRate.clear();
		txtQuantity.clear();
		txtUnit.clear();
	}

	protected void setNewFocus() {
		if (rbBarcode.isSelected()) {
			txtItemBarcode.requestFocus();
		} else {
			txtItemName.requestFocus();
		}
	}

	private void clearInvoiceErrorFields() {
		lblInvoiceDateErrMsg.setText("");
		lblCustomerErrMsg.setText("");
		lblNoItemError.setText("");
	}

	private boolean validateInvoiceItem(Product product) {
		clearItemErrorFields();
		boolean valid = true;
		if (txtItemName.getText().equals("")) {
			lblItemNameErrMsg.setText("Enter product name");
			valid = false;
		}
		if (null == product && !txtItemName.getText().equals("")) {
			lblItemNameErrMsg.setText("Invalid product name");
			valid = false;
		}
		String rate = txtRate.getText().trim();
		if (rate.isEmpty()) {
			lblRateErrMsg.setText("Rate not specified");
			alertHelper.beep();
			valid = false;
		}
		String quantity = txtQuantity.getText().trim();
		if (quantity.isEmpty()) {
			lblQuantityErrMsg.setText("Quantity not specified");
			alertHelper.beep();
			valid = false;
		} else if (0 == Double.valueOf(quantity)) {
			lblQuantityErrMsg.setText("Invalid Quantity");
			alertHelper.beep();
			valid = false;
		} else if (null != product && (product.getQuantity() < Double.valueOf(quantity))) {
			valid = false;
			lblQuantityErrMsg.setText("Available stock is : " + appUtils.getDecimalFormat(product.getQuantity()));
			alertHelper.beep();
		}
		boolean isMatch = productTableData.stream().anyMatch(i -> i.getProductName().equals(product.getProductName()));
		if (isMatch) {
			lblItemNameErrMsg.setText("Prodcut already added");
			valid = false;
		}

		return valid;
	}

	private void clearItemErrorFields() {
		lblItemNameErrMsg.setText("");
		lblQuantityErrMsg.setText("");
		lblRateErrMsg.setText("");
	}

	private void getCashHelpPopup() {

		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Cash Help");

		final String styleSheetPath = "/css/alertDialog.css";
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getStylesheets().add(AlertHelper.class.getResource(styleSheetPath).toExternalForm());

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));

		// Set the button types.
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 100, 10, 10));

		Label lbl = new Label("Net Sales Amount :");
		lbl.getStyleClass().add("nodeLabel");
		TextField txtNetTotal = new TextField();
		txtNetTotal.setPrefColumnCount(15);
		txtNetTotal.getStyleClass().add("readOnlyField");
		txtNetTotal.setEditable(false);
		txtNetTotal.setText(txtNetSalesAmount.getText());
		grid.add(lbl, 0, 0);
		grid.add(txtNetTotal, 1, 0);

		Label lblCashAmt = new Label("Cash Amount :");
		lblCashAmt.getStyleClass().add("nodeLabel");
		TextField txtCashAmt = new TextField();
		txtCashAmt.setPrefColumnCount(15);
		txtCashAmt.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		grid.add(lblCashAmt, 0, 1);
		grid.add(txtCashAmt, 1, 1);

		Label lblRAmt = new Label("Return Amount :");
		lblRAmt.getStyleClass().add("nodeLabel");
		TextField txtReturnAmt = new TextField();
		txtReturnAmt.setPrefColumnCount(15);
		txtReturnAmt.getStyleClass().add("readOnlyField");
		txtReturnAmt.getStyleClass().add("summation");
		txtReturnAmt.setEditable(false);
		grid.add(lblRAmt, 0, 2);
		grid.add(txtReturnAmt, 1, 2);
		GridPane.setHalignment(lbl, HPos.RIGHT);
		GridPane.setHalignment(lblCashAmt, HPos.RIGHT);
		GridPane.setHalignment(lblRAmt, HPos.RIGHT);

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> txtCashAmt.requestFocus());

		txtCashAmt.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				txtReturnAmt.setText("");
				if (!txtCashAmt.getText().equals("") && !ke.getCode().equals(KeyCode.PERIOD)
						&& !ke.getCode().equals(KeyCode.DECIMAL)) {
					double netTotal = Double.valueOf(
							IndianCurrencyFormatting.removeFormattingWithCurrency(txtNetSalesAmount.getText()));
					double cashAmt = Double.valueOf(txtCashAmt.getText());
					txtReturnAmt.setText(appUtils.getDecimalFormat(cashAmt - netTotal));
				}
			}
		});

		dialog.showAndWait();
	}

	private void getTableRowEditDeletePopup() {

		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Action");

		final String styleSheetPath = "/css/alertDialog.css";
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getStylesheets().add(AlertHelper.class.getResource(styleSheetPath).toExternalForm());

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));
		HBox box = new HBox();
		Label lbl = new Label("Please choose action on product");
		lbl.getStyleClass().add("nodeLabel");
		box.getChildren().add(lbl);
		box.setPrefHeight(55.0);
		dialog.getDialogPane().setContent(box);
		// Set the button types.
		ButtonType updateButtonType = new ButtonType("Edit", ButtonData.OK_DONE);
		ButtonType deleteButtonType = new ButtonType("Remove", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, deleteButtonType, ButtonType.CANCEL);
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == updateButtonType) {
				Product p = tableView.getSelectionModel().getSelectedItem();
				if (productTableData.contains(p)) {
					productTableData.remove(p);
					txtItemName.setText(p.getProductName());
					setProductDetails();
				}
			} else if (dialogButton == deleteButtonType) {
				Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure?");
				if (alert.getResult() == ButtonType.YES) {
					Product p = tableView.getSelectionModel().getSelectedItem();
					if (productTableData.contains(p)) {
						productTableData.remove(p);
					}
				}
			}
			return null;
		});
		dialog.showAndWait();
	}

	private void updateInvoiceAmount() {
		double subTotal = 0.0;
		double quantity = 0.0;
		int noOfItems = 0;
		double gstAmount = 0.0;
		double discountAmount = 0.0;
		double netSalesAmount = 0.0;
		noOfItems = productTableData.size();
		for (Product product : productTableData) {
			GSTDetails gst = product.getGstDetails();
			subTotal = subTotal + (product.getTableDispRate() * product.getTableDispQuantity());
			quantity = quantity + product.getTableDispQuantity();
			gstAmount = gstAmount + gst.getGstAmount();
			discountAmount = discountAmount + product.getDiscountAmount();
		}
		if (isGSTInclusive) {
			subTotal = subTotal - gstAmount;
		}

		netSalesAmount = (subTotal - discountAmount) + gstAmount;
		txtNoOfItems.setText(String.valueOf(noOfItems));
		txtTotalQty.setText(appUtils.getDecimalFormat(quantity));
		txtSubTotal.setText(IndianCurrencyFormatting.applyFormatting(subTotal));
		txtDiscountAmt.setText(IndianCurrencyFormatting.applyFormatting(discountAmount));
		txtGstAmount.setText(IndianCurrencyFormatting.applyFormatting(gstAmount));
		txtNetSalesAmount.setText(
				IndianCurrencyFormatting.applyFormattingWithCurrency(appUtils.getDecimalRoundUp(netSalesAmount)));
	}

	private void updateDiscount() {
		double discountAllPercent = Double.valueOf(txtDiscountPercent.getText());
		for (Product p : productTableData) {
			p.setDiscount(discountAllPercent);
			p.setGstDetails(appUtils.getGSTDetails(p));
		}
		tableView.refresh();
		updateInvoiceAmount();
	}

	private void getGSTDetailsPopUp() {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/ViewGSTDetails.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getGSTDetailsPopUp Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final GSTDetailsController controller = (GSTDetailsController) fxmlLoader.getController();
		controller.productList = productTableData;
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("GST Details");
		controller.loadData();
		stage.showAndWait();
	}

}
