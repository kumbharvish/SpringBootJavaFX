package com.billing.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.MyStoreDetails;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.main.Global;
import com.billing.service.DBBackupService;
import com.billing.service.DBScheduledDumpTask;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@SuppressWarnings("restriction")
@Controller
public class HomeController extends AppContext {

	@Autowired
	AppUtils appUtils;

	@Autowired
	AlertHelper alertHelper;

	MyStoreDetails storeDetails;

	@Autowired
	DBBackupService dbBackupService;

	@Autowired
	DBScheduledDumpTask dbScheduledDumpTask;

	public Stage currentStage = null;

	public UserDetails userDetails = null;

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@FXML
	private Label lblSupportEmail;

	@FXML
	private Label lblSupportMobile;

	@FXML
	private MenuBar menuBar;

	@FXML
	private MenuItem customersMenuItem;

	@FXML
	private MenuItem customersReportMenuItem;

	@FXML
	private MenuItem stockSummaryReportMenuItem;

	@FXML
	private MenuItem suppliersReportMenuItem;

	@FXML
	private MenuItem lowStockSummaryReportMenuItem;

	@FXML
	private MenuItem productsMenuItem;

	@FXML
	private MenuItem quickStockCorrectionMenuItem;

	@FXML
	private MenuItem createInvoiceMenuItem;

	@FXML
	private MenuItem searchInvoiceMenuItem;

	@FXML
	private MenuItem salesReportMenuItem;

	@FXML
	private MenuItem cashReportMenuItem;

	@FXML
	private MenuItem userPreferencesMenuItem;

	@FXML
	private MenuItem dataBackupMenuItem;

	// Help Menu
	@FXML
	private ToolBar toolBar;

	@FXML
	private TabPane tabPane;

	@FXML
	private BorderPane rootPane;

	@FXML
	private Label lblLicenseValidUpto;

