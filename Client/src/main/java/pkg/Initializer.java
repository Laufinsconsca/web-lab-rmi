package pkg;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.atteo.classindex.ClassIndex;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Initializer {
    static final String FXML_PATH = "fxml/";
    private static final Class<ConnectableItem> annotationClass = ConnectableItem.class;
    private final Stage ownerStage;
    private final OpenableWindow parentController;
    private final Function<Class<?>, OpenableWindow> initializeWindowController = new Function<>() {
        @Override
        public OpenableWindow apply(Class<?> clazz) {
            OpenableWindow controller = null;
            try {
                controller = (OpenableWindow) clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            String path = FXML_PATH + controller.getClass().getDeclaredAnnotation(annotationClass).pathFXML();
            controller = initializeModalityWindow(path, controller);
            controller.getStage().initOwner(ownerStage);
            controller.getStage().setTitle(controller.getClass().getDeclaredAnnotation(annotationClass).name());
            return controller;
        }
    };

    public Initializer(Stage ownerStage, OpenableWindow parentController) {
        this.ownerStage = ownerStage;
        this.parentController = parentController;
    }

    public static Map[] initializeMap(Map<Field, Class<?>> classes, Map<String, Field> map, Item item) {
        StreamSupport.stream(ClassIndex.getAnnotated(annotationClass).spliterator(), false)
                .filter(f -> f.getDeclaredAnnotation(annotationClass).type() == item)
                .sorted(Comparator.comparingInt(f -> f.getDeclaredAnnotation(annotationClass).priority()))
                .forEach(clazz -> Stream.of(clazz.getDeclaredFields())
                        .filter(field -> field.isAnnotationPresent(annotationClass))
                        .filter(f -> f.getDeclaredAnnotation(annotationClass).type() == item)
                        .sorted(Comparator.comparingInt(f -> f.getDeclaredAnnotation(annotationClass).priority()))
                        .forEach(field -> {
                            map.put(field.getDeclaredAnnotation(annotationClass).name(), field);
                            classes.put(field, clazz);
                        }));
        return new Map[]{classes, map};
    }

    private <T extends OpenableWindow> T initializeModalityWindow(String pathFXML, T modalityWindow) {
        FXMLLoader loader;
        Parent createNewFunction;
        Stage createNewFunctionStage = new Stage();
        try {
            loader = new FXMLLoader(modalityWindow.getClass().getClassLoader().getResource(pathFXML));
            createNewFunction = loader.load();
            modalityWindow = loader.getController();
            createNewFunctionStage.setScene(new Scene(createNewFunction));
            createNewFunctionStage.initModality(Modality.APPLICATION_MODAL);
            modalityWindow.setStage(createNewFunctionStage);
            modalityWindow.setParentController(parentController);
        } catch (IOException e) {
            AlertWindows.showError(e);
        }
        return modalityWindow;
    }

    public void initializeWindowControllers(Map<String, OpenableWindow> controllerMap) {
        StreamSupport.stream(ClassIndex.getAnnotated(annotationClass).spliterator(), false)
                .filter(f -> f.getDeclaredAnnotation(annotationClass).type() == Item.CONTROLLER)
                .forEach(clazz -> controllerMap.put(clazz.getDeclaredAnnotation(annotationClass).pathFXML(),
                        initializeWindowController.apply(clazz)));
    }
}