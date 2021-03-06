/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Plane");
				System.out.println("2. Add Pilot");
				System.out.println("3. Add Flight");
				System.out.println("4. Add Technician");
				System.out.println("5. Book Flight");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order");
				System.out.println("8. List total number of repairs per year in ascending order");
				System.out.println("9. Find total number of passengers with a given status");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPlane(esql); break;
					case 2: AddPilot(esql); break;
					case 3: AddFlight(esql); break;
					case 4: AddTechnician(esql); break;
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddPlane(DBproject esql) {//1
		try{
			String query;
			
			System.out.println("Please input a plane make no longer than 32 characters: ");
			String planeMake = in.readLine();
	
			System.out.println("Please input a plane model no longer than 64 characters: ");
			String planeModel = in.readLine();

			System.out.println("Please input an integer for the plane's age: ");
			int planeAge = Integer.parseInt(in.readLine());

			System.out.println("Please input an integer for the number of the plane's seats: ");
			int planeSeats = Integer.parseInt(in.readLine());

			query = String.format("INSERT INTO Plane(id, make, model, age, seats) VALUES ( (nextval('id_seq_plane')), '%s', '%s', '%d','%d');", planeMake, planeModel, planeAge, planeSeats);
			esql.executeUpdate(query);
			esql.executeQueryAndPrintResult("SELECT * FROM Plane;"); 
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	public static void AddPilot(DBproject esql) {//2
		try{
			String query;
	
			System.out.println("Please input the pilot's full name: ");
			String fullName = in.readLine();


			System.out.println("Please input the pilot's nationality: ");
			String national = in.readLine();
			query = String.format("INSERT INTO Pilot(id, fullname, nationality) VALUES ( (nextval('id_seq_pilot')), '%s', '%s');", fullName, national);
			
			esql.executeUpdate(query);
			esql.executeQueryAndPrintResult("SELECT * FROM Pilot;"); 

		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}

	}

	public static void AddFlight(DBproject esql) {//3
		// Given a pilot, plane and flight, adds a flight in the DB
		try{
			System.out.println("Please input the flight cost: ");
			int cost = Integer.parseInt(in.readLine());
			
			System.out.println("Please input the number of stops: ");
			int num_stops = Integer.parseInt(in.readLine());
			
			System.out.println("Please enter the depature date(YYYY-MM-DD): ");
			String dep_date = in.readLine();
			
			System.out.println("Please enter the arrival date(YYYY-MM-DD): ");
			String arrive_date = in.readLine();
			
			System.out.println("Please enter the depature airport code(ABCDE): ");
			String dep_code = in.readLine();
			
			System.out.println("Please enter the arrival airport code(ABCDE): ");
			String arrive_code = in.readLine();


			System.out.println("Please input a Pilot ID: ");
			int pilot_id = Integer.parseInt(in.readLine());

			System.out.println("Please enter a plane ID: ");
			int plane_id = Integer.parseInt(in.readLine());

			System.out.println("Please enter a flight ID: ");
			int flight_id = Integer.parseInt(in.readLine());

			
            String query2 = String.format("INSERT INTO Flight( fnum, cost, num_sold, num_stops, actual_departure_date, actual_arrival_date, arrival_airport, departure_airport) VALUES ( (nextval('id_seq_flight')) ,'%d','0','%d', '%s', '%s', '%s', '%s');", cost, num_stops, dep_date, arrive_date, dep_code, arrive_code);

                        esql.executeUpdate(query2);


			String query = String.format("INSERT INTO FlightInfo(fiid, flight_id, pilot_id, plane_id) VALUES ( (nextval('id_seq_finfo')), '%d', '%d', '%d');", flight_id, pilot_id, plane_id);
			esql.executeUpdate(query);
			esql.executeQueryAndPrintResult("SELECT * FROM FlightInfo;"); 
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	public static void AddTechnician(DBproject esql) {//4
		try{
		String query;

		System.out.println("Please input the technician's full name: ");
		String full_Name = in.readLine(); //fullName is defined in addPilot, so full_Name used here
		

		query = String.format("INSERT INTO Technician (id, full_name) VALUES ( (nextval('id_seq_pilot')) , ('%s'));", full_Name);
		esql.executeUpdate(query);
		esql.executeQueryAndPrintResult("SELECT * FROM Technician;"); 

		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		try{
			String query;
			System.out.println("Please input a customer ID: ");
			int customerID = Integer.parseInt(in.readLine());
				
			System.out.println("Please input a flight number: ");
			int flightNum = Integer.parseInt(in.readLine());
		
			//Get the number of seats sold
			query = String.format("SELECT F.num_sold FROM Flight F WHERE F.fnum = %d LIMIT 1 ", flightNum);
			int sold_seats  = Integer.parseInt(esql.executeQueryAndReturnResult(query).get(0).get(0));
			//Get the total number of seats on plane
			query = String.format("Select P.seats FROM Plane P, Flightinfo FI  WHERE P.id = FI.plane_id AND FI.flight_id = %d LIMIT 1", flightNum);
			int total_seats = Integer.parseInt(esql.executeQueryAndReturnResult(query).get(0).get(0));
	
			if(total_seats - sold_seats > 0)
			{
				query = String.format("INSERT INTO Reservation(rnum, cid, fid, status) VALUES ((nextval('id_seq_reservation')) , %d, %d, 'R');", customerID, flightNum);

			}
			else
			{
				query = String.format("INSERT INTO Reservation(rnum, cid, fid, status) VALUES ((nextval('id_seq_reservation')) , %d, %d, 'W');", customerID, flightNum);
			}
			esql.executeUpdate(query);

                        query = String.format("SELECT F.num_sold  FROM Flight F WHERE fnum = %d", flightNum);
                        esql.executeQueryAndPrintResult(query);


			query = String.format("UPDATE Flight SET num_sold = num_sold + 1 WHERE fnum = %d", flightNum);
			esql.executeUpdate(query);

			query = String.format("SELECT F.num_sold  FROM Flight  F WHERE fnum = %d", flightNum);
			esql.executeQueryAndPrintResult(query);


			System.out.println("Your new reservation: ");
			query = String.format("SELECT * FROM Reservation WHERE cid = %d AND fid = %d", customerID, flightNum);
			esql.executeQueryAndPrintResult(query); 
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}

	}

	public static void ListNumberOfAvailableSeats(DBproject esql) {//6
		// For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
		try{
			String query;
			System.out.println("Please input a flight number: ");
			int flight_number = Integer.parseInt(in.readLine());
			query = String.format("SELECT F.num_sold FROM Flight F WHERE F.fnum = %d", flight_number);
			int sold_seats = Integer.parseInt(esql.executeQueryAndReturnResult(query).get(0).get(0));
			
			query = String.format("SELECT P.seats FROM Plane P, Flightinfo FI  WHERE P.id = FI.plane_id AND FI.flight_id = %d", flight_number);
			int numberOfSeats = Integer.parseInt(esql.executeQueryAndReturnResult(query).get(0).get(0));
			
			numberOfSeats = numberOfSeats - sold_seats;
			String output;
			if(numberOfSeats > 0)
				output = String.format("There are %d seats on flight %d\n", numberOfSeats, flight_number);
			else
				 output = String.format("All seats on flight %d are sold\n", flight_number);
		
			System.out.println(output);
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
		// Count number of repairs per planes and list them in descending order
		try{
			String query = "SELECT P.id, COUNT(R.rid) FROM Plane P, Repairs R WHERE P.id = R.plane_id GROUP BY P.id ORDER BY COUNT DESC";
			esql.executeQueryAndPrintResult(query);
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		// Count repairs per year and list them in ascending order
		try{
			String query = "SELECT EXTRACT(YEAR FROM R.repair_date) as \"repairYear\", COUNT(R.rid) FROM Repairs R GROUP BY \"repairYear\" ORDER BY COUNT ASC";
			esql.executeQueryAndPrintResult(query);
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		try{
		String query;
		System.out.println("Please input the passenger status code (W, C, or R): ");
		String inputs = in.readLine();
		query = String.format("SELECT COUNT(R.status) FROM Reservation R, Customer C WHERE (R.status = '%s' AND R.cid = C.id);", inputs);
		//query = "SELECT COUNT(R.status) FROM Reservation R, Customer C WHERE (R.cid = C.id AND R.status = " + inputs + " \" );";
		esql.executeQueryAndPrintResult(query);
		}
		catch(Exception e){
		System.err.println(e.getMessage());
		}
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
	}
}
