package pkg;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class MainController implements Initializable, OpenableWindow {
    @FXML
    private final Map<String, OpenableWindow> controllerMap = new HashMap<>();
    @FXML
    ComboBox<String> kernelComboBox;
    @FXML
    TextField tf11, tf12, tf13, tf14, tf15, tf21, tf22, tf23, tf24, tf25, tf31, tf32, tf33, tf34, tf35, tf41, tf42, tf43,
            tf44, tf51, tf52, tf53, tf54, tf55;
    List<TextField> textFieldList, reducedTextFieldList, extraTextFieldList;
    private Stage stage;
    private BufferedImage convolvedImage;
    private Kernel kernel;
    private Map<String, Field> kernelMap;
    private Map<Field, Class<?>> classes;
    private Optional<File> inputImageFile = Optional.empty();

    private static Optional<File> getLoadingDirectory(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load an image");
        fileChooser.setInitialDirectory(new File(System.getenv("APPDATA")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG files", "*.jpg"));
        return Optional.ofNullable(fileChooser.showOpenDialog(stage));
    }

    private static Optional<File> getSavingDirectory(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save convolved image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files", "*.png"));
        return Optional.ofNullable(fileChooser.showSaveDialog(stage));
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void setStage(Stage stage) {
        stage.setResizable(false);
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new Initializer(stage, this).initializeWindowControllers(controllerMap);
        textFieldList = List.of(tf11, tf12, tf13, tf14, tf15, tf21, tf22, tf23, tf24, tf25, tf31, tf32, tf33, tf34, tf35,
                tf41, tf42, tf43, tf44, tf51, tf52, tf53, tf54, tf55);
        reducedTextFieldList = List.of(tf22, tf23, tf24, tf32, tf33, tf34, tf42, tf43, tf44);
        extraTextFieldList = new ArrayList<>(textFieldList);
        for (TextField textField : reducedTextFieldList) {
            extraTextFieldList.remove(textField);
        }
        textFieldList.forEach(this::setValidator);
        reducedTextFieldList.forEach(textField -> textField.setText("0"));
        extraTextFieldList.forEach(textField -> textField.setEditable(false));
        kernelMap = new LinkedHashMap<>();
        classes = new LinkedHashMap<>();
        Map[] maps = Initializer.initializeMap(classes, kernelMap, Item.KERNEL);
        classes = (Map<Field, Class<?>>) maps[0];
        kernelMap = (Map<String, Field>) maps[1];
        kernelComboBox.getItems().addAll(kernelMap.keySet());
        kernelComboBox.setValue(kernelComboBox.getItems().get(0));
        try {
            kernel = new Kernel((double[][]) kernelMap.get(kernelComboBox.getItems().get(0)).get(new KernelHolder()));
        } catch (IllegalAccessException e) {
            AlertWindows.showError(e);
        }
        setTextFieldsFromReducedKernel(kernel.getKernelData());
        System.setProperty("java.security.policy", "client.policy");
        System.setProperty("java.rmi.server.codebase", "file:C:\\Users\\Laufinsconsca\\IdeaProjects\\secondLab\\server.jar");
        System.setSecurityManager(new SecurityManager());
    }

    private void show(boolean isResizable, StackTraceElement stackTraceElement) {
        Stage stage = lookupController(stackTraceElement.getMethodName()).getStage();
        stage.setResizable(isResizable);
        stage.show();
    }

    private void show(boolean isResizable) {
        show(isResizable, Thread.currentThread().getStackTrace()[2]);
    }

    private OpenableWindow lookupController() {
        return lookupController(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    private OpenableWindow lookupController(String path) {
        //client.OpenableWindow controller = controllerMap.get(path + ".client.fxml");
        return controllerMap.get(path + ".fxml");
    }

    @FXML
    private void preview() {
        //kernel = getKernelFromTextFields();
        kernel = getReducedKernelFromTextFields();
        inputImageFile.ifPresentOrElse(file -> {
            try {
                show(true, Thread.currentThread().getStackTrace()[3]);
            } catch (ServerNotRunningException e) {
                AlertWindows.showWarning("Server not running exception");
            }
        }, () -> AlertWindows.showWarning("An image isn't selected"));
    }

    @FXML
    private void doOnClickOnTheComboBox() {
        ConnectableItem item = kernelMap.get(kernelComboBox.getSelectionModel().getSelectedItem())
                .getDeclaredAnnotation(ConnectableItem.class);
        try {
            kernel = new Kernel((double[][]) kernelMap.get(item.name()).get(new KernelHolder()));
            setTextFieldsFromReducedKernel(kernel.getKernelData());
        } catch (IllegalAccessException e) {
            AlertWindows.showError(e);
        }
    }

    @FXML
    private void load() {
        inputImageFile = getLoadingDirectory(stage);
    }

    @FXML
    private void save() {
        inputImageFile.ifPresentOrElse(file -> {
            Optional<File> fileOptional = getSavingDirectory(stage);
            fileOptional.ifPresent(file1 -> {
                try {
                    ImageIO.write(convolvedImage, "png", file1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }, () -> AlertWindows.showWarning("An image isn't selected"));
    }

    public File getInputImageFile() {
        AtomicReference<File> file = new AtomicReference<>();
        inputImageFile.ifPresent(file::set);
        return file.get();
    }

    public Kernel getKernel() {
        return kernel;
    }

    public void setConvolvedImage(BufferedImage convolvedImage) {
        this.convolvedImage = convolvedImage;
    }

    TextField setValidator(TextField textField) {
        textField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) {
                if (!textField.getText().matches("^[-+]?[0-9]*[.,]?[0-9]+(?:[eE][-+]?[0-9]+)?$")) {
                    textField.setText("");
                }
            }

        });
        return textField;
    }

    private Kernel getKernelFromTextFields() {
        double[][] kernelData = new double[5][5];
        int i = 0;
        int j = 0;
        for (TextField textField : textFieldList) {
            kernelData[i][j++] = Double.parseDouble(textField.getText());
            if (j == 5) {
                i++;
                j = 0;
            }
        }
        return new Kernel(kernelData);
    }

    private Kernel getReducedKernelFromTextFields() {
        double[][] kernelData = new double[3][3];
        int i = 0;
        int j = 0;
        for (TextField textField : reducedTextFieldList) {
            kernelData[i][j++] = Double.parseDouble(textField.getText());
            if (j == 3) {
                i++;
                j = 0;
            }
        }
        return new Kernel(kernelData);
    }

    private void setTextFieldsFromReducedKernel(double[][] kernelData) {
        int i = 0;
        int j = 0;
        for (TextField textField : reducedTextFieldList) {
            textField.setText(kernelData[i][j++] + "");
            if (j == 3) {
                i++;
                j = 0;
            }
        }
    }

    @Override
    public void setParentController(OpenableWindow controller) {
    }
}

