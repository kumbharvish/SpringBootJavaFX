package com.billing.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.billing.constants.AppConstants;
import com.billing.controller.LoginController;
import com.billing.properties.AppProperties;
import com.billing.service.AppLicenseService;
import com.billing.service.DBBackupService;
import com.billing.utils.AlertHelper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@ComponentScan("com.billing")
@SpringBootApplication
public class MyStoreApplication extends Application {

	private ConfigurableApplicationContext springContext;
	
	private static final Logger logger = LoggerFactory.getLogger(MyStoreApplication.class);

	private Parent parent;
	
	private FXMLLoader fxmlLoader;
	
	
	public static void main(String[] args) {
		launch();
	}

	@Override
	public void init() {
		springContext = SpringApplication.run(MyStoreApplication.class);
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(springContext::getBean);
        System.setProperty("java.awt.headless", "false");
	}

	@Override
	public void start(final Stage initStage) throws Exception {
		showLoginStage(initStage);
	}

	private void showLoginStage(Stage initStage) {
		AppProperties appProperties = (AppProperties)springContext.getBean(AppProperties.class);
		AppLicenseService appLicenseService = (AppLicenseService)springContext.getBean(AppLicenseService.class);
		AlertHelper alertHelper = (AlertHelper)springContext.getBean(AlertHelper.class);
		try {
			if (!appProperties.check()) {
				alertHelper.showErrorAlert(null,AppConstants.LICENSE_ERROR,null,AppConstants.LICENSE_ERROR_1);
				alertHelper.showProductKeyDialog();
				System.exit(0);
			} else {
				if (appLicenseService.change()) {
					alertHelper.showErrorAlert(null,AppConstants.COMP_DATE,null,AppConstants.COMP_DATE_ERROR);
					System.exit(0);
				} else {
					if (!appProperties.doCheck()) {
						alertHelper.showErrorAlert(null,AppConstants.LICENSE_EXPIRED,null,AppConstants.LICENSE_ERROR_2);
						alertHelper.showLicenseKeyDialog();
						System.exit(0);
					} else {
						logger.error(" --- Application Check Complete and Started --- ");
						fxmlLoader.setLocation(getClass().getResource("/com/billing/gui/LoginScreen.fxml"));
						parent = fxmlLoader.load();
						LoginController loginController = fxmlLoader.getController();
						loginController.show(parent);
					}
				}
			}

		} catch (Exception e) {
			logger.error("Application Startup Exception --> :", e);
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws Exception {
		System.out.println("---- DATA BACKUP From STOP Method ---");
		DBBackupService dbBackupService = (DBBackupService)springContext.getBean(DBBackupService.class);
		dbBackupService.createDBDump(null);
		System.out.println("---- Terminate Application ---");
		System.exit(0);
	}

}