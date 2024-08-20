package appointmentmaker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class contains functions designed to abstract the functionality of creating, modifying,
 * deleting, and viewing appointments.
 *
 * @author Austin Johnson
 */
public class AppointmentHelpers {
    private Connection conn;

    /**
     * This function formats all columns for the appointment table
     * into a more user-friendly format without all the underscores.
     * Any single word attributes are just passed along directly.
     * <p>
     * Lambda function in switch statement added to
     * reduce the size of the switch statement.
     * </p>
     *
     * @param attribute the unform
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
     * Converts the given time from UTC to the local timezone on the
     * users' computer.
     *
     * @param utc the time in UTC
     * @return the time in the local time zone
     */
    public String convertTimeToLocal(String utc) {
        //Get my utc time into a DateFormat
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            cal.setTime(formatter.parse(utc));
        } catch (Exception e) {
        }

        formatter.setTimeZone(TimeZone.getTimeZone(ZoneId.of(ZoneId.systemDefault().toString())));

        return formatter.format(cal.getTime());
    }

    /**
     * Converts the given time from local time to UTC
     * for server storage.
     *
     * @param loc the time in the local time zone
     * @return the time in UTC timezone
     */
    public String convertTimeToUTC(String loc) {
        //Get my utc time into a DateFormat
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone(ZoneId.of(ZoneId.systemDefault().toString())));
        try {
            cal.setTime(formatter.parse(loc));
        } catch (Exception e) {
        }

        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return formatter.format(cal.getTime());
    }

    /**
     * Converts the given time from local time to EST
     *
     * @param time the time in the local time zone
     * @return the time in EST timezone
     */
    public String convertTimeToEST(String time) {
        //Get my utc time into a DateFormat
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone(ZoneId.of(ZoneId.systemDefault().toString())));
        try {
            cal.setTime(formatter.parse(time));
        } catch (Exception e) {
            System.out.println("convertToEST: " + e);
        }

        formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        return formatter.format(cal.getTime());
    }

    /**
     * This function finds all appointments within a given range, and returns
     * all of those appointments in a list.
     * <p>
     * When time is 'w', the range is 7 days. When time is 'm', the range is 31 days.
     * When the time is 'a', it returns all appointments.
     *
     * @param time a char that allows the user to select a week or month time frame
     *             denoted by 'w' for week or 'm' for month or 'a' for all
     * @return an ObservableList of all the appointments within the specified range
     */
    public ObservableList<ObservableList> getAppointments(char time) {
        ObservableList<ObservableList> csrData = FXCollections.observableArrayList();
        try {
            String query = "SELECT * FROM appointments";
            ResultSet rs = conn.createStatement().executeQuery(query);
            //Populate the customers data into the data ObservableList
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();

                //Get current datetime and the appointment datetime
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                java.util.Date current = Date.from(now.atZone(TimeZone.getTimeZone("UTC").toZoneId()).toInstant());
                java.util.Date appointmentStart = format.parse(rs.getString(6));

                //Check the difference
                if (current.getTime() <= appointmentStart.getTime()) {
                    long difference = ((current.getTime() - appointmentStart.getTime()) / (1000 * 60 * 60 * 24) % 365);

                    if (time == 'w' && difference >= -7 || time == 'm' && difference >= -31) {
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            //Add the data to a row
                            if (rs.getMetaData().getColumnName(i).equals("Start") ||
                                    rs.getMetaData().getColumnName(i).equals("End") ||
                                    rs.getMetaData().getColumnName(i).equals("Last_Update") ||
                                    rs.getMetaData().getColumnName(i).equals("Create_Date")) {
                                String s = rs.getString(i);
                                row.add(convertTimeToLocal(s));
                            } else {
                                row.add(rs.getString(i));
                            }
                        }
                        //Add the full row to the observableList
                        csrData.add(row);
                    }
                } else if (time == 'a') {
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        //Add the data to a row
                        if (rs.getMetaData().getColumnName(i).equals("Start") ||
                                rs.getMetaData().getColumnName(i).equals("End") ||
                                rs.getMetaData().getColumnName(i).equals("Last_Update") ||
                                rs.getMetaData().getColumnName(i).equals("Create_Date")) {
                            String s = rs.getString(i);
                            row.add(convertTimeToLocal(s));
                        } else {
                            row.add(rs.getString(i));
                        }
                    }
                    //Add the full row to the observableList
                    csrData.add(row);
                }
            }
        } catch (Exception e) {
            System.out.println("getAppointments: " + e);
        }

        return csrData;
    }

    /**
     * This function gets an appointment from the server and
     * populates the data into an ObservableList<String> for
     * the update appointment screen
     *
     * @param id the unique ID of the appointment being retrieved
     * @return an ObservableList containing all the appointment's data
     */
    public ObservableList<String> getAppointment(int id) {
        try {
            String query = "SELECT * FROM appointments WHERE Appointment_ID = ? ";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, String.valueOf(id));
            ResultSet rs = stmt.executeQuery();

            //Populate the customers data into the data ObservableList
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Add the data to a row
                    if (rs.getMetaData().getColumnName(i).equals("Start") ||
                            rs.getMetaData().getColumnName(i).equals("End") ||
                            rs.getMetaData().getColumnName(i).equals("Last_Update") ||
                            rs.getMetaData().getColumnName(i).equals("Create_Date")) {
                        row.add(convertTimeToLocal(rs.getString(i)));
                    } else {
                        row.add(rs.getString(i));
                    }
                }

                //Add the full row to the appointment
                return row;
            }
        } catch (Exception e) {
            System.out.println("getAppointment: " + e);
        }

        return null;
    }

    /**
     * Finds the current highest appointment ID and return the next lowest
     * unique ID
     *
     * @return the lowest unique appointment ID
     */
    public int getNextApptId() {
        try {
            //Query the database
            String query = "SELECT MAX(Appointment_ID) FROM appointments";
            ResultSet rs = conn.createStatement().executeQuery(query);
            rs.next();

            return rs.getInt(1) + 1;

        } catch (Exception e) {
            System.out.println("getNextApptID: " + e);
        }

        return -1;
    }

    /**
     * Gets the names of all the contacts in the contact table
     * and returns it as a list.
     *
     * @return an ObservableList of all the contacts' names
     */
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
            System.out.println("getAllContats: " + e);
        }

        return null;
    }

    /**
     * Finds a contact's ID given the name.
     *
     * @param contact the name of the contact
     * @return the contact's ID
     */
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
            System.out.println("getContact: " + e);
            return "Error querying the database for the CONTACT";
        }
        return "Selected CONTACT does not exist";
    }

    /**
     * Finds a contact's name given the ID.
     *
     * @param contact the contact's ID
     * @return the contact's name
     */
    public String getContact(int contact) {
        try {
            //Query the database for the customer
            String query = "SELECT Contact_Name FROM contacts WHERE Contact_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, String.valueOf(contact));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            System.out.println("getContact: " + e);
            return "Error querying the database for the CONTACT";
        }
        return "Selected CONTACT does not exist";
    }

    /**
     * This function validates that the data entered into the add appointment page is valid.
     * <p>
     * Its validity is verified by confirming that the user and customer exist, that the
     * appointment is during office hours, and that the appointment doesn't overlap with
     * any other appointment.
     * </p>
     * <p>
     * Lambda used for closing the dialog window helps reduce the number of lines and
     * complexity of the code.
     * </p>
     *
     * @param currentUser  the current user logged in
     * @param apptID       the unique appointment id
     * @param title        the name of the appointment
     * @param desc         appointment description
     * @param loc          appointment location
     * @param contact      the appointment contact person
     * @param type         the type of appointment
     * @param sDate        the day the appointment starts
     * @param sTHour       the hour the appointment starts
     * @param sTMinute     the minute the appointment starts
     * @param eDate        the day the appointment ends
     * @param eTHour       the hour the appointment ends
     * @param eTMinute     the minute the appointment ends
     * @param csr          the customer
     * @param user         the user meeting with the customer
     * @param primaryStage the primary stage of the project
     * @return returns true if validation was successful, false if there was an error
     */
    public boolean validateAppointment(String currentUser, String apptID, String title, String desc,
                                       String loc, String contact, String type, DatePicker sDate, String sTHour,
                                       String sTMinute, DatePicker eDate, String eTHour, String eTMinute,
                                       String csr, String user, Stage primaryStage) {
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
                System.out.println("Selected USER does not exist");
                return false;
            }

            //Check for customer
            if (!csrRS.next()) {
                System.out.println("Selected CUSTOMER does not exist");
                return false;
            }

            //Create all variables to check date and time
            LocalDate startDate = sDate.getValue();
            LocalDate endDate = eDate.getValue();
            Date startTime = null;
            Date endTime = null;

            //Get my start and end time into date format for comparison
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                startTime = formatter.parse(startDate.toString() + " " + sTHour + ":" + sTMinute + ":00");
                endTime = formatter.parse(endDate.toString() + " " + eTHour + ":" + eTMinute + ":00");
                if (!duringOfficeHours(formatter.format(startTime),
                        formatter.format(endTime))) {
                    final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(primaryStage);
                    VBox dialogVbox = new VBox(20);
                    Button okBtn = new Button("Ok");
                    dialogVbox.getChildren().add(new Text("Appointment not during office hours\n 8:00am EST - 10:00pm EST."));
                    dialogVbox.getChildren().add(okBtn);
                    Scene dialogScene = new Scene(dialogVbox, 300, 100);
                    dialogScene.getStylesheets().add(getClass().getResource("resources/stylesheet.css").toExternalForm());
                    dialogVbox.getStyleClass().add("dialogBox");
                    dialog.setScene(dialogScene);
                    dialog.show();

                    EventHandler<ActionEvent> okEvent = (ActionEvent ee) -> {
                        dialog.close();
                    };
                    okBtn.setOnAction(okEvent);
                    System.out.println("Appointment not during business hours");
                    return false;
                }
                startTime = formatter.parse(convertTimeToUTC(formatter.format(startTime)));
                endTime = formatter.parse(convertTimeToUTC(formatter.format(endTime)));
            } catch (ParseException ex) {
                System.out.println("validateAppointment: " + ex);
                return false;
            }

            if (startDate.isAfter(endDate)) {
                System.out.println("Start date must be before end date.");
                return false;
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

                //Throw an error if there's an appointment with an overlap
                if (startTime.before(tempEnd) && endTime.after(tempStart)) {
                    final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(primaryStage);
                    VBox dialogVbox = new VBox(20);
                    Button okBtn = new Button("Ok");
                    dialogVbox.getChildren().add(new Text("This appointment overlaps with an \nexisting appointment for this customer."));
                    dialogVbox.getChildren().add(okBtn);
                    Scene dialogScene = new Scene(dialogVbox, 300, 100);
                    dialogScene.getStylesheets().add(getClass().getResource("resources/stylesheet.css").toExternalForm());
                    dialogVbox.getStyleClass().add("dialogBox");
                    dialog.setScene(dialogScene);
                    dialog.show();

                    EventHandler<ActionEvent> okEvent = (ActionEvent ee) -> {
                        dialog.close();
                    };
                    okBtn.setOnAction(okEvent);
                    return false;
                }
            }

            //Format the times correctly
            String start = formatter.format(startTime);
            String end = formatter.format(endTime);

            //Add the appt
            addAppointment(currentUser, apptID, title, desc,
                    loc, contact, type, start, end, csr, user);

        } catch (Exception e) {
            System.out.println("validateAppointment: " + e);
            return false;
        }
        return true;
    }

    /**
     * This function validates that the data entered into the update appointment page is valid.
     * <p>
     * Its validity is verified by confirming that the user and customer exist, that the
     * appointment is during office hours, and that the appointment doesn't overlap with
     * any other appointment.
     * </p>
     * <p>
     * This function explicitly notes the original appointment creator and creation date instead of assigning the
     * current user as the creator and the current date as the creation date.
     * </p>
     * <p>
     * Lambda used for closing the dialog window helps reduce the number of lines and
     * complexity of the code.
     * </p>
     *
     * @param currentUser  the current user logged in
     * @param apptID       the unique appointment id
     * @param title        the name of the appointment
     * @param desc         appointment description
     * @param loc          appointment location
     * @param contact      the appointment contact person
     * @param type         the type of appointment
     * @param sDate        the day the appointment starts
     * @param sTHour       the hour the appointment starts
     * @param sTMinute     the minute the appointment starts
     * @param eDate        the day the appointment ends
     * @param eTHour       the hour the appointment ends
     * @param eTMinute     the minute the appointment ends
     * @param csr          the customer
     * @param user         the user meeting with the customer
     * @param createdBy    the user who created the appointment
     * @param creationDate the day the appointment was first created
     * @param primaryStage the primary stage of the project
     * @return returns true if validation was sucessful, false if there was an error
     */
    public boolean validateAppointment(String currentUser, String apptID, String title, String desc,
                                       String loc, String contact, String type, DatePicker sDate, String sTHour,
                                       String sTMinute, DatePicker eDate, String eTHour, String eTMinute,
                                       String csr, String user, String createdBy, String creationDate, Stage primaryStage) {
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
                System.out.println("Selected USER does not exist");
                return false;
            }

            //Check for customer
            if (!csrRS.next()) {
                System.out.println("Selected CUSTOMER does not exist");
                return false;
            }

            //Create all variables to check date and time
            LocalDate startDate = sDate.getValue();
            LocalDate endDate = eDate.getValue();
            Date startTime = null;
            Date endTime = null;

            //Get my start and end time into date format for comparison
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                startTime = formatter.parse(startDate.toString() + " " + sTHour + ":" + sTMinute + ":00");
                endTime = formatter.parse(endDate.toString() + " " + eTHour + ":" + eTMinute + ":00");
                if (!duringOfficeHours(formatter.format(startTime),
                        formatter.format(endTime))) {
                    final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(primaryStage);
                    VBox dialogVbox = new VBox(20);
                    Button okBtn = new Button("Ok");
                    dialogVbox.getChildren().add(new Text("Appointment not during office hours\n 8:00am EST - 10:00pm EST."));
                    dialogVbox.getChildren().add(okBtn);
                    Scene dialogScene = new Scene(dialogVbox, 300, 100);
                    dialogScene.getStylesheets().add(getClass().getResource("resources/stylesheet.css").toExternalForm());
                    dialogVbox.getStyleClass().add("dialogBox");
                    dialog.setScene(dialogScene);
                    dialog.show();

                    EventHandler<ActionEvent> okEvent = (ActionEvent ee) -> {
                        dialog.close();
                    };
                    okBtn.setOnAction(okEvent);
                    return false;
                }
                startTime = formatter.parse(convertTimeToUTC(formatter.format(startTime)));
                endTime = formatter.parse(convertTimeToUTC(formatter.format(endTime)));
            } catch (ParseException ex) {
                System.out.println("validateAppointment: " + ex);
                return false;
            }

            if (startDate.isAfter(endDate)) {
                System.out.println("Start date must be before end date.");
                return false;
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

                //Throw an error if there's an appointment with an overlap
                if (startTime.before(tempEnd) && endTime.after(tempStart)) {
                    final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(primaryStage);
                    VBox dialogVbox = new VBox(20);
                    Button okBtn = new Button("Ok");
                    dialogVbox.getChildren().add(new Text("This appointment overlaps with an \nexisting appointment for this customer."));
                    dialogVbox.getChildren().add(okBtn);
                    Scene dialogScene = new Scene(dialogVbox, 300, 100);
                    dialogScene.getStylesheets().add(getClass().getResource("resources/stylesheet.css").toExternalForm());
                    dialogVbox.getStyleClass().add("dialogBox");
                    dialog.setScene(dialogScene);
                    dialog.show();

                    EventHandler<ActionEvent> okEvent = (ActionEvent ee) -> {
                        dialog.close();
                    };
                    okBtn.setOnAction(okEvent);
                    return false;
                }
            }

            //Format the times correctly
            String start = formatter.format(startTime);
            String end = formatter.format(endTime);

            //Add the appt
            addAppointment(currentUser, apptID, title, desc,
                    loc, contact, type, start, end, csr, user, createdBy, creationDate);

        } catch (Exception e) {
            System.out.println("validateAppointment: " + e);
            return false;
        }
        return true;
    }

    /**
     * Checks if the appointment ends by the end of the day.
     *
     * @param startTime the starting time of the appointment
     * @param endTime the ending time of the appointment
     * @return returns true if the appointment is during office hours, and false if the appointment goes past the end of the day
     */
    public boolean duringOfficeHours(String startTime, String endTime) {
        //Convert times from local to est
        startTime = convertTimeToEST(startTime);
        endTime = convertTimeToEST(endTime);

        //Format times into a localdatetime object for comparing
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);

        return start.getHour() >= 8 && end.getHour() <= 22 && end.getHour() >= 8 && (end.getHour() != 22 || end.getMinute() <= 0);
    }

    /**
     * Adds an appointment to the database using the data from validateAppointment.
     *
     * @param currentUser the current user logged in
     * @param apptID      the unique appointment id
     * @param title       the name of the appointment
     * @param desc        appointment description
     * @param loc         appointment location
     * @param contact     the appointment contact person
     * @param type        the type of appointment
     * @param start       the starting datetime
     * @param end         the ending datetime
     * @param csr         the customer
     * @param user        the user meeting with the customer
     */
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
            System.out.println("addAppointment: " + e);
        }
    }

    /**
     * Adds an appointment to the database using the data from validateAppointment.
     * <p>
     * This function also explicitly sets the creation date.
     *
     * @param currentUser  the current user logged in
     * @param apptID       the unique appointment id
     * @param title        the name of the appointment
     * @param desc         appointment description
     * @param loc          appointment location
     * @param contact      the appointment contact person
     * @param type         the type of appointment
     * @param start        the starting datetime
     * @param end          the ending datetime
     * @param csr          the customer
     * @param user         the user meeting with the customer
     * @param creationDate the date the appointment was originally created
     */
    private void addAppointment(String currentUser, String apptID, String title, String desc,
                                String loc, String contact, String type, String start, String end,
                                String csr, String user, String createdBy, String creationDate) {
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
            stmt.setString(8, creationDate);
            stmt.setString(9, createdBy);
            stmt.setString(10, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            stmt.setString(11, currentUser);
            stmt.setString(12, csr);
            stmt.setString(13, user);
            stmt.setString(14, getContact(contact));

            //Execute
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("addAppointment: " + e);
        }
    }

    /**
     * This function deletes the given appointment from the
     * appointment table.
     *
     * @param id the id of the appointment to be deleted
     */
    public void deleteAppointment(String id) {
        try {
            //Query the database for the customer
            String query = "DELETE FROM appointments WHERE Appointment_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, id);
            stmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("deleteAppointment: " + e);
        }
    }

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
     * Creates a notification window for the user if there is an appointment
     * within 15 minutes of the user logging in.
     * <p>
     * Lambda used for closing the dialog window helps reduce the number of lines and
     * complexity of the code.
     * </p>
     *
     * @param currentUser  the current user logged in
     * @param primaryStage the primary stage of the application
     */
    public void appointmentNotification(String currentUser, Stage primaryStage) {
        try {
            //Query the database for the customer
            String query = "SELECT start, MIN(Hour(Start)), Minute(start) FROM appointments \n" +
                    "LEFT JOIN users on appointments.User_ID = users.User_ID WHERE DATE(start) = CURDATE() AND users.User_Name = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, currentUser);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String startTime = convertTimeToLocal(rs.getString("start"));

                //Format times into a localdatetime object for comparing
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime start = LocalDateTime.parse(startTime, formatter);
                LocalDateTime current = LocalDateTime.now();

                if (current.plusMinutes(15).isAfter(start) || current.minusMinutes(30).isAfter(start)) {
                    final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(primaryStage);
                    VBox dialogVbox = new VBox(20);
                    Button okBtn = new Button("Ok");
                    dialogVbox.getChildren().add(new Text("Notice: \nThere is an appointment starting within 15 minutes"));
                    dialogVbox.getChildren().add(okBtn);
                    Scene dialogScene = new Scene(dialogVbox, 300, 100);
                    dialogScene.getStylesheets().add(getClass().getResource("resources/stylesheet.css").toExternalForm());
                    dialogVbox.getStyleClass().add("dialogBox");
                    dialog.setScene(dialogScene);
                    dialog.show();

                    EventHandler<ActionEvent> okEvent = (ActionEvent ee) -> {
                        dialog.close();
                    };
                    okBtn.setOnAction(okEvent);
                }


            }
        } catch (SQLException e) {
            System.out.println("appointmentNotification: " + e);
        }
    }
}