	public void initialize() {
		tabPane.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) -> {
					if (newValue != null) {
						Platform.runLater(() -> {
							Object object = newValue.getProperties().get("controller");
							if (object != null) {
								((TabContent) object).putFocusOnNode();
							}
						});
					}
				});

		toolBar.managedProperty().bind(toolBar.visibleProperty());
		// Listner to load dashboard if no tab in tabpane

		tabPane.getTabs().addListener(new ListChangeListener<Tab>() {

			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Tab> c) {
				if (tabPane.getTabs().size() == 0 && userDetails.getUserType().equals("INTERNAL")) {
					loadDashboard();
				}
			}

		});

		try {
			lblLicenseValidUpto
					.setText("License Valid Upto : " + appUtils.dec(appUtils.getAppDataValues("APP_SECURE_KEY")));
			lblSupportEmail.setText("Copyright © " + appUtils.getAppDataValues("CUSTOMER_SUPPORT_EMAIL"));
			lblSupportMobile.setText("Contact Us : " + appUtils.getAppDataValues("CUSTOMER_SUPPORT_MOBILE"));
		} catch (Exception e) {
			logger.error("lblLicenseValidUpto -->" + e);
		}

		// Start Scheduled DB Dump task
		startScheduledDBDumpTask();
	}

	public void loadData() {
		if (userDetails.getUserType().equals("EXTERNAL")) {
			String allowedMenus = appUtils.getAppDataValues("EXTERNAL_USER_MENUS");
			logger.info("--- Allowed Menus for External User ---> " + allowedMenus);
			toolBar.getItems().removeIf(item -> (item.getId() != null && !allowedMenus.contains(item.getId())));
			menuBar.getMenus().stream().forEach(menu -> {
				updateMenuForExternalUser(menu, allowedMenus);
			});
			// Default tab create invoice
			addTab("CreateInvoice", "Invoice");
		}
		appUtils.licenseExpiryAlert();
		if (null == storeDetails) {
			alertHelper.showInstructionsAlert(currentStage, "MyStore Setup", "Welcome to MyStore,",
					AppConstants.INSTR_MYSTORE_SETUP, 600, 140);
			// Show Store Details tab
			addTab("StoreDetails", "Store Details");
		} else if (userDetails.getUserType().equals("INTERNAL")) {
			// Load Dashboard
			loadDashboard();
		}
	}

	private void loadDashboard() {
		URL resource = this.getClass().getResource("/com/billing/gui/Dashboard.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(resource);
		Parent pane = null;
		try {
			pane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			alertHelper.beep();
			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final DashboardController controller = (DashboardController) fxmlLoader.getController();
		controller.currentStage = currentStage;
		controller.userDetails = userDetails;
		controller.cashReportMenuItem = cashReportMenuItem;
		controller.customersReportMenuItem = customersReportMenuItem;
		controller.lowStockSummaryReportMenuItem = lowStockSummaryReportMenuItem;
		controller.stockSummaryReportMenuItem = stockSummaryReportMenuItem;
		controller.suppliersReportMenuItem = suppliersReportMenuItem;
		controller.storeDetails = storeDetails;
		controller.loadData();
		rootPane.setCenter(pane);
	}

	private void updateMenuForExternalUser(Menu menu, String allowedMenus) {
		menu.getItems().removeIf(item -> (item.getId() != null && !allowedMenus.contains(item.getId())));
		if (menu.getItems().size() == 0) {
			menu.setVisible(false);
		}
	}

	private void startScheduledDBDumpTask() {
		Timer time = new Timer();
		Integer dbDumpInterval = Integer.parseInt(appUtils.getAppDataValues("DB_DUMP_INTERVAL").split("")[0]);
		logger.info("---- DB Dump Scheduled with Interval of :: " + dbDumpInterval + " Hour ---");
		dbDumpInterval = dbDumpInterval * 60 * 60 * 1000; // Convert Hours to Milliseconds
		time.schedule(dbScheduledDumpTask, 0, dbDumpInterval);
	}

	@FXML
	void onAboutUsCommand(ActionEvent event) {
		addTab("AboutUs", "About Us");
	}

	@FXML
	void onBillWiseProfitCommand(ActionEvent event) {
		addTab("InvoiceWiseProfitReport", "Invoice Wise Profit Report");
	}

	@FXML
	void onCashReportClick(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			cashReportMenuItem.fire();
		}
	}

	@FXML
	void onCashReportCommand(ActionEvent event) {
		addTab("CashReport", "Cash Report");
	}

	@FXML
	void onExpenseReportCommand(ActionEvent event) {
		addTab("ExpensesReport", "Expense Report");
	}

	@FXML
	void onCreateInvoiceClick(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			createInvoiceMenuItem.fire();
		}
	}

	@FXML
	void onCreateInvoiceCommand(ActionEvent event) {
		addTab("CreateInvoice", "Invoice");
	}

	@FXML
	void onCustomerCommand(ActionEvent event) {
		addTab("Customers", "Customers");

	}

	@FXML
	void onCustomerPaymentHistoryCommand(ActionEvent event) {
		addTab("CustomerPaymentHistory", "Customer Payment History");
	}

	@FXML
	void onCustomerPurchaseHistoryCommand(ActionEvent event) {
		addTab("CustomerPurchaseHistory", "Customer Purchase History");
	}

	@FXML
	void onCustomerReportCommand(ActionEvent event) {
		addTab("CustomersReport", "Customers Report");
	}

	@FXML
	void onSuppliersReportCommand(ActionEvent event) {
		addTab("SuppliersReport", "Suppliers Report");
	}

	@FXML
	void onCustomerWiseProfitCommand(ActionEvent event) {
		addTab("CustomerWiseProfitReport", "Customer Wise Profit Report");
	}

	@FXML
	void onDailySalesReportCommand(ActionEvent event) {
		addTab("GraphicalDailySalesReport", "Daily Sales Report");
	}

	/*
	 * @FXML void onDataBackupClick(MouseEvent event) { if (event.getButton() ==
	 * MouseButton.PRIMARY && event.getClickCount() == 1) {
	 * dataBackupMenuItem.fire(); } }
	 */
	@FXML
	void onDataBackupCommand(ActionEvent event) {
		dbBackupService.saveDBDumpToChoosenLocation(currentStage);
	}

	@FXML
	void onRestoreDatabaseCommand(ActionEvent event) {
		dbBackupService.restoreDatabase(currentStage);
	}

	@FXML
	private void onExitCommand(ActionEvent event) {
		currentStage.fireEvent(new WindowEvent(currentStage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	@FXML
	void onCreateExpenseCommand(ActionEvent event) {
		addTab("Expense", "Expense");
	}

	@FXML
	void onDeleteDataCommand(ActionEvent event) {
		addTab("DeleteData", "Delete Data");
	}

	@FXML
	void onSearchExpenseCommand(ActionEvent event) {
		addTab("SearchExpense", "Search Expense");
	}

	@FXML
	void onGenerateBarcodeCommand(ActionEvent event) {
		addTab("GenerateBarcode", "Generate Barcode");
	}

	@FXML
	void onHideToolbarCommand(ActionEvent event) {
		CheckMenuItem menuItem = (CheckMenuItem) event.getSource();
		toolBar.setVisible(!menuItem.isSelected());
	}

	@FXML
	void onManageAccountCommand(ActionEvent event) {
		addTab("ManageAccount", "Manage Account");
	}

	@FXML
	void onCustomersClick(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			customersMenuItem.fire();
		}
	}

	@FXML
	void onMeasurementUnitsCommand(ActionEvent event) {
		addTab("UOM", "Measurement Units");
	}

	@FXML
	void onConsolidatedReportCommand(ActionEvent event) {
		addTab("ConsolidatedReport", "Consolidated Report");
	}

	@FXML
	void onMonthlySalesReportCommand(ActionEvent event) {
		addTab("GraphicalMonthlySalesReport", "Monthly Sales Report");
	}

	@FXML
	void onPaymentModeWiseSalesCommand(ActionEvent event) {
		addTab("GraphicalPaymentModeWiseReport", "Payment Mode Wise Sales Report");
	}

	@FXML
	void onGSTR1ReportCommand(ActionEvent event) {
		addTab("GSTR1Report", "GSTR1 Report");
	}

	@FXML
	void onPrintBarcodeCommand(ActionEvent event) {
		addTab("PrintBarcode", "Print Barcode");
	}

	@FXML
	void onProductCategoriesCommand(ActionEvent event) {
		addTab("ProductCategory", "Product Categories");
	}

	/*
	 * @FXML void onQuickStockCorrectionClick(MouseEvent event) { if
	 * (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
	 * quickStockCorrectionMenuItem.fire(); } }
	 */

	@FXML
	void onProductCategoryWiseStockCommand(ActionEvent event) {
		addTab("ProductCategoryWiseStockReport", "Product Cateogry Wise Stock Report");
	}

	@FXML
	void onProductProfitReportCommand(ActionEvent event) {
		addTab("ProductProfitReport", "Product Profit Report");
	}

	@FXML
	void onProductWiseProfitCommand(ActionEvent event) {
		addTab("ProductWiseProfitReport", "Product Wise Profit Report");
	}

	@FXML
	void onProductWiseSalesCommand(ActionEvent event) {
		addTab("ProductWiseSalesReport", "Product Wise Sales Report");
	}

	@FXML
	void onProductsClick(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			productsMenuItem.fire();
		}
	}

	@FXML
	void onProductsCommand(ActionEvent event) {
		addTab("Products", "Products");
	}

	@FXML
	void onProfitLossReportCommand(ActionEvent event) {
		addTab("ProfitLossReport", "Profit Loss Report");
	}

	@FXML
	void onSaleReportClick(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			salesReportMenuItem.fire();
		}
	}

	@FXML
	void onSalesReportCommand(ActionEvent event) {
		addTab("SalesReport", "Sales Report");
	}

	@FXML
	void onSalesReturnReportCommand(ActionEvent event) {
		addTab("SalesReturnReport", "Sales Return Report");
	}

	@FXML
	void onStockSummaryReportCommand(ActionEvent event) {
		addTab("StockSummaryReport", "Stock Summary Report");
	}

	@FXML
	void onSearchInvoiceClick(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			searchInvoiceMenuItem.fire();
		}
	}

	@FXML
	void onSearchInvoiceCommand(ActionEvent event) {
		addTab("SearchInvoice", "Search Invoice");
	}

	@FXML
	void onPurchaseEntryCommand(ActionEvent event) {
		addTab("PurchaseEntry", "Purchase Entry");
	}

	@FXML
	void onPurchaseEntryWiseProfitCommand(ActionEvent event) {
		addTab("PurchaseEntryWiseProfitReport", "Purchase Entry Wise Profit Report");
	}

	@FXML
	void onSearchPurchaseEntryCommand(ActionEvent event) {
		addTab("SearchPurchaseEntry", "Search Purchase Entry");
	}

	@FXML
	void onStoreDetailsCommand(ActionEvent event) {
		addTab("StoreDetails", "Store Details");
	}

	@FXML
	void onSuppliersCommand(ActionEvent event) {
		addTab("Suppliers", "Suppliers");
	}

	@FXML
	void onSuppliersPayHistoryCommand(ActionEvent event) {
		addTab("SupplierPaymentHistory", "Supplier Payment History");
	}

	@FXML
	void onSuppliersPurchaseHistoryCommand(ActionEvent event) {
		addTab("SupplierPurchaseHistory", "Supplier Purchase History");
	}

	@FXML
	void onUserPreferencesCommand(ActionEvent event) {
		addTab("UserPreferences", "User Preferences");
	}

	/*
	 * @FXML void onUserPreferencesClick(MouseEvent event) { if (event.getButton()
	 * == MouseButton.PRIMARY && event.getClickCount() == 1) {
	 * userPreferencesMenuItem.fire(); } }
	 */

	@FXML
	void onLowStockSummaryCommand(ActionEvent event) {
		addTab("LowStockSummaryReport", "Low Stock Summary Report");
	}

	@FXML
	void onTaxesCommand(ActionEvent event) {
		addTab("Taxes", "Taxes");
	}

	@FXML
	void onInvoiceTemplatesCommand(ActionEvent event) {
		addTab("InvoiceTemplates", "Invoice Templates");
	}

	@FXML
	void onQuickStockCorrectionCommand(ActionEvent event) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/QuickStockCorrection.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("showQuickStockCorrectionPopup Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final QuickStockCorrectionController controller = (QuickStockCorrectionController) fxmlLoader.getController();
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("Stock Correction");
		controller.loadData();
		controller.setTask(null);
		stage.showAndWait();
	}

	private void addTab(final String fxmlFileName, final String title) {

		final String KEY = "fxml";

		final String viewPath = "/com/billing/gui/" + fxmlFileName + ".fxml";

		URL resource = this.getClass().getResource(viewPath);
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(resource);
		Parent pane = null;
		try {
			pane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("HomeController addTab Error in loading the view file :" + fxmlFileName, e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final TabContent controller = (TabContent) fxmlLoader.getController();
		controller.setMainWindow(currentStage);
		controller.setTabPane(tabPane);
		controller.setUserDetails(userDetails);

		if (!controller.loadData()) {
			return;
		}

		Tab tab = new Tab();
		tab.getProperties().put("controller", controller);
		tab.getProperties().put(KEY, fxmlFileName);
		tab.setContent(pane);
		tab.setText(title);
		setContextMenu(tab);

		tab.setOnCloseRequest((Event event1) -> {
			if (!controller.shouldClose()) {
				event1.consume();
			}
		});
		if (tabPane.getTabs().size() != 0) {
			TabContent controllerPrev = (TabContent) tabPane.getTabs().get(0).getProperties().get("controller");
			if (controllerPrev.shouldClose()) {
				tabPane.getTabs().clear();
			} else {
				return;
			}
		}

		if (tabPane.getTabs().size() == 0) {
			rootPane.setCenter(tabPane);
		}
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		controller.putFocusOnNode();
	}

	private void setContextMenu(final Tab tab) {

		final MenuItem closeTabItem = new MenuItem("Close Tab");
		final MenuItem closeOtherTabsItem = new MenuItem("Close Other Tabs");
		final MenuItem closeAllTabsItem = new MenuItem("Close All Tabs");

		final ContextMenu contextMenu = new ContextMenu(closeTabItem, closeOtherTabsItem, closeAllTabsItem);

		setCloseTabAction(tab, closeTabItem);
		setCloseOtherTabsAction(tab, closeOtherTabsItem);
		setCloseAllTabsAction(tab, closeAllTabsItem);

		tab.setContextMenu(contextMenu);
	}

	private void setCloseAllTabsAction(final Tab tab, final MenuItem menuItem) {
		final EventHandler<ActionEvent> eventHandler = (ActionEvent event) -> {
			closeAllTabs();
		};

		menuItem.setOnAction(eventHandler);
	}

	private void setCloseOtherTabsAction(final Tab tab, final MenuItem menuItem) {
		final EventHandler<ActionEvent> eventHandler = (ActionEvent event) -> {
			final TabPane tabPane = tab.getTabPane();
			Global.closeTabs(tabPane, tab);
		};

		menuItem.setOnAction(eventHandler);
	}

	private void setCloseTabAction(final Tab tab, final MenuItem menuItem) {

		final EventHandler<ActionEvent> eventHandler = (ActionEvent event) -> {
			final TabPane tabPane = tab.getTabPane();
			tabPane.getSelectionModel().select(tab);
			TabContent controller = (TabContent) tab.getProperties().get("controller");
			if (controller.shouldClose()) {
				tabPane.getTabs().remove(tab);
			}
		};

		menuItem.setOnAction(eventHandler);
	}

	public boolean closeAllTabs() {
		final ObservableList<Tab> tabs = tabPane.getTabs();
		final List<Tab> tabsToRemove = new ArrayList<>(tabs.size());

		for (Tab tabControl : tabs) {
			tabPane.getSelectionModel().select(tabControl);
			TabContent controller = (TabContent) tabControl.getProperties().get("controller");
			if (!controller.shouldClose()) {
				return false;
			} else {
				tabsToRemove.add(tabControl); // mark this tab to be removed
			}
		}

		tabs.removeAll(tabsToRemove); // actually remove the tabs here
		return true;
	}

}
