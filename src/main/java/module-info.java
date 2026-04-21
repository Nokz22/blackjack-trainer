module com.blackjacktrainer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens com.blackjacktrainer.app to javafx.graphics;
    opens com.blackjacktrainer.ui  to javafx.fxml;

    exports com.blackjacktrainer.app;
    exports com.blackjacktrainer.core;
    exports com.blackjacktrainer.game;
    exports com.blackjacktrainer.ui;
}
