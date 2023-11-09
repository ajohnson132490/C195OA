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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import static java.lang.System.*;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import static javafx.application.Application.launch;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;



/**
 *
 * @author ajohnson132490
 */
public class AppointmentMaker extends Application {
    private Connection conn;
    private static final String URL = "jdbc:mysql://localhost:3306/client_schedule";
    private static final String USERNAME = "sqlUser";
    private static final String PASSWORD = "Passw0rd!";
    private Locale locale;
    private ResourceBundle lang;
    
    //Helper Functions
    /**
     * This function formats all columns for the customer table 
     * into a more user friendly format without all of the underscores.
     * Any single word attributes are just passed along directly.
     * 
     * @param attribute
     * @return the formatted column name
     */
    public String customerTableColumnName(String attribute) {
        return switch (attribute) {
            case "Customer_ID" -> "ID";
            case "Customer_Name" -> "Name";
            case "Postal_Code" -> "Zip Code";
            case "Create_Date" -> "Create Date";
            case "Created_By" -> "Created By";
            case "Last_Update" -> "Last Updated";
            case "Last_Updated_By" -> "Last Updated By";
            case "Division_ID" -> "Division ID";
            default -> attribute;
        };
    }
    
    /**
     * This function formats all columns for the appointment table 
     * into a more user friendly format without all of the underscores.
     * Any single word attributes are just passed along directly.
     * 
     * @param attribute
     * @return the formatted column name
     */
    public String appointmentTableColumnName(String attribute) {
        return switch (attribute) {
            case "Appointment_ID" -> "ID";
            case "Create_Date" -> "Create Date";
            case "Created_By" -> "Created By";
            case "Last_Update" -> "Last Update";
            case "Last_Updated" -> "Last Updated";
            case "Customer_ID" -> "Customer ID";
            case "User_ID" -> "User ID";
            case "Contact_ID" -> "Contact";
            default -> attribute;
        };
    }
    
