package appointmentmaker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerHelpers {
    private Connection conn;

    /**
     * This function sets the connection to the database used by all the
     * other functions
     *
     * @param conn the connection to the appointment maker database
     */
    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    /**
     * Formats a given address for ease of viewing in the viewCustomers page
     *
     * @param address the address
     * @return a formatted address including the country and address
     */
    public String formatAddress(String address) {
        try {

            //Find the country name
            String query = "SELECT Country FROM countries WHERE Country_ID = "
                    + "(SELECT Country_ID FROM first_level_divisions WHERE division_ID = "
                    + "(SELECT division_ID from customers WHERE Address = ?))";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, address);
            ResultSet rs = stmt.executeQuery();
            rs.next();

            //Find the first level division
            String query2 = "SELECT division FROM first_level_divisions WHERE division_ID = "
                    + "(SELECT division_ID from customers WHERE Address = ?)";
            PreparedStatement stmt2 = conn.prepareStatement(query2);
            stmt2.setString(1, address);
            ResultSet rs2 = stmt2.executeQuery();
            rs2.next();

            //return country name
            if (rs.getString("Country").equals("Canada")) {
                return "Canadian address: " + address + ", " + rs2.getString("division");
            }
            return rs.getString("Country") + " address: " + address + ", " + rs2.getString("division");

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    /**
     * Finds the country that a division is in.
     *
     * @param divisionID the division id
     * @return the country name
     */
    public String getCountry(String divisionID) {
        try {
            //Find the country name
            String query = "SELECT Country FROM countries WHERE Country_ID = "
            + "(SELECT Country_ID FROM first_level_divisions WHERE Division_ID = ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, divisionID);
            ResultSet rs = stmt.executeQuery();
            rs.next();

            //return country name
            return rs.getString("Country");

        } catch (Exception e) {
            System.out.println("getCountry: " + e);
            return "";
        }
    }

    /**
     * Finds the division name given the division id
     *
     * @param divisionID the division id
     * @return the division name
     */
    public String getDivision(String divisionID) {
        try {
            //Find the country name
            String query = "SELECT Division FROM first_level_divisions WHERE Division_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, divisionID);
            ResultSet rs = stmt.executeQuery();
            rs.next();

            //return country name
            return rs.getString("Division");

        } catch (Exception e) {
            System.out.println("getDivision: " + e);
            return "";
        }
    }

    /**
     * Finds the division id given the name of the division.
     *
     * @param division the division name
     * @return the division id
     */
    private String getDivisionId(String division) {
        try {
            String query = "SELECT Division_ID FROM first_level_divisions WHERE Division = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, division);
            ResultSet rs = stmt.executeQuery();
            rs.next();

            return rs.getString("Division_ID");
        } catch (Exception e) {
            System.out.println("getdivisionID: " + e);
        }

        return "";
    }

    /**
     * Finds the current highest csr ID and return the next lowest
     * unique ID
     *
     * @return the lowest unique customer ID
     */
    public int getNextCsrId() {
        try {
            //Query the database
            String query = "SELECT MAX(Customer_ID) FROM customers";
            ResultSet rs = conn.createStatement().executeQuery(query);
            rs.next();

            return rs.getInt(1) + 1;

        } catch (Exception e) {
            System.out.println(e);
        }

        return -1;
    }

    /**
     * Gets a customers information given their ID.
     *
     * @param id the id of the customer
     * @return the customer information
     */
    public ObservableList<String> getCustomer(int id) {
        try {
            String query = "SELECT * FROM customers WHERE Customer_ID = ? ";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, String.valueOf(id));
            ResultSet rs = stmt.executeQuery();

            //Populate the customers data into the data ObservableList
            while(rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Add the data to a row
                    row.add(rs.getString(i));
                }

                //Add the full row to the appointment
                return row;
            }
        } catch (Exception e) {
            System.out.println("getCustomer: " + e);        }

        return null;
    }

    /**
     * This function adds a customer to the database.
     *
     * @param csrID the customers ID
     * @param name customers name
     * @param division first level division where the customer lives
     * @param address customers address
     * @param postalCode customers zip code
     * @param phone customers phone number
     * @param createdBy who created the customer
     * @param currentUser who last updated the customer
     */
    public void addCustomer(String csrID, String name, String division, String address,
                             String postalCode, String phone, String createdBy, String currentUser) {
        try {
            //Create the query
            String query = "INSERT INTO customers "
                    + "(Customer_ID, Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, "
                    + "Last_Update, Last_Updated_By, Division_ID)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);

            //Set all the variables
            stmt.setString(1, csrID);
            stmt.setString(2, name);
            stmt.setString(3, address);
            stmt.setString(4, postalCode);
            stmt.setString(5, phone);
            stmt.setString(6, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            stmt.setString(7, createdBy);
            stmt.setString(8, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            stmt.setString(9, currentUser);
            stmt.setString(10, getDivisionId(division));

            //Execute
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * This function removes all appointments for a given customer, then deletes the customer
     * from the database.
     *
     * @param id the Customer_ID of the customer being deleted
     */
    public void deleteCustomer(String id) {
                try {
                    //Delete associated appointments first
                    String query = "DELETE FROM appointments WHERE Customer_ID = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, id);
                    stmt.executeUpdate();

                    //Delete csr
                    query = "DELETE FROM customers WHERE Customer_ID = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, id);
                    stmt.executeUpdate();

                } catch (Exception ex) {
                    System.out.println("deleteCustomer: " + ex);
                }

    }
}
