package com.billing.main;

import javafx.animation.FadeTransition;
import javafx.application.*;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

@SuppressWarnings("restriction")
public class FxPreloader extends Preloader{

	private Stage initStage;
	private Pane splashLayout;
	private ProgressBar loadProgress;
	private Label progressText;
	private static final int SPLASH_WIDTH = 840;
	private static final int SPLASH_HEIGHT = 106;
	
	@Override
	public void start(Stage stage) throws Exception {
		this.initStage = stage;
		ImageView splash = new ImageView(
				new Image(MyStoreApplication.class.getResourceAsStream("/images/MyStoreSplash.png")));
		loadProgress = new ProgressBar();
		loadProgress.setPrefWidth(SPLASH_WIDTH);
		progressText = new Label("");
		splashLayout = new VBox();
		splashLayout.getChildren().addAll(splash, loadProgress, progressText);
		progressText.setAlignment(Pos.CENTER);
		splashLayout.setStyle(
				"-fx-padding: 5; " + "-fx-background-color: cornsilk; " + "-fx-border-width:5; " + "-fx-border-color: "
						+ "linear-gradient(" + "to bottom, " + "chocolate, " + "derive(chocolate, 50%)" + ");");
		splashLayout.setEffect(new DropShadow());
		
		Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
		final Rectangle2D bounds = Screen.getPrimary().getBounds();
		initStage.setScene(splashScene);
		initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
		initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
		initStage.initStyle(StageStyle.TRANSPARENT);
		initStage.setAlwaysOnTop(true);
		initStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shop32X32.png")));
		initStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shop48X48.png")));
		initStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shop64X64.png")));
		initStage.show();
		
	}
	
	 @Override
	    public void handleProgressNotification(ProgressNotification pn) {
		 		progressText.setText("Starting Application...");
	    }
	 
	    @Override
	    public void handleStateChangeNotification(StateChangeNotification evt) {
	    	if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
	    		FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
				fadeSplash.setFromValue(1.0);
				fadeSplash.setToValue(0.0);
				fadeSplash.setOnFinished(actionEvent -> initStage.hide());
				fadeSplash.play();
	    	}
	    }

}