    /**
     *
     * @param address
     * @return
     */
    public String formatAddress(String address) {
        try {
            //Connect to the database
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            
            //Find the country name
            String query = "SELECT Country FROM countries WHERE Country_ID = "
                    + "(SELECT Country_ID FROM first_level_divisions WHERE Division_ID = "
                    + "(SELECT Division_ID from customers WHERE Address = ?))";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, address);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            
            //Find the first level division
            String query2 = "SELECT Division FROM first_level_divisions WHERE Division_ID = "
                    + "(SELECT Division_ID from customers WHERE Address = ?)";
            PreparedStatement stmt2 = conn.prepareStatement(query2);
            stmt2.setString(1, address);
            ResultSet rs2 = stmt2.executeQuery();
            rs2.next();
            
            //return country name
            if (rs.getString("Country").equals("Canada")) {
                return "Canadian address: " + address + ", " + rs2.getString("Division");
            }
            return rs.getString("Country") + " address: " + address + ", " + rs2.getString("Division");
            
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
    
    /**
     *
     * @param utc
     * @return
     */
    public String convertTimeToLocal(String utc) {
        //Get my utc time into a DateFormat
        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        java.util.Date date = null;
        try {
            date = utcFormat.parse(utc);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(AppointmentMaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        DateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        localFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(ZoneId.systemDefault().toString())));
        
        return localFormat.format(date);
    }
    
    /**
     * This function finds all appointments within a given range, and returns 
     * all of those appointments in a list.
     * <p>
     * When r is 0, the range is 7 days. When r is 1, the range is 31 days.
     * 
     * @param r the range of dates
     * @param rs the result set of the database query of all appointments
     * @return an ObservableList of all the appointments within the specified range
     */
    public ObservableList<ObservableList> getAppointments(int r, ResultSet rs) {
        ObservableList<ObservableList> csrData = FXCollections.observableArrayList();
        try {
            //Populate the customers data into the data ObservableList
            while(rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                
                //Get current datetime and the appointment datetime
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now(); 
                java.util.Date current = Date.from(now.atZone(TimeZone.getTimeZone("UTC").toZoneId()).toInstant());
                java.util.Date appointmentStart = format.parse(rs.getString(6));
                
                //Check the difference
                long difference = ((current.getTime() - appointmentStart.getTime()) / (1000 * 60 * 60 * 24)% 365);
                //System.out.println("Difference (Years): " + ((current.getTime() - appointmentStart.getTime()) / (1000l * 60 * 60 * 24 * 365)));
                //System.out.println("Difference (Days): " + ((current.getTime() - appointmentStart.getTime()) / (1000 * 60 * 60 * 24)% 365));
                
                
                /*DELETE COMMENTS BEFORE IF STATEMENT ONCE NEW APPOINTMENTS CAN BE CREATED*/
                
                
                //if (r == 0 && difference <= 7 || r ==1 && difference <= 31) {
                    System.out.println("There shouldn't be any appointments here...");
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        //Add the data to a row
                        if (rs.getMetaData().getColumnName(i).equals("Start") || 
                                rs.getMetaData().getColumnName(i).equals("End") ||
                                rs.getMetaData().getColumnName(i).equals("Last_Update")) {
                            row.add(convertTimeToLocal(rs.getString(i)));
                        } else {
                            row.add(rs.getString(i));
                        }
                    }
                
                
                    //Add the full row to the observableList
                    csrData.add(row);
                //} else if (r != 0 && r != 1) {
                //    throw new IllegalArgumentException("r must be 1 or 0");
                //}
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(AppointmentMaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return csrData;
    }
    
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
                    conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                    
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
        //Add all items to the root
        Pane root = new Pane();
        root.getChildren().add(mainVBox);
        mainVBox.getStyleClass().add("mainPage");
        
        //Create Scene
        Scene scene = new Scene(root, 1250, 600);
        scene.getStylesheets().add(getClass().getResource("resources/stylesheet.css").toExternalForm());
        
        //Create title HBox
        HBox upper = new HBox();
        upper.setAlignment(Pos.TOP_LEFT);
        Label title = new Label("View Customers");
        title.setStyle("-fx-font: 24 ariel;");
        upper.getChildren().add(title);
        mainVBox.getChildren().add(upper);
        
        //Creating tableview VBox
        VBox tableVBox = new VBox(5);
        
        //All Customers TableView Table
        TableView customersTable = new TableView();
                customersTable.setPrefWidth(1200);

        ObservableList<ObservableList> csrData = FXCollections.observableArrayList();
        try {
            //Connect to the database
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            
            //Query the database
            String query = "SELECT * FROM customers";
            ResultSet rs = conn.createStatement().executeQuery(query);
            
            //Populate the table with columns
            for (int i = 0; i<rs.getMetaData().getColumnCount(); i++) {
                final int finI = i;
                //Create a new column
                TableColumn col = new TableColumn<>();
                col.setText(customerTableColumnName(rs.getMetaData().getColumnName(i+1)));    
                
                //Set Column formatting
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){                    
                    @Override
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {                                                                                              
                        return new SimpleStringProperty(param.getValue().get(finI).toString());                        
                    }                    
                });
                
                //Add column to the table
                customersTable.getColumns().add(col);
            }
            
            //Populate the customers data into the data ObservableList
            while(rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Add the data to a row
                    if (rs.getMetaData().getColumnName(i).equals("Address")) {
                        row.add(formatAddress(rs.getString(i)));
                        
                    } else {
                        row.add(rs.getString(i));

                    }
                }
                //Add the full row to the observableList
                csrData.add(row);
            }
            
            //Populate table with customer data
            customersTable.setItems(csrData);   
        } catch (Exception e) {
            System.out.println(e);
        }
        
        tableVBox.getChildren().add(customersTable);
        
        //Buttons HBox
        HBox buttons = new HBox(15);
        buttons.setPadding(new Insets(0, 0, 0, 995));
        
        //Create the buttons
        Button addBtn = new Button("Add");
        EventHandler<ActionEvent> addEvent = (ActionEvent e) -> {
            //todo: create add form
        };
        addBtn.setOnAction(addEvent);
        
        Button updateBtn = new Button("Update");
        EventHandler<ActionEvent> updateEvent = (ActionEvent e) -> {
            //todo: create update form
        };
        updateBtn.setOnAction(updateEvent);
        
        Button deleteBtn = new Button("Delete");
        EventHandler<ActionEvent> deleteEvent = (ActionEvent e) -> {
            //todo: figure out how to delete items and refresh the tableview
        };
        deleteBtn.setOnAction(deleteEvent);
        
        //Add the buttons to the table VBox
        buttons.getChildren().addAll(addBtn, updateBtn, deleteBtn);
        tableVBox.getChildren().add(buttons);

        //Add the tableview VBox to the main VBox
        mainVBox.getChildren().add(tableVBox);
        
        //Lower page controls
        HBox lower = new HBox();
        Button viewAppointmentsBtn = new Button("View Appointments");
        EventHandler<ActionEvent> viewAppointmentsEvent = (ActionEvent e) -> {
            viewAppointments(primaryStage);
        };
        viewAppointmentsBtn.setOnAction(viewAppointmentsEvent);
        viewAppointmentsBtn.setPrefWidth(625);
        viewAppointmentsBtn.setPrefHeight(75);
        
