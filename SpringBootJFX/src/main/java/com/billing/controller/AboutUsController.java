package com.billing.controller;

import org.springframework.stereotype.Controller;

import com.billing.dto.UserDetails;
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

    private TabPane tabPane;
    
    private String version = "(1.0.0)";
    
    private static final String DEVELOPED_BY = "Vishal Kumbhar / Adhir Shishupal ";
    
    private static final String MOBILE = "+91 8149880299 / +91 9579616107";
    
    private static final String EMAIL_ID = "Kumbharvish@gmail.com";
    
    private static final String MY_STORE ="Tele-Distributor";
    
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
    private Label lblEmailId;

    @FXML
    private Label lblMobileNo;

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
         lblEmailId.setText(EMAIL_ID);
         lblMobileNo.setText(MOBILE);
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
