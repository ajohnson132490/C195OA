/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package appointmentmaker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.DatePicker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 *
 * @author LabUser
 */
public class IntUtils {
    private Connection conn;
    
    /**
     *
     * @param conn
     */
    public void setConnection(Connection conn) {
        this.conn = conn;
    }
    
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
    
    public String convertTimeToET(String utc) {
        //Get my utc time into a DateFormat
        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        java.util.Date date = null;
        try {
            date = utcFormat.parse(utc);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(AppointmentMaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        DateFormat estFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        estFormat.setTimeZone(TimeZone.getTimeZone("EST"));
        
        return estFormat.format(date);
    }
    
    public String convertTimeToUTC(String est) {
        //Get my utc time into a DateFormat
        DateFormat estFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        estFormat.setTimeZone(TimeZone.getTimeZone("EST"));
        java.util.Date date = null;
        try {
            date = estFormat.parse(est);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(AppointmentMaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        return utcFormat.format(date);
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
                if (current.getTime() <= appointmentStart.getTime()){
                    long difference = ((current.getTime() - appointmentStart.getTime()) / (1000 * 60 * 60 * 24)% 365);

                    if (r == 0 && difference >= -7 || r == 1 && difference >= -31) {
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
                    }
                } else if (r != 0 && r != 1) {
                    throw new IllegalArgumentException("r must be 1 or 0");
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(AppointmentMaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return csrData;
    }
    
    public ObservableList<ObservableList> getAppointments(int userID) {
        ObservableList<ObservableList> userAppointments = FXCollections.observableArrayList();
        try {
            //Query the database
            String query = "SELECT Start, End FROM appointments WHERE User_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, Integer.toString(userID));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                
                //Add the data to the row
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        //Add the data to a row
                        if (rs.getMetaData().getColumnName(i).equals("Start") || 
                                rs.getMetaData().getColumnName(i).equals("End") ||
                                rs.getMetaData().getColumnName(i).equals("Last_Update")) {
                            row.add(convertTimeToET(rs.getString(i)));
                        } else {
                            row.add(rs.getString(i));
                        }
                }
                
                //Add the row to the data
                userAppointments.add(row);
            }
         } catch (SQLException e) {
             System.out.println(e);
         }
        
        return userAppointments;
    }
    
    public int getNextApptId() {
         try {
            //Query the database
            String query = "SELECT MAX(Appointment_ID) FROM appointments";
            ResultSet rs = conn.createStatement().executeQuery(query);
            rs.next();
            
            return rs.getInt(1) + 1;
            
         } catch (Exception e) {
             System.out.println(e);
         }
         
         return -1;
    }
    
    public ObservableList<String> getAllContacts() {
        ObservableList<String> contacts = FXCollections.observableArrayList();
        try {
            //Query the database
            String query = "SELECT Contact_Name FROM Contacts";
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                contacts.add(rs.getString(1));
            }
            return contacts;
            
         } catch (Exception e) {
             System.out.println(e);
         }
         
         return null;
    }
    
    public String getContact(String contact) {
        try {
            //Query the database for the customer
            String query = "SELECT Contact_ID FROM contacts WHERE Contact_Name = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, contact);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return String.valueOf(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e);
            return "Error querying the database for the CONTACT";
        }
        return "Selected CONTACT does not exist";
    }

    public void validateAppointment(String currentUser, String apptID, String title, String desc,
            String loc, String contact, String type, DatePicker sDate, String sTHour,
            String sTMinute, DatePicker eDate, String eTHour, String eTMinute,
            String csr, String user) {
        try {
            //Query the database for the user
            String query = "SELECT * FROM users WHERE User_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user);
            ResultSet userRS = stmt.executeQuery();
            
            //Query the database for the customer
            query = "SELECT * FROM customers WHERE Customer_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, csr);
            ResultSet csrRS = stmt.executeQuery();
            
            //Check for user
            if (!userRS.next()) {
                throw new IllegalArgumentException("Selected USER does not exist");
            }
            
            //Check for customer
            if (!csrRS.next()) {
                throw new IllegalArgumentException("Selected CUSTOMER does not exist");
            }
            
            //Create all variables to check date and time
            LocalDate startDate = sDate.getValue();
            LocalDate endDate = eDate.getValue();
            Date startTime = null;
            Date endTime = null;
            
            //Get my start and end time into date format for comparison
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                startTime = formatter.parse(convertTimeToUTC(startDate.toString() + " " + sTHour + ":" + sTMinute + ":00"));
                endTime = formatter.parse(convertTimeToUTC(endDate.toString() + " " + eTHour + ":" + eTMinute + ":00"));
            } catch (ParseException ex) {
                java.util.logging.Logger.getLogger(AppointmentMaker.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //Check if appointment starts before it ends, and if its within office hours
            if (!duringOfficeHours(eTHour, eTMinute)) {
                throw new IllegalArgumentException("Appointment must end by 22:00 ET");
            }
            if (startDate.compareTo(endDate) > 0) {
                throw new IllegalArgumentException("Start date must be before end date.");
            } 

            //Query the database for all customer appointments
            query = "SELECT Start, End FROM appointments WHERE Customer_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, csr);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                //Format and compare dates
                Date tempStart = formatter.parse(rs.getString(1));
                Date tempEnd = formatter.parse(rs.getString(2));
                
                int resultStart = startTime.compareTo(tempStart);
                int resultEnd = endTime.compareTo(tempEnd);
                
                System.out.println("Start: " + startTime.toString());
                System.out.println("End: " + endTime.toString());
                System.out.println("tempStart: " + tempStart.toString());
                System.out.println("tempEnd: " + tempEnd.toString());

                //Throw an error if there's an appointment with an overlap
                if (startTime.before(tempEnd) && endTime.after(tempStart)) {
                    throw new IllegalArgumentException("This appointment overlaps with an existing appointment for this customer.");
                }        
            }

            //Format the times correctly
            String start = formatter.format(startTime);
            String end = formatter.format(endTime);

            //Add the appt
            addAppointment(currentUser, apptID, title, desc,
            loc, contact, type, start, end, csr, user);
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public boolean duringOfficeHours(String eTHour, String eTMinute) {
        return !(eTHour.equals("22") && !eTMinute.equals("00"));
    }
    
    
    private void addAppointment(String currentUser, String apptID, String title, String desc,
            String loc, String contact, String type, String start, String end,
            String csr, String user) {
        try {
            //Create the query
            String query = "INSERT INTO appointments "
                        + "(Appointment_ID, Title, Description, Location, Type, Start, End,"
                        + "Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID,"
                        + "User_ID, Contact_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                
                //Set all the variables
                stmt.setString(1, apptID);
                stmt.setString(2, title);
                stmt.setString(3, desc);
                stmt.setString(4, loc);
                stmt.setString(5, type);
                stmt.setString(6, start);
                stmt.setString(7, end);
                stmt.setString(8, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                stmt.setString(9, currentUser);
                stmt.setString(10, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                stmt.setString(11, currentUser);
                stmt.setString(12, csr);
                stmt.setString(13, user);
                stmt.setString(14, getContact(contact));
                
                //Execute
                stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
