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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import static java.lang.System.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.ResourceBundle;
import static javafx.application.Application.launch;

/**
 *
 * @author ajohnson132490
 */
public class AppointmentMaker extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        Locale locale = new Locale("fr", "FR");
        ResourceBundle lang = ResourceBundle.getBundle("appointmentmaker.lang", locale);
        String url = "jdbc:mysql://localhost:3306/client_schedule";
        String username = "sqlUser";
        String pass = "Passw0rd!";
        try {
        Connection conn = DriverManager.getConnection(url, username, pass);
        Statement stmt = conn.createStatement();       // Create Statement
        String query = "SELECT * FROM customers";
        ResultSet rs = stmt.executeQuery(query);       // Execute Query
        while (rs.next()) {                            // Process Results
            out.print(rs.getInt("Customer_ID") + "  ");   // Print Columns
            out.print(rs.getString("Customer_Name") + "  ");
            out.print(rs.getString("Address") + "  ");
            out.print(rs.getString("Postal_Code") + "  ");
            out.println(rs.getString("Create_Date"));
        }
        } catch (Exception e) {
            System.out.println(e);
        }
        
        StackPane root = new StackPane();

        
        try {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                System.out.println(lang.getString("greeting"));
            }
        });
        root.getChildren().add(btn);
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
        
        
        Scene scene = new Scene(root, 300, 250);
        
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
