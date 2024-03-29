package com.billing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.UserDetails;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class AboutUsController implements TabContent {
	
	@Autowired
	AppUtils appUtils;

    private TabPane tabPane;
    
    private String version = "(1.0.0)";
    
    private static final String DEVELOPED_BY = "My Store Team";
    
    private static final String MY_STORE ="My Store";
    
    private static final String COPYRIGHT ="Copyright "+"\u00a9"+" 2017";
    
    @FXML
    private Text txtTitle;

    @FXML
    private Text txtVersion;

    @FXML
    private Label lblDevelopedBy;
    
    @FXML
    private Text txtCopyRight;

    @FXML
	private Label lblSupportEmail;
	
	@FXML
	private Label lblSupportMobile;

    @FXML
    private Button btnClose;

    @Override
    public boolean shouldClose() {
        return true;
    }

    @Override
    public void putFocusOnNode() {
        btnClose.requestFocus();
    }

    @Override
    public boolean loadData() {
    	populateControls();
        return true;
    }

    @Override
    public void setMainWindow(Stage stage) {
    }

    @Override
    public void setTabPane(TabPane pane) {
        tabPane = pane;
    }

    private void populateControls() {
         txtTitle.setText(MY_STORE);
         txtVersion.setText("Version " + version);
         txtCopyRight.setText(COPYRIGHT);
         lblDevelopedBy.setText(DEVELOPED_BY);
         lblSupportEmail.setText(appUtils.getAppDataValues("CUSTOMER_SUPPORT_EMAIL"));
         lblSupportMobile.setText(appUtils.getAppDataValues("CUSTOMER_SUPPORT_MOBILE"));
    }

    @FXML
    private void onCloseAction(ActionEvent event) {
        final Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        tabPane.getTabs().remove(currentTab);
    }

	@Override
	public boolean saveData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void invalidated(Observable observable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeTab() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean validateInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUserDetails(UserDetails user) {
		// TODO Auto-generated method stub
		
	}
}
