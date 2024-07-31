package appointmentmaker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
}
