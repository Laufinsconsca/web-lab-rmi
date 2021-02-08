package pkg;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Objects;

@ConnectableItem(name = "Preview", type = Item.CONTROLLER, pathFXML = "preview.fxml")
public class PreviewController implements OpenableWindow {
    Stage stage;
    MainController mainController;
    @FXML
    BorderPane previewPane;
    ConvolveService service;

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void setStage(Stage stage) {
        stage.setOnShowing(event -> {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            try {
                service = (ConvolveService) Naming.lookup("rmi://localhost/Convolve");
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                if (!(e instanceof ConnectException)) {
                    AlertWindows.showError(e);
                }
            }
            if (Objects.isNull(service)) {
                throw new ServerNotRunningException();
            }
            File inputImageFile = mainController.getInputImageFile();
            BufferedImage inputAWTImage = null;
            BufferedImage outputAWTImage = null;
            try {
                inputAWTImage = ImageIO.read(inputImageFile);
            } catch (IOException e) {
                AlertWindows.showError(e);
            }
            try {
                outputAWTImage = service.convolveAnImage(new ByteArrayOfTheImage(inputAWTImage), mainController.getKernel()).getBufferedImage();
            } catch (RemoteException e) {
                AlertWindows.showError(e);
            }
            Image image = SwingFXUtils.toFXImage(outputAWTImage, null);
            previewPane.setPrefWidth(2 * image.getWidth() + 10);
            previewPane.setPrefHeight(image.getHeight());
            previewPane.setLeft(new ImageView(SwingFXUtils.toFXImage(inputAWTImage, null)));
            previewPane.setRight(new ImageView(image));
            mainController.setConvolvedImage(outputAWTImage);
        });
        this.stage = stage;
    }


    @Override
    public void setParentController(OpenableWindow controller) {
        if (controller instanceof MainController) {
            this.mainController = (MainController) controller;
        }
    }
}
