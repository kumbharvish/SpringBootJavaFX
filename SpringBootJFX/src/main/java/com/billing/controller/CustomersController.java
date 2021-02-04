package com.billing.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.CustomerHistoryService;
import com.billing.service.CustomerService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

@Controller
public class CustomersController extends AppContext implements TabContent {

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private final String ADD = "ADD";

	private final String SETTLEUP = "SETTLEUP";

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	CustomerService customerService;

	@Autowired
	CustomerHistoryService customerHistoryService;

	@Autowired
	AppUtils appUtils;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	FilteredList<Customer> filteredList;

	@FXML
	private TextField txtCustName;

	@FXML
	private Label lblCustNameErrMsg;

	@FXML
	private TextField txtMobileNo;

	@FXML
	private Label lblMobileNoErrMsg;

	@FXML
	private TextField txtCity;

	@FXML
	private TextField txtEmail;

	@FXML
	private TextField txtBalanceAmount;

	@FXML
	private TextField txtAddress;

	@FXML
	private TextField txtState;

	@FXML
	private TextField txtGstin;

	@FXML
	private Button btnAdd;

	@FXML
	private Button btnUpdate;

	@FXML
	private Button btnDelete;

	@FXML
	private Button btnReset;

	@FXML
	private Button btnSettleUp;

	@FXML
	private TextField txtSearchCustomer;

	@FXML
	private TableView<Customer> tableView;

	@FXML
	private TableColumn<Customer, String> tcMobileNo;

	@FXML
	private TableColumn<Customer, String> tcName;

	@FXML
	private TableColumn<Customer, String> tcCity;

	@FXML
	private TableColumn<Customer, String> tcEmail;

	@FXML
	private TableColumn<Customer, Double> tcPendingAmount;

	@FXML
	private TableColumn<Customer, String> tcState;

	@FXML
	private TableColumn<Customer, String> tcGstin;

	@FXML
	private TableColumn<Customer, String> tcAddress;

	@FXML
	void onASettleUpCommand(ActionEvent event) {
		Customer customer = tableView.getSelectionModel().getSelectedItem();
		if (txtMobileNo.getText().equals("")) {
			alertHelper.showErrorNotification("Please select customer");
		} else {
			getPopup(customer, SETTLEUP);
		}
	}

	@FXML
	void onAddAmountCommand(ActionEvent event) {
		Customer customer = tableView.getSelectionModel().getSelectedItem();
		if (txtMobileNo.getText().equals("")) {
			alertHelper.showErrorNotification("Please select customer");
		} else {
			getPopup(customer, ADD);
		}
	}

