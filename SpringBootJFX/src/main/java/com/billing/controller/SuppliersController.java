package com.billing.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.StatusDTO;
import com.billing.dto.Supplier;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.SupplierService;
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
import javafx.event.EventHandler;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

@Controller
public class SuppliersController extends AppContext implements TabContent {

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	SupplierService supplierService;

	@Autowired
	AppUtils appUtils;

	int supplierId;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	FilteredList<Supplier> filteredList;

	@FXML
	private TextField txtName;

	@FXML
	private Label lblNameErrMsg;

	@FXML
	private TextField txtMobileNo;

	@FXML
	private Label lblMobileNoErrMsg;

	@FXML
	private TextField txtBalanceAmount;

	@FXML
	private TextField txtGSTNo;

	@FXML
	private TextField txtPAN;

	@FXML
	private Button btnAdd;

	@FXML
	private Button btnUpdate;

	@FXML
	private Button btnDelete;

	@FXML
	private Button btnReset;

	@FXML
	private TextField txtComments;

	@FXML
	private TextField txtEmail;

	@FXML
	private TextField txtAddress;

	@FXML
	private TextField txtCity;

	@FXML
	private TableView<Supplier> tableView;

	@FXML
	private TableColumn<Supplier, String> tcMobileNo;

	@FXML
	private TableColumn<Supplier, String> tcName;

	@FXML
	private TableColumn<Supplier, Double> tcBalanceAmount;

	@FXML
	private TableColumn<Supplier, String> tcCity;

	@FXML
	private TableColumn<Supplier, String> tcEmail;

	@FXML
	private TableColumn<Supplier, String> tcAddress;

	@FXML
	private TableColumn<Supplier, String> tcGSTNo;

	@FXML
	private TableColumn<Supplier, String> tcPAN;

	@FXML
	private TableColumn<Supplier, String> tcComments;

	@FXML
	private TextField txtSearchSupplier;

	private final String ADD = "ADD";

	private final String SETTLEUP = "SETTLEUP";

