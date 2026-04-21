package com.blackjacktrainer.app;

import com.blackjacktrainer.ui.GameView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * JavaFX application entry point.
 *
 * <p>Bootstraps the primary stage and delegates all UI to {@link GameView}.
 */
public class Main extends Application {

    public static final String APP_TITLE   = "Blackjack Card Counting Trainer";
    public static final double MIN_WIDTH   = 900;
    public static final double MIN_HEIGHT  = 640;

    @Override
    public void start(Stage primaryStage) {
        GameView gameView = new GameView(primaryStage);
        Scene scene = new Scene(gameView.getRootPane(), MIN_WIDTH, MIN_HEIGHT);
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