        Button viewCustomersBtn = new Button("View Customers");
        EventHandler<ActionEvent> viewCustomersEvent = (ActionEvent e) -> {
            viewCustomers(primaryStage);
        };
        viewCustomersBtn.setOnAction(viewCustomersEvent);
        viewCustomersBtn.setPrefWidth(625);
        viewCustomersBtn.setPrefHeight(75);
        
        //Add buttons to lower HBox
        lower.getChildren().addAll(viewAppointmentsBtn, viewCustomersBtn);
        mainVBox.getChildren().add(lower);
        
        
        primaryStage.setTitle("View Customers");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public void viewAppointments(Stage primaryStage) {
        //Create the main VBox
        VBox mainVBox = new VBox(15);
        //Add all items to the root
        Pane root = new Pane();
        root.getChildren().add(mainVBox);
        mainVBox.getStyleClass().add("mainPage");
        
        //Create Scene
        Scene scene = new Scene(root, 1250, 625);
        scene.getStylesheets().add(getClass().getResource("resources/stylesheet.css").toExternalForm());
        
        //Create title HBox
        HBox upper = new HBox();
        upper.setAlignment(Pos.TOP_LEFT);
        Label title = new Label("View Appointments");
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
        TableView customersTable = new TableView();
                customersTable.setPrefWidth(1200);

        ObservableList<ObservableList> csrData = FXCollections.observableArrayList();
        try {
            //Connect to the database
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            
            //Query the database
            String query = "SELECT * FROM appointments";
            ResultSet rs = conn.createStatement().executeQuery(query);
            
            //Populate the table with columns
            for (int i = 0; i<rs.getMetaData().getColumnCount(); i++) {
                final int finI = i;
                //Create a new column
                TableColumn col = new TableColumn<>();
                col.setText(appointmentTableColumnName(rs.getMetaData().getColumnName(i+1)));    
                
                //Set Column formatting
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){                    
                    @Override
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {                                                                                              
                        return new SimpleStringProperty(param.getValue().get(finI).toString());                        
                    }                    
                });
                
                //Add column to the table
                customersTable.getColumns().add(col);
            }
            
            //TODO READ BASED ON RADIO BUTTONS
            csrData = getAppointments(0, rs);
            
            //Populate table with customer data
            customersTable.setItems(csrData);   
        } catch (SQLException e) {
            System.out.println(e);
        }
        
        tableVBox.getChildren().add(customersTable);
        
        //Buttons HBox
        HBox buttons = new HBox(15);
        buttons.setPadding(new Insets(0, 0, 0, 995));
        
        //Create the buttons
        Button addBtn = new Button("Add");
        EventHandler<ActionEvent> addEvent = (ActionEvent e) -> {
            //todo: create add form
        };
        addBtn.setOnAction(addEvent);
        
        Button updateBtn = new Button("Update");
        EventHandler<ActionEvent> updateEvent = (ActionEvent e) -> {
            //todo: create update form
        };
        updateBtn.setOnAction(updateEvent);
        
        Button deleteBtn = new Button("Delete");
        EventHandler<ActionEvent> deleteEvent = (ActionEvent e) -> {
            //todo: figure out how to delete items and refresh the tableview
        };
        deleteBtn.setOnAction(deleteEvent);
        
        //Add the buttons to the table VBox
        buttons.getChildren().addAll(addBtn, updateBtn, deleteBtn);
        tableVBox.getChildren().add(buttons);

        //Add the tableview VBox to the main VBox
        mainVBox.getChildren().add(tableVBox);
        
        //Lower page controls
        HBox lower = new HBox();
        Button viewAppointmentsBtn = new Button("View Appointments");
        EventHandler<ActionEvent> viewAppointmentsEvent = (ActionEvent e) -> {
            viewAppointments(primaryStage);
        };
        viewAppointmentsBtn.setOnAction(viewAppointmentsEvent);
        viewAppointmentsBtn.setPrefWidth(625);
        viewAppointmentsBtn.setPrefHeight(75);
        
        Button viewCustomersBtn = new Button("View Customers");
        EventHandler<ActionEvent> viewCustomersEvent = (ActionEvent e) -> {
            viewCustomers(primaryStage);
        };
        viewCustomersBtn.setOnAction(viewCustomersEvent);
        viewCustomersBtn.setPrefWidth(625);
        viewCustomersBtn.setPrefHeight(75);
        
        //Add buttons to lower HBox
        lower.getChildren().addAll(viewAppointmentsBtn, viewCustomersBtn);
        mainVBox.getChildren().add(lower);
        
        
        primaryStage.setTitle("View Appointments");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
}
