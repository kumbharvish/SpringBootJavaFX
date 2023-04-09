package com.billing.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.MyStoreDetails;
import com.billing.dto.UserDetails;
import com.billing.dto.WindowState;
import com.billing.main.AppContext;
import com.billing.main.Global;
import com.billing.service.AppLicenseService;
import com.billing.service.StoreDetailsService;
import com.billing.service.UserService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@SuppressWarnings("restriction")
@Controller
public class LoginController extends AppContext {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	AppUtils appUtils;

	@Autowired
	UserService userService;

	@Autowired
	AppLicenseService appLicenseService;

	@Autowired
	StoreDetailsService myStoreService;

	@Autowired
	AlertHelper alertHelper;

	MyStoreDetails storeDetails;

	public Stage currentStage = null;

	private final static String APPLICATION_LOGIN_TITTLE = "Login";

	private FXMLLoader fxmlLoader;

	@FXML
	private Button btnLogin;

	@FXML
	private JFXTextField txtUserName;

	@FXML
	private JFXPasswordField txtPassword;

	@FXML
	private Label errorMessage;

	@FXML
	private Label lblShopName;

	@FXML
	private Label lblLicenseValideUpto;

	@FXML
	private Label lblSupportEmail;

	@FXML
	private Label lblSupportMobile;

	@FXML
	public void initialize() {
		btnLogin.setDefaultButton(true);
		fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
	}

	@FXML
	void doLogin(ActionEvent event) {
		String user = txtUserName.getText();
		String pwd = txtPassword.getText();
		if ("".equals(user) || "".equals(pwd)) {
			errorMessage.setText("Please enter Username / Password !");
		} else {
			UserDetails userDetails = userService.validateUser(user, pwd);
			if (userDetails != null) {
				currentStage.close();
				// Open Home Window
				fxmlLoader.setLocation(getClass().getResource("/com/billing/gui/Home.fxml"));
				Parent root = null;
				try {
					root = fxmlLoader.<BorderPane>load();
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("Error in loading the Home page view file : ", e);
				}
				final Scene scene = new Scene(root, 850, 700);
				addKeyFilter(scene);
				Stage stage = new Stage();
				stage.setScene(scene);
				HomeController homeController = fxmlLoader.getController();
				homeController.currentStage = stage;
				homeController.userDetails = userDetails;
				homeController.storeDetails = storeDetails;
				stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shop32X32.png")));
				stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shop48X48.png")));
				stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shop64X64.png")));

				final WindowState s = Global.getDefaultWindowState();
				stage.setX(s.getXPos());
				stage.setY(s.getYPos());
				stage.setWidth(s.getWidth());
				stage.setHeight(s.getHeight());

				if (storeDetails != null) {
					stage.setTitle("My Store - " + storeDetails.getStoreName());
				} else {
					stage.setTitle("My Store");
				}
				stage.setMaximized(true);
				stage.show();
				// Call Load Data for Home
				homeController.loadData();
				stage.setOnCloseRequest((WindowEvent event2) -> {
					if (!homeController.closeAllTabs()) {
						event2.consume();
						return;
					}
					Alert alert = alertHelper.showConfirmAlertWithYesNo(stage, null, "Do you want to exit?");
					if (alert.getResult() == ButtonType.NO) {
						event2.consume();
					}
				});
			} else {
				errorMessage.setText("Incorrect Username / Password");
			}
		}
	}

	@FXML
	void doClose(ActionEvent event) {
		System.exit(0);
	}

