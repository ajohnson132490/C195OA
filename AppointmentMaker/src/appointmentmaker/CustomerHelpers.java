package appointmentmaker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerHelpers {
    private Connection conn;

    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    /**
     *
     * @param address
     * @return
     */
    public String formatAddress(String address) {
        try {

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
     * Finds the current highest csr ID and return the next lowest
     * unique ID
     *
     * @return the lowest unique appointment ID
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

    private String getDivisionId(String district) {
        try {
            String query = "SELECT Division_ID FROM first_level_divisions WHERE Division = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, district);
            ResultSet rs = stmt.executeQuery();
            rs.next();

            return rs.getString("Division_ID");
        } catch (Exception e) {
            System.out.println("getDivisionID: " + e);
        }

        return "";
    }

    /**
     * Adds an appointment to the database using the data from validateAppointment.
     * <p>
     * This function also explicitly sets the creation date.
     *
     * @param currentUser the user adding the appointment
     * @param apptID the unique appointment id
     * @param title the name of the appointment
     * @param desc appointment description
     * @param loc appointment location
     * @param contact the appointment contact person
     * @param type the type of appointment
     * @param start the starting datetime
     * @param end the ending datetime
     * @param csr the customer
     * @param user the user meeting with the customer
     * @param creationDate the date the appointment was originally created
     */
    public void addCustomer(String csrID, String name, String district, String address,
                             String postalCode, String phone, String creationDate, String createdBy, String currentUser) {
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
            stmt.setString(10, getDivisionId(district));

            //Execute
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
