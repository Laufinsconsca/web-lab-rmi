package pkg;

import javafx.stage.Stage;

interface OpenableWindow {
    Stage getStage();

    void setStage(Stage stage);

    void setParentController(OpenableWindow controller);
}