	public void show(Parent parent) {
		Scene scene = new Scene(parent);
		Stage stage = new Stage();
		final Rectangle2D bounds = Screen.getPrimary().getBounds();
		stage.setX(bounds.getMinX() + bounds.getWidth() / 2 - 300 / 2);
		stage.setY(bounds.getMinY() + bounds.getHeight() / 2 - 447 / 2);
		stage.setAlwaysOnTop(true);
		stage.setScene(scene);
		stage.setTitle(APPLICATION_LOGIN_TITTLE);
		stage.setResizable(false);
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shop32X32.png")));
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shop48X48.png")));
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shop64X64.png")));
		currentStage = stage;
		storeDetails = myStoreService.getMyStoreDetails();
		if (storeDetails != null) {
			lblShopName.setText(storeDetails.getStoreName());
		} else {
			lblShopName.setText("My Store");
		}

		try {
			lblLicenseValideUpto.setText(appUtils.dec(appUtils.getAppDataValues("APP_SECURE_KEY")));
			lblSupportEmail.setText(appUtils.getAppDataValues("CUSTOMER_SUPPORT_EMAIL"));
			lblSupportMobile.setText(appUtils.getAppDataValues("CUSTOMER_SUPPORT_MOBILE"));
		} catch (Exception e) {
			logger.error("LogginController.show-->" + e);
			e.printStackTrace();
		}
		stage.show();
		// Update Last Run
		appLicenseService.updateLastRun();
	}

	private void addKeyFilter(final Scene scene) {

		final KeyCombination f4Key = new KeyCodeCombination(KeyCode.F4, KeyCombination.SHORTCUT_DOWN);

		final KeyCombination leftKey = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHORTCUT_DOWN);
		final KeyCombination leftNumPadKey = new KeyCodeCombination(KeyCode.KP_LEFT, KeyCombination.SHORTCUT_DOWN);

		final KeyCombination rightKey = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHORTCUT_DOWN);
		final KeyCombination rightNumPadKey = new KeyCodeCombination(KeyCode.KP_RIGHT, KeyCombination.SHORTCUT_DOWN);

		scene.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {

			// if shortcut(a.k.a. ctrl) + F4 key combination was pressed
			if (f4Key.match(event)) {
				final TabPane tabPane = (TabPane) scene.lookup("#tabPane");

				if (!tabPane.getSelectionModel().isEmpty()) {
					final Tab tab = tabPane.getSelectionModel().getSelectedItem();
					TabContent controller = (TabContent) tab.getProperties().get("controller");
					if (controller.shouldClose()) {
						tabPane.getTabs().remove(tab);
					}
				}
				event.consume();
			} else if (leftKey.match(event) || leftNumPadKey.match(event)) {
				/*
				 * control+ left arrow key is pressed shift to the previous tab (The movement is
				 * circular)
				 */
				final TabPane tabPane = (TabPane) scene.lookup("#tabPane");
				if (tabPane.getTabs().size() > 1 && !tabPane.getSelectionModel().isEmpty()) {
					if (tabPane.getSelectionModel().getSelectedIndex() == 0) {
						tabPane.getSelectionModel().selectLast();
					} else {
						tabPane.getSelectionModel().selectPrevious();
					}
				}
				event.consume();
			} else if (rightKey.match(event) || rightNumPadKey.match(event)) {
				/*
				 * control+ right arrow key is pressed shift to the next tab (The movement is
				 * circular)
				 */
				final TabPane tabPane = (TabPane) scene.lookup("#tabPane");
				if (tabPane.getTabs().size() > 1 && !tabPane.getSelectionModel().isEmpty()) {
					if (tabPane.getSelectionModel().getSelectedIndex() == tabPane.getTabs().size() - 1) { // last tab
						tabPane.getSelectionModel().selectFirst();
					} else {
						tabPane.getSelectionModel().selectNext();
					}
				}
				event.consume();
			}
		});

		scene.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
			if (event.getTarget() instanceof Button && event.getCode() == KeyCode.ENTER) {
				Button button = (Button) event.getTarget();
				if (!button.isDisabled()) {
					button.fire();
				}
				event.consume();
			}
		});

		scene.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
			if (event.getTarget() instanceof CheckBox && event.getCode() == KeyCode.ENTER) {
				CheckBox checkBox = (CheckBox) event.getTarget();
				if (!checkBox.isDisabled()) {
					checkBox.setSelected(!checkBox.isSelected());
				}
				event.consume();
			}
		});
	}

}
