package com.example.demo;

import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.function.Predicate;

public class Client extends Application {

    public final static int SERVER_PORT = 2526;
    public final static String SERVER_IP = "127.0.0.1";

    private Socket clientSocket = null;
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream inputStream = null;

    private Button connectButton;
    private Button resultButton;
    private TextField serverIp;
    private TextField serverPort;

    private ValidatingTextField firstNumInput;
    private ValidatingTextField secondNumInput;
    private TextField resultTextField;


    @Override
    public void start(Stage stage) throws IOException {

        Label ipLabel = new Label("IP address:");
        serverIp = new TextField();
        serverIp.setEditable(false);
        HBox ipPane = new HBox(5, ipLabel, serverIp);
        ipPane.setAlignment(Pos.BASELINE_CENTER);


        Label portLabel = new Label("Port:");
        serverPort = new TextField();
        serverPort.setEditable(false);
        HBox portPane = new HBox(5, portLabel, serverPort);
        portPane.setAlignment(Pos.BASELINE_CENTER);

        connectButton = new Button("connect to server");
        connectButton.setOnAction(new ConnectButtonHandler());
        connectButton.setStyle(
                "-fx-pref-height: 28px;" +
                        "-fx-pref-width: 150px;" +
                "-fx-background-color: #34c1da;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-radius: 5;" +
                        "-fx-padding: 3 6 6 6;");

        firstNumInput = new ValidatingTextField(input -> input.matches("^[1-9][0-9]*$"));
        firstNumInput.setPromptText("Please enter a number");
        secondNumInput = new ValidatingTextField(input -> input.matches("^[1-9][0-9]*$"));
        secondNumInput.setPromptText("Please enter a number");

        resultButton = new Button("get results");
        resultTextField = new TextField();
        resultTextField.setEditable(false);
        resultTextField.setPromptText("nod of entered numbers");

        resultButton.setOnAction(new ResultButtonHandler());
        resultButton.setDisable(true);
        resultButton.setStyle(
                "-fx-pref-height: 28px;" +
                        "-fx-pref-width: 150px;" +
                        "-fx-background-color: #f90989;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-radius: 5;" +
                        "-fx-padding: 3 6 6 6;");



        GridPane gridPane = new GridPane();
        gridPane.setStyle("-fx-background-color: #bc8ef1");
        gridPane.setHgap(30); //horizontal gap in pixels => that's what you are asking for
        gridPane.setVgap(10); //vertical gap in pixels
        gridPane.setPadding(new Insets(10, 10, 10, 10)); //margins around the whole grid
        //(top/right/bottom/left)


        Label appLabel = new Label("Client");
        appLabel.setStyle("-fx-font-size: 18pt;");
        gridPane.add(appLabel, 1, 1);
        gridPane.add(connectButton, 0, 3);
        gridPane.add(resultButton, 0, 6);
        gridPane.add(ipPane, 1, 3);
        gridPane.add(portPane, 2, 3);

        gridPane.add(new Label("Enter first number"), 1, 5);
        gridPane.add(new Label("Enter second number"), 2, 5);

        gridPane.add(firstNumInput, 1, 6);
        gridPane.add(secondNumInput, 2, 6);

        gridPane.add(new Label("nod of entered numbers:"), 1, 8);
        gridPane.add(resultTextField, 2, 8);

        Scene scene = new Scene(gridPane, 700, 400);
        stage.setScene(scene);
        stage.setTitle("PSP4 PART3 CLIENT");
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing");
                try {
                    outputStream.writeObject("quite");
                    if (inputStream != null) inputStream.close();
                    if (outputStream != null) outputStream.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }


    class ConnectButtonHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent actionEvent) {

            try {
                clientSocket = new Socket("127.0.0.1", 2525);

                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                inputStream = new ObjectInputStream(clientSocket.getInputStream());

                serverIp.setText(SERVER_IP);
                serverPort.setText(Integer.valueOf(SERVER_PORT).toString());

                connectButton.setDisable(true);
                connectButton.setStyle(
                        "-fx-pref-height: 28px;" +
                                "-fx-pref-width: 150px;" +
                                "-fx-background-color: #34c1da;" +
                                "-fx-text-fill: white;" +
                                "-fx-border-radius: 5;" +
                                "-fx-padding: 3 6 6 6;"
                );

                BooleanBinding booleanBinding = firstNumInput.isValidProperty.and(secondNumInput.isValidProperty);


                resultButton.disableProperty().bind(booleanBinding.not());
                connectButton.setText("Connection accepted");

            } catch (ConnectException e) {
                connectButton.setText("Try again");
                connectButton.setStyle(
                        "-fx-pref-height: 28px;" +
                                "-fx-pref-width: 150px;" +
                                "-fx-background-color:#fe2f57;" +
                                "-fx-text-fill: white;" +
                                "-fx-border-radius: 5;" +
                                "-fx-padding: 3 6 6 6;"
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class ResultButtonHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent actionEvent) {
            String result = firstNumInput.getText() + "_" + secondNumInput.getText();
            try {
                outputStream.writeObject(result);
                Integer i = (Integer) inputStream.readObject();
                resultTextField.setText(i.toString());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static class ValidatingTextField extends TextField {
        private final Predicate<String> validation;
        private BooleanProperty isValidProperty = new SimpleBooleanProperty();

        private ValidatingTextField(Predicate<String> validation) {
            this.validation = validation;

            textProperty().addListener((observableValue, oldValue, newValue) -> {
                isValidProperty.set(validation.test(newValue));
            });
            isValidProperty.set(validation.test(""));
        }
    }

}