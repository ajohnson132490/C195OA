package appointmentmaker;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.ResultSet;

public class Reports {
    private Connection conn;

    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    public ObservableList<ObservableList> appointmentsReport() {
        ObservableList<ObservableList> appointmentData = FXCollections.observableArrayList();
        try {
            //Get all the types and months
            String query = "SELECT DISTINCT type, Month(start), count(*) AS QTY FROM appointments GROUP BY type";
            ResultSet rs = conn.createStatement().executeQuery(query);

            while(rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {

                    //Add the data to a row
                    if (rs.getMetaData().getColumnName(i).equals("Start")) {
                        //Get the data
                        String s = rs.getString(i);

                        //Switch the month from a number to the month name
                        switch (s) {
                                case "1":
                                    row.add("January");
                                    break;
                                case "2":
                                    row.add("February");
                                    break;
                                case "3":
                                    row.add("March");
                                    break;
                                case "4":
                                    row.add("April");
                                    break;
                                case "5":
                                    row.add("May");
                                    break;
                                case "6":
                                    row.add("June");
                                    break;
                                case "7":
                                    row.add("July");
                                    break;
                                case "8":
                                    row.add("August");
                                    break;
                                case "9":
                                    row.add("September");
                                    break;
                                case "10":
                                    row.add("October");
                                    break;
                                case "11":
                                    row.add("November");
                                    break;
                                case "12":
                                    row.add("December");
                                    break;
                            }
                    } else {
                        //Add the data to the row
                        row.add(rs.getString(i));
                    }
                }
                //Add the full row to the observableList
                appointmentData.add(row);
            }
        } catch (Exception e) {
            System.out.println("appointmentsReport: " + e);
        }

        return appointmentData;
    }

    public ObservableList<ObservableList> appointmentsSchedule() {
        ObservableList<ObservableList> appointmentData = FXCollections.observableArrayList();
        try {
            //Get all the types and months
            String query = "SELECT Contact_ID, Appointment_ID, Title, Type, Description, Start, End, Customer_ID FROM appointments " +
                    "ORDER BY Contact_ID, Start";
            ResultSet rs = conn.createStatement().executeQuery(query);

            while(rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {

                    //Add the data to a row
                    if (rs.getMetaData().getColumnName(i).equals("Contact_ID")) {
                        AppointmentHelpers h = new AppointmentHelpers();
                        h.setConnection(conn);
                        //Get the contacts name and add it to the appointment
                        row.add(h.getContact(rs.getInt("Contact_ID")));
                    } else {
                        //Add the data to the row
                        row.add(rs.getString(i));
                    }
                }
                //Add the full row to the observableList
                appointmentData.add(row);
            }
        } catch (Exception e) {
            System.out.println("appointmentsReport: " + e);
        }

        return appointmentData;
    }

    public ObservableList<ObservableList> customerDemographic() {
        ObservableList<ObservableList> appointmentData = FXCollections.observableArrayList();
        try {
            //Get all the types and months
            String query = "SELECT customers.Customer_ID, customers.Customer_Name, countries.Country FROM customers " +
                    "LEFT JOIN first_level_divisions ON customers.Division_ID = first_level_divisions.Division_ID " +
                    "LEFT JOIN countries on first_level_divisions.Country_ID = countries.Country_ID " +
                    "ORDER BY countries.Country";
            ResultSet rs = conn.createStatement().executeQuery(query);

            while(rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Add the data to the row
                    row.add(rs.getString(i));
                }
                //Add the full row to the observableList
                appointmentData.add(row);
            }
        } catch (Exception e) {
            System.out.println("appointmentsReport: " + e);
        }

        return appointmentData;
    }

    public void setColumns(TableView<ObservableList> table, int type) {
        try {
            table.getColumns().clear();
            String query = "";
            ResultSet rs;
            switch (type) {
                case 1:
                    //Query the database
                    query = "SELECT DISTINCT type, Month(start), count(*) AS QTY FROM appointments " +
                            "GROUP BY type";
                    rs = conn.createStatement().executeQuery(query);
                    //Populate the table with columns
                    for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                        final int finI = i;
                        //Create a new column
                        TableColumn col = new TableColumn<>();
                        col.setText(rs.getMetaData().getColumnName(i + 1));
                        col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                            @Override
                            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                                return new SimpleStringProperty(param.getValue().get(finI).toString());
                            }
                        });
                        table.getColumns().add(col);
                    }
                    break;
                case 2:
                    //Query the database
                    query = "SELECT Contact_ID, Appointment_ID, Title, Type, Description, Start, End, Customer_ID FROM appointments " +
                            "ORDER BY Contact_ID, Start";
                    rs = conn.createStatement().executeQuery(query);
                    //Populate the table with columns
                    for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                        final int finI = i;
                        //Create a new column
                        TableColumn col = new TableColumn<>();
                        col.setText(rs.getMetaData().getColumnName(i + 1));
                        col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                            @Override
                            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                                return new SimpleStringProperty(param.getValue().get(finI).toString());
                            }
                        });
                        table.getColumns().add(col);
                    }
                    break;
                case 3:
                    //Query the database
                    query = "SELECT customers.Customer_ID, customers.Customer_Name, countries.Country FROM customers " +
                            "LEFT JOIN first_level_divisions ON customers.Division_ID = first_level_divisions.Division_ID " +
                            "LEFT JOIN countries on first_level_divisions.Country_ID = countries.Country_ID " +
                            "ORDER BY countries.Country";
                    rs = conn.createStatement().executeQuery(query);
                    //Populate the table with columns
                    for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                        final int finI = i;
                        //Create a new column
                        TableColumn col = new TableColumn<>();
                        col.setText(rs.getMetaData().getColumnName(i + 1));
                        col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                            @Override
                            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                                return new SimpleStringProperty(param.getValue().get(finI).toString());
                            }
                        });
                        table.getColumns().add(col);
                    }
                    break;
            }
        } catch (Exception e) {

        }
    }
}