	@Override
	public void initialize() {
		// Error Messages
		lblMobileNoErrMsg.managedProperty().bind(lblMobileNoErrMsg.visibleProperty());
		lblMobileNoErrMsg.visibleProperty().bind(lblMobileNoErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblCustNameErrMsg.managedProperty().bind(lblCustNameErrMsg.visibleProperty());
		lblCustNameErrMsg.visibleProperty().bind(lblCustNameErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		setTableCellFactories();
		// Force Number Listner
		txtMobileNo.textProperty().addListener(appUtils.getForceNumberListner());
		// Table row selection
		tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedRowChanged);

		txtSearchCustomer.textProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					if (newValue == null || newValue.isEmpty()) {
						filteredList.setPredicate(null);
					} else {
						filteredList.setPredicate((Customer t) -> {
							// Compare name and mobile number
							String lowerCaseFilter = newValue.toLowerCase();
							if (t.getCustName().toLowerCase().contains(lowerCaseFilter)) {
								return true;
							} else if (String.valueOf(t.getCustMobileNumber()).contains(lowerCaseFilter)) {
								return true;
							}
							return false;
						});

					}
				});
	}

	private void setTableCellFactories() {

		final Callback<TableColumn<Customer, Double>, TableCell<Customer, Double>> callback = new Callback<TableColumn<Customer, Double>, TableCell<Customer, Double>>() {
			@Override
			public TableCell<Customer, Double> call(TableColumn<Customer, Double> param) {
				TableCell<Customer, Double> tableCell = new TableCell<Customer, Double>() {

					@Override
					protected void updateItem(Double item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							super.setText(null);
						} else {
							super.setText(IndianCurrencyFormatting.applyFormatting(item));
						}
					}
				};
				tableCell.getStyleClass().add("numeric-cell");
				return tableCell;
			}
		};

		// Table Column Mapping
		tcMobileNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getCustMobileNumber())));
		tcName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustName()));
		tcCity.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustCity()));
		tcEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustEmail()));
		tcGstin.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGstin()));
		tcAddress.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));
		tcState.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getState()));
		tcPendingAmount.setCellFactory(callback);
		// Set CSS
		tcMobileNo.getStyleClass().add("numeric-cell");
		tcName.getStyleClass().add("character-cell");
		tcCity.getStyleClass().add("character-cell");
		tcEmail.getStyleClass().add("character-cell");
		tcAddress.getStyleClass().add("character-cell");
		tcState.getStyleClass().add("character-cell");
		tcGstin.getStyleClass().add("character-cell");
	}

	public void onSelectedRowChanged(ObservableValue<? extends Customer> observable, Customer oldValue,
			Customer newValue) {
		resetFields();
		if (newValue != null) {
			setCustomerDetails(newValue);
		}
	}

	private void setCustomerDetails(Customer customer) {
		txtMobileNo.setText(String.valueOf(customer.getCustMobileNumber()));
		txtMobileNo.setDisable(true);
		txtCustName.setText(customer.getCustName());
		txtCity.setText(customer.getCustCity());
		txtEmail.setText(customer.getCustEmail());
		txtState.setText(customer.getState());
		txtAddress.setText(customer.getAddress());
		txtGstin.setText(customer.getGstin());
		txtBalanceAmount.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(customer.getBalanceAmt()));
	}

	@FXML
	void onAddCommand(ActionEvent event) {
		if (!validateInput()) {
			return;
		}
		saveData();
	}

	@FXML
	void onDeleteCommand(ActionEvent event) {
		if (txtMobileNo.getText().equals("")) {
			alertHelper.showErrorNotification("Please select customer");
		} else {
			List<BillDetails> billList = customerHistoryService.getBillDetails(Long.parseLong(txtMobileNo.getText()));
			if (billList.size() > 0) {
				alertHelper.showErrorAlert(currentStage, "Error", null,
						"The selected customer can not be deleted,since there is other data related to this customer in system");
			} else {
				Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure?");
				if (alert.getResult() == ButtonType.YES) {
					Customer customer = new Customer();
					customer.setCustMobileNumber(Long.parseLong(txtMobileNo.getText()));
					customerService.delete(customer);
					alertHelper.showSuccessNotification("Customer deleted successfully");
					resetFields();
					loadData();
				} else {
					resetFields();
				}

			}
		}
	}

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onResetCommand(ActionEvent event) {
		resetFields();
		tableView.getSelectionModel().clearSelection();
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
		if (txtMobileNo.getText().equals("")) {
			alertHelper.showErrorNotification("Please select customer");
		} else {
			if (!validateInput()) {
				return;
			}
			updateData();
		}
	}

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtMobileNo.requestFocus();
	}

	@Override
	public boolean loadData() {
		List<Customer> list = customerService.getAll();
		ObservableList<Customer> tableData = FXCollections.observableArrayList();
		tableData.addAll(list);
		filteredList = new FilteredList(tableData, null);
		tableView.setItems(filteredList);
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
		Customer customer = getCustomerDetails();
		StatusDTO status = customerService.add(customer);
		if (status.getStatusCode() == 0) {
			alertHelper.showSuccessNotification("Customer added successfully");
			resetFields();
			loadData();
		} else {
			if (status.getStatusCode() == -1 && status.getException().contains("UNIQUE")) {
				alertHelper
						.showErrorNotification("Customer already exists for mobile number : " + txtMobileNo.getText());
			} else {
				alertHelper.showDataSaveErrAlert(currentStage);
			}
		}
		return true;
	}

	private Customer getCustomerDetails() {
		Customer customer = new Customer();
		customer.setCustMobileNumber(Long.valueOf(txtMobileNo.getText()));
		customer.setCustName(txtCustName.getText());
		customer.setCustCity(txtCity.getText());
		customer.setCustEmail(txtEmail.getText());
		customer.setState(txtState.getText());
		customer.setAddress(txtAddress.getText());
		customer.setGstin(txtGstin.getText());
		return customer;
	}

	private void updateData() {
		Customer customer = getCustomerDetails();
		StatusDTO status = customerService.update(customer);
		if (status.getStatusCode() == 0) {
			alertHelper.showSuccessNotification("Customer updated successfully");
			resetFields();
			loadData();
		} else {
			alertHelper.showErrorNotification("Customer mobile does not exists : " + txtMobileNo.getText());
		}
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
		// Mobile Number
		int name = txtMobileNo.getText().trim().length();
		if (name == 0) {
			alertHelper.beep();
			lblMobileNoErrMsg.setText("Please enter mobile number");
			txtMobileNo.requestFocus();
			valid = false;
		} else {
			lblMobileNoErrMsg.setText("");
		}

		// Customer Name
		int mUnit = txtCustName.getText().trim().length();
		if (mUnit == 0) {
			alertHelper.beep();
			lblCustNameErrMsg.setText("Please enter customer name");
			txtCustName.requestFocus();
			valid = false;
		} else {
			lblCustNameErrMsg.setText("");
		}

		return valid;
	}

	private void resetFields() {
		txtMobileNo.setText("");
		txtMobileNo.setDisable(false);
		txtCustName.setText("");
		txtCity.setText("");
		txtEmail.setText("");
		// Reset Error msg
		lblCustNameErrMsg.setText("");
		lblMobileNoErrMsg.setText("");
		txtBalanceAmount.setText("");
		txtState.setText("");
		txtGstin.setText("");
		txtAddress.setText("");
	}

	private void getPopup(Customer customer, String type) {

		Dialog<String> dialog = new Dialog<>();
		if (ADD.equals(type)) {
			dialog.setTitle("Add Pending Amount");
		} else {
			dialog.setTitle("Settle Up");
		}

		final String styleSheetPath = "/css/alertDialog.css";
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getStylesheets().add(AlertHelper.class.getResource(styleSheetPath).toExternalForm());

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));

		// Set the button types.
		ButtonType updateButtonType = new ButtonType("Update", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 100, 10, 10));
		TextField txtAmount = new TextField();
		txtAmount.setPrefColumnCount(10);
		txtAmount.textProperty().addListener(appUtils.getForceDecimalNumberListner());

		Label lbl = new Label("Amount :");
		lbl.getStyleClass().add("nodeLabel");

		grid.add(lbl, 0, 0);
		grid.add(txtAmount, 1, 0);

		Node validateButton = dialog.getDialogPane().lookupButton(updateButtonType);
		validateButton.setDisable(true);

		txtAmount.textProperty().addListener((observable, oldValue, newValue) -> {
			validateButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> txtAmount.requestFocus());

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == updateButtonType) {
				return txtAmount.getText();
			}
			return null;
		});

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(amount -> {
			try {
				if (amount != null && amount != "") {
					if (ADD.equals(type)) {
						// Add
						if (Double.valueOf(amount) > 0) {
							Customer cust = new Customer();
							cust.setCustMobileNumber(Long.valueOf(txtMobileNo.getText()));
							cust.setCustName(txtCustName.getText());
							cust.setAmount(Double.valueOf(amount));

							String narration = "Added by : " + userDetails.getFirstName() + " "
									+ userDetails.getLastName();

							StatusDTO statusAddAmt = customerService.addCustomerPaymentHistory(
									Long.valueOf(txtMobileNo.getText()), Double.valueOf(txtAmount.getText()), 0,
									AppConstants.CREDIT, narration);

							if (statusAddAmt.getStatusCode() == 0) {
								alertHelper.showSuccessNotification("Pending amount updated successfully");
								Customer cust1 = tableView.getSelectionModel().getSelectedItem();
								loadData();
								// customer is not selected in table
								if (cust1 == null) {
									cust1 = cust;
								}
								setUpdatedCustBalance(cust1.getCustMobileNumber());
							} else {
								alertHelper.showErrorNotification("Error occured while adding amount");
							}
						} else {
							alertHelper.showErrorNotification("Amount should be greater than Zero (0)");
						}

					} else {
						// Settle up
						if (Double.valueOf(amount) > 0 && Double.valueOf(amount) <= customer.getBalanceAmt()) {
							Customer customerSt = new Customer();
							customerSt.setCustMobileNumber(Long.valueOf(txtMobileNo.getText()));
							customerSt.setCustName(txtCustName.getText());
							customerSt.setAmount(Double.valueOf(txtAmount.getText()));
							String narration = "Settled up by : " + userDetails.getFirstName() + " "
									+ userDetails.getLastName();
							StatusDTO statusAddAmt = customerService.addCustomerPaymentHistory(
									Long.valueOf(txtMobileNo.getText()), 0, Double.valueOf(txtAmount.getText()),
									AppConstants.DEBIT, narration);

							if (statusAddAmt.getStatusCode() == 0) {
								alertHelper.showSuccessNotification("Pending amount updated succesfully");
								Customer cust = tableView.getSelectionModel().getSelectedItem();
								loadData();
								// customer is not selected in table
								if (cust == null) {
									cust = customerSt;
								}
								setUpdatedCustBalance(cust.getCustMobileNumber());
							} else {
								alertHelper.showErrorNotification("Error occured while settling up");
							}
						} else {
							alertHelper.showErrorNotification("Amount should be less than current balance");
						}
					}

				} else {
					alertHelper.showErrorNotification("Please enter amount");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	protected void setUpdatedCustBalance(Long custMobileNumber) {
		Customer customer = customerService.getCustomer(custMobileNumber);
		setCustomerDetails(customer);
	}

}