	@Override
	public void initialize() {
		// Error Messages
		lblMobileNoErrMsg.managedProperty().bind(lblMobileNoErrMsg.visibleProperty());
		lblMobileNoErrMsg.visibleProperty().bind(lblMobileNoErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblNameErrMsg.managedProperty().bind(lblNameErrMsg.visibleProperty());
		lblNameErrMsg.visibleProperty().bind(lblNameErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		setTableCellFactories();
		// Force Number Listner
		txtMobileNo.textProperty().addListener(appUtils.getForceNumberListner());
		// Table row selection
		tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedRowChanged);
		supplierId = 0; // Reset Supplier Id
		txtSearchSupplier.textProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					if (newValue == null || newValue.isEmpty()) {
						filteredList.setPredicate(null);
					} else {
						filteredList.setPredicate((Supplier t) -> {
							// Compare name and Mobile number
							String lowerCaseFilter = newValue.toLowerCase();
							if (t.getSupplierName().toLowerCase().contains(lowerCaseFilter)) {
								return true;
							} else if (String.valueOf(t.getSupplierMobile()).contains(lowerCaseFilter)) {
								return true;
							}
							return false;
						});
					}
				});
	}

	private void setTableCellFactories() {

		final Callback<TableColumn<Supplier, Double>, TableCell<Supplier, Double>> callback = new Callback<TableColumn<Supplier, Double>, TableCell<Supplier, Double>>() {
			@Override
			public TableCell<Supplier, Double> call(TableColumn<Supplier, Double> param) {
				TableCell<Supplier, Double> tableCell = new TableCell<Supplier, Double>() {

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
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSupplierMobile())));
		tcName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplierName()));
		tcEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmailId()));
		tcGSTNo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGstNo()));
		tcPAN.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPanNo()));
		tcComments.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getComments()));
		tcAddress.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplierAddress()));
		tcCity.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCity()));
		tcBalanceAmount.setCellFactory(callback);
		// Set CSS
		tcMobileNo.getStyleClass().add("numeric-cell");
		tcName.getStyleClass().add("character-cell");
		tcCity.getStyleClass().add("character-cell");
		tcEmail.getStyleClass().add("character-cell");
		tcGSTNo.getStyleClass().add("character-cell");
		tcAddress.getStyleClass().add("character-cell");
		tcComments.getStyleClass().add("character-cell");
		tcPAN.getStyleClass().add("character-cell");
	}

	public void onSelectedRowChanged(ObservableValue<? extends Supplier> observable, Supplier oldValue,
			Supplier newValue) {
		resetFields();
		if (newValue != null) {
			setSupplierDetails(newValue);
		}
	}

	private void setSupplierDetails(Supplier newValue) {
		txtMobileNo.setText(String.valueOf(newValue.getSupplierMobile()));
		txtName.setText(newValue.getSupplierName());
		txtCity.setText(newValue.getCity());
		txtEmail.setText(newValue.getEmailId());
		txtAddress.setText(newValue.getSupplierAddress());
		txtGSTNo.setText(newValue.getGstNo());
		txtPAN.setText(newValue.getPanNo());
		txtComments.setText(newValue.getComments());
		supplierId = newValue.getSupplierID();
		txtBalanceAmount.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(newValue.getBalanceAmount()));
	}

	@FXML
	void onAddCommand(ActionEvent event) {
		if (supplierId != 0) {
			alertHelper.showErrorNotification("Please reset fields");
		} else {
			if (!validateInput()) {
				return;
			}
			saveData();
		}
	}

	@FXML
	void onDeleteCommand(ActionEvent event) {
		if (supplierId == 0) {
			alertHelper.showErrorNotification("Please select supplier");
		} else {
			if (!validateInput()) {
				return;
			}
			deleteData();
		}
	}

	private void deleteData() {
		StatusDTO status = supplierService.isSupplierEntryAvailable(supplierId);
		if (status.getStatusCode() == 0) {
			alertHelper.showErrorNotification("Delete operation not allowed. Purchase entry present for this supplier");
		} else {
			Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure?");
			if (alert.getResult() == ButtonType.YES) {
				Supplier supplier = new Supplier();
				supplier.setSupplierID(supplierId);
				StatusDTO statusDelete = supplierService.delete(supplier);
				if (statusDelete.getStatusCode() == 0) {
					resetFields();
					loadData();
					alertHelper.showSuccessNotification("Supplier deleted successfully");
				} else {
					alertHelper.showDataDeleteErrAlert(currentStage);
				}

			} else {
				resetFields();
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
		if (supplierId == 0) {
			alertHelper.showErrorNotification("Please select supplier");
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
		txtName.requestFocus();
	}

	@Override
	public boolean loadData() {
		List<Supplier> list = supplierService.getAll();
		ObservableList<Supplier> tableData = FXCollections.observableArrayList();
		tableData.addAll(list);
		filteredList = new FilteredList(tableData, null);
		tableView.setItems(filteredList);
		// Set Shortcuts
		// Add
		KeyCombination kc = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_ANY);
		Runnable rn = () -> onAddCommand(null);
		currentStage.getScene().getAccelerators().put(kc, rn);

		// Update
		KeyCombination ku = new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_ANY);
		Runnable ru = () -> onUpdateCommand(null);
		currentStage.getScene().getAccelerators().put(ku, ru);

		// Delete
		KeyCombination kd = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_ANY);
		Runnable rd = () -> onDeleteCommand(null);
		currentStage.getScene().getAccelerators().put(kd, rd);

		// Reset
		KeyCombination kr = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_ANY);
		Runnable rr = () -> onResetCommand(null);
		currentStage.getScene().getAccelerators().put(kr, rr);
		setEnterButtonRules();
		return true;
	}

	private void setEnterButtonRules() {
		// Mobile No
		txtName.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (!txtName.getText().equals("") && ke.getCode().equals(KeyCode.ENTER)) {
					txtMobileNo.requestFocus();
				}
			}
		});
		txtMobileNo.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (!txtMobileNo.getText().equals("") && ke.getCode().equals(KeyCode.ENTER)) {
					txtGSTNo.requestFocus();
				}
			}
		});
		txtGSTNo.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					txtPAN.requestFocus();
				}
			}
		});
		txtPAN.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					txtCity.requestFocus();
				}
			}
		});
		txtCity.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					txtAddress.requestFocus();
				}
			}
		});
		txtAddress.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					txtEmail.requestFocus();
				}
			}
		});
		txtEmail.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					txtComments.requestFocus();
				}
			}
		});
		txtComments.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					btnAdd.requestFocus();
				}
			}
		});

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
		Supplier sp = new Supplier();
		sp.setSupplierName(txtName.getText());
		sp.setCity(txtCity.getText());
		sp.setSupplierAddress(txtAddress.getText());
		sp.setEmailId(txtEmail.getText());
		sp.setComments(txtComments.getText());
		sp.setSupplierMobile(Long.parseLong(txtMobileNo.getText()));
		sp.setPanNo(txtPAN.getText());
		sp.setGstNo(txtGSTNo.getText());
		StatusDTO status = supplierService.add(sp);
		if (status.getStatusCode() == 0) {
			resetFields();
			loadData();
			alertHelper.showSuccessNotification("Supplier added successfully");
		} else {
			alertHelper.showDataSaveErrAlert(currentStage);
		}
		return true;
	}

	private void updateData() {
		Supplier sp = new Supplier();
		sp.setSupplierName(txtName.getText());
		sp.setCity(txtCity.getText());
		sp.setSupplierAddress(txtAddress.getText());
		sp.setEmailId(txtEmail.getText());
		sp.setComments(txtComments.getText());
		sp.setSupplierMobile(Long.parseLong(txtMobileNo.getText()));
		sp.setPanNo(txtPAN.getText());
		sp.setGstNo(txtGSTNo.getText());
		sp.setSupplierID(supplierId);

		StatusDTO status = supplierService.update(sp);
		if (status.getStatusCode() == 0) {
			resetFields();
			loadData();
			alertHelper.showSuccessNotification("Supplier updated successfully");
		} else {
			alertHelper.showDataSaveErrAlert(currentStage);
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
		int mUnit = txtName.getText().trim().length();
		if (mUnit == 0) {
			alertHelper.beep();
			lblNameErrMsg.setText("Please enter name");
			txtName.requestFocus();
			valid = false;
		} else {
			lblNameErrMsg.setText("");
		}

		return valid;
	}

	private void resetFields() {
		txtMobileNo.setText("");
		txtName.setText("");
		txtCity.setText("");
		txtEmail.setText("");
		txtGSTNo.setText("");
		txtPAN.setText("");
		txtAddress.setText("");
		txtComments.setText("");
		supplierId = 0;
		txtName.requestFocus();
	}

	@FXML
	void onASettleUpCommand(ActionEvent event) {
		Supplier supplier = tableView.getSelectionModel().getSelectedItem();
		if (supplier == null) {
			alertHelper.showErrorNotification("Please select supplier");
		} else {
			getPopup(supplier, SETTLEUP);
		}
	}

	@FXML
	void onAddAmountCommand(ActionEvent event) {
		Supplier supplier = tableView.getSelectionModel().getSelectedItem();
		if (supplier == null) {
			alertHelper.showErrorNotification("Please select supplier");
		} else {
			getPopup(supplier, ADD);
		}
	}

	private void getPopup(Supplier supplier, String type) {

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

		TextField txtNarration = new TextField();
		txtNarration.setPrefColumnCount(10);

		Label lblNarration = new Label("Narration :");
		lblNarration.getStyleClass().add("nodeLabel");

		grid.add(lblNarration, 0, 1);
		grid.add(txtNarration, 1, 1);

		Node validateButton = dialog.getDialogPane().lookupButton(updateButtonType);
		validateButton.setDisable(true);

		txtAmount.textProperty().addListener((observable, oldValue, newValue) -> {
			validateButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> txtAmount.requestFocus());

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == updateButtonType) {
				return txtAmount.getText() + ":@:" + txtNarration.getText();
			}
			return null;
		});

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(value -> {
			try {
				if (value != null && value != "") {
					String[] values = value.split(":@:");
					String amount = values[0];
					String narrationValue = values.length == 2 ? values[1] : null;
					if (ADD.equals(type)) {
						// Add
						if (Double.valueOf(amount) > 0) {
							Supplier sup = new Supplier();
							sup.setSupplierID(supplierId);
							sup.setSupplierName(txtName.getText());
							sup.setBalanceAmount(Double.valueOf(amount));

							String narration = "Added by : " + userDetails.getFirstName() + " "
									+ userDetails.getLastName();

							if (narrationValue != null && narrationValue != "") {
								narration = narrationValue;
							}
							StatusDTO statusAddAmt = supplierService.addSupplierPaymentHistory(supplierId,
									Double.valueOf(txtAmount.getText()), 0, AppConstants.CREDIT, narration);

							if (statusAddAmt.getStatusCode() == 0) {
								alertHelper.showSuccessNotification("Balance amount updated successfully");
								Supplier sup1 = tableView.getSelectionModel().getSelectedItem();
								loadData();
								// customer is not selected in table
								if (sup1 == null) {
									sup1 = sup;
								}
								setUpdatedSupplierBalance(sup1.getSupplierID());
							} else {
								alertHelper.showErrorNotification("Error occured while adding amount");
							}
						} else {
							alertHelper.showErrorNotification("Amount should be greater than Zero (0)");
						}

					} else {
						// Settle up
						if (Double.valueOf(amount) > 0 && Double.valueOf(amount) <= supplier.getBalanceAmount()) {
							Supplier sup = new Supplier();
							sup.setSupplierID(supplierId);
							sup.setSupplierName(txtName.getText());
							sup.setBalanceAmount(Double.valueOf(amount));
							String narration = "Settled up by : " + userDetails.getFirstName() + " "
									+ userDetails.getLastName();
							if (narrationValue != null && narrationValue != "") {
								narration = narrationValue;
							}
							StatusDTO statusAddAmt = supplierService.addSupplierPaymentHistory(supplierId, 0,
									Double.valueOf(txtAmount.getText()), AppConstants.DEBIT, narration);

							if (statusAddAmt.getStatusCode() == 0) {
								alertHelper.showSuccessNotification("Balance amount updated succesfully");
								Supplier sup1 = tableView.getSelectionModel().getSelectedItem();
								loadData();
								// customer is not selected in table
								if (sup1 == null) {
									sup1 = sup;
								}
								setUpdatedSupplierBalance(sup1.getSupplierID());
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

	protected void setUpdatedSupplierBalance(int suppplierId) {
		Supplier supplier = supplierService.getSupplier(suppplierId);
		setSupplierDetails(supplier);
	}
}
