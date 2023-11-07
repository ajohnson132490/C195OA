/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package appointmentmaker;

import java.sql.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import static java.lang.System.*;
import java.lang.reflect.InvocationTargetException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.ResourceBundle;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;



/**
 *
 * @author ajohnson132490
 */
public class AppointmentMaker extends Application {
    private static final String URL = "jdbc:mysql://localhost:3306/client_schedule";
    private static final String USERNAME = "sqlUser";
    private static final String PASSWORD = "Passw0rd!";
    Locale locale;
    ResourceBundle lang;
    
    /**
     * The start function is the initial landing screen of the application
     * where the user will be able to log in and see their current location.
     * <p>
     * Language is determined in the start function.
     * 
     * @param primaryStage the main window of the application
     */
    @Override
    public void start(Stage primaryStage) {
        ///Start on the Login screen
        //Set Language
        locale = new Locale("fr", "FR");
        lang = ResourceBundle.getBundle("appointmentmaker.lang", locale);
        
        //Create Scene
        Pane root = new Pane();
        Scene scene = new Scene(root, 350, 400);
        scene.getStylesheets().add(getClass().getResource("resources/stylesheet.css").toExternalForm());
        
        //Create the main VBox
        VBox mainVBox = new VBox();
        Label title = new Label("Appointment Maker");
        title.setStyle("-fx-font: 24 ariel;");
        mainVBox.getChildren().add(title);
        
        //Username and Password fields
        VBox form = new VBox();
        form.getStyleClass().add("loginForm");
        
        Label uName = new Label("Username");
        TextField uField = new TextField();
        
        Label pWord = new Label("Password");
        TextField pField = new TextField();
        
        //Login Button
        HBox loginBtnPadding = new HBox();
        loginBtnPadding.setPadding(new Insets(10, 0, 0, 150));
        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(50);
        loginBtnPadding.getChildren().add(loginBtn);
        loginBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    //Connect to the database
                    Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                    
                    //Query the database
                    String query = "SELECT Password FROM users WHERE User_Name = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, uField.getText());
                    ResultSet rs = stmt.executeQuery();
                    
                    //Check if the password entered matches the username
                    if (rs.next() && rs.getString("Password").equals(pField.getText())) {
                        System.out.println("Login Success");
                        viewCustomers(primaryStage);
                    } else {
                        System.out.println("Username or password is incorrect");
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ENTER:    
                        try {
                            //Connect to the database
                            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                            
                            //Query the database
                            String query = "SELECT Password FROM users WHERE User_Name = ?";
                            PreparedStatement stmt = conn.prepareStatement(query);
                            stmt.setString(1, uField.getText());
                            ResultSet rs = stmt.executeQuery();
                            
                            //Check if the password entered matches the username
                            if (rs.next() && rs.getString("Password").equals(pField.getText())) {
                                System.out.println("Login Success");
                                viewCustomers(primaryStage);
                            } else {
                                System.out.println("Username or password is incorrect");
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                }
            }
        });
        
        //Add Form parts to form VBox
        form.getChildren().add(uName);
        form.getChildren().add(uField);
        form.getChildren().add(pWord);
        form.getChildren().add(pField);
        form.getChildren().add(loginBtnPadding);
        mainVBox.getChildren().add(form);
        
        //User Location
        HBox lower = new HBox();
        lower.setPadding(new Insets(70, 0, 0, 150));
        ZoneId zoneID = ZoneId.systemDefault();
        Label location = new Label("Location: " + zoneID.toString());
        location.setPrefWidth(150);        
        lower.getChildren().add(location);
        mainVBox.getChildren().add(lower);
        
        
        
        //Add all items to the root
        root.getChildren().add(mainVBox);
        mainVBox.getStyleClass().add("loginRoot");
        
        //Set Stage
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public void viewCustomers(Stage primaryStage) {
        //Create the main VBox
        VBox mainVBox = new VBox(15);
        
        //Create title HBox
        HBox upper = new HBox();
        upper.setAlignment(Pos.TOP_LEFT);
        Label title = new Label("View Customers");
        title.setStyle("-fx-font: 24 ariel;");
        upper.getChildren().add(title);
        mainVBox.getChildren().add(upper);
        
        //Creating tableview VBox
        VBox tableVBox = new VBox(5);
        
        //Week or Month Radio Buttons
        HBox radioButtons = new HBox(20);
        ToggleGroup weekOrMonth = new ToggleGroup();
        RadioButton week = new RadioButton("Week");
        week.setToggleGroup(weekOrMonth);
        week.setSelected(true);
        RadioButton month = new RadioButton("Month");
        month.setToggleGroup(weekOrMonth);
        radioButtons.getChildren().addAll(week, month);
        tableVBox.getChildren().add(radioButtons);
        
        //All Customers TableView Table
        TableView customers = new TableView();
        
        
        
        
        
        
        //Add the tableview VBox to the main VBox
        mainVBox.getChildren().add(tableVBox);
        
        //Add all items to the root
        Pane root = new Pane();
        root.getChildren().add(mainVBox);
        mainVBox.getStyleClass().add("mainPage");
        
        //Create Scene
        Scene scene = new Scene(root, 700, 400);
        scene.getStylesheets().add(getClass().getResource("resources/stylesheet.css").toExternalForm());
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
}
