package appointmentmaker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.ResultSet;

public class Reports {
    private Connection conn;

    public ObservableList<ObservableList> appointmentsReport() {
        ObservableList<ObservableList> appointmentData = FXCollections.observableArrayList();
        try {
            //Get all the types and months
            String query = "SELECT type, Month(Start) FROM appointments;";
            ResultSet rs = conn.createStatement().executeQuery(query);

            while(rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {

                        //Add the data to a row
                        if (rs.getMetaData().getColumnName(i).equals("Start") ||
                                rs.getMetaData().getColumnName(i).equals("End") ||
                                rs.getMetaData().getColumnName(i).equals("Last_Update") ||
                                rs.getMetaData().getColumnName(i).equals("Create_Date")) {
                            String s = rs.getString(i);
                            row.add(s);
                        } else {
                            row.add(rs.getString(i));
                        }
                        //Add the full row to the observableList
                            appointmentData.add(row);
                        }
            }
        } catch (Exception e) {
            System.out.println("appointmentsReport: " + e);
        }

        return appointmentData;
    }
}
