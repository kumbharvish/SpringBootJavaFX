package com.billing.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.ProductAnalysis;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.ProductHistoryService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;

@Controller
public class ProdcutWiseProfitReportController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(ProdcutWiseProfitReportController.class);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductHistoryService productHistoryService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<ProductAnalysis> productProfitList;

	@FXML
	private DatePicker dpFromDate;

	@FXML
	private DatePicker dpToDate;

	@FXML
	private TableView<ProductAnalysis> tableView;

	@FXML
	private TableColumn<ProductAnalysis, String> tcProductName;

	@FXML
	private TableColumn<ProductAnalysis, Double> tcTotalQty;

	@FXML
	private TableColumn<ProductAnalysis, Double> tcSellPrice;

	@FXML
	private TableColumn<ProductAnalysis, Double> tcTotalAmount;

	@FXML
	private TableColumn<ProductAnalysis, Double> tcProfitAmount;

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		dpFromDate.requestFocus();
	}

	@Override
	public boolean loadData() {
		productProfitList.clear();
		List<ProductAnalysis> produtList = productHistoryService.getProductWiseProfit(dpFromDate.getValue().toString(),
				dpToDate.getValue().toString());
		productProfitList.addAll(produtList);
		return true;
	}

	private void setTableCellFactories() {

		final Callback<TableColumn<ProductAnalysis, Double>, TableCell<ProductAnalysis, Double>> callback = new Callback<TableColumn<ProductAnalysis, Double>, TableCell<ProductAnalysis, Double>>() {

			@Override
			public TableCell<ProductAnalysis, Double> call(TableColumn<ProductAnalysis, Double> param) {
				TableCell<ProductAnalysis, Double> tableCell = new TableCell<ProductAnalysis, Double>() {

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

		tcProductName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));

		tcSellPrice.setCellFactory(callback);
		tcTotalQty.setCellFactory(callback);
		tcProfitAmount.setCellFactory(callback);
		tcTotalAmount.setCellFactory(callback);

		tcProductName.getStyleClass().add("character-cell");
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
	public void initialize() {
		setTableCellFactories();
		productProfitList = FXCollections.observableArrayList();
		tableView.setItems(productProfitList);
		dpFromDate.setValue(LocalDate.now());
		dpToDate.setValue(LocalDate.now());
		appUtils.setDateConvertor(dpFromDate);
		appUtils.setDateConvertor(dpToDate);
		dpFromDate.setDayCellFactory(this::getDateCell);
		dpFromDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpFromDate.setValue(today);
			}
			loadData();
		});
		dpToDate.setDayCellFactory(this::getDateCell);
		dpToDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpToDate.setValue(today);
			}
			loadData();
		});

	}

	@Override
	public boolean saveData() {
		return true;
	}

	@Override
	public void invalidated(Observable observable) {

	}

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		if (productProfitList.size() == 0) {
			alertHelper.showErrorNotification("No records to export");
			return false;
		}
		return true;
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

}
