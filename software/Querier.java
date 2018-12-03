/**
 * @author:  Zak Edwards
 * @version: 1.0 28/11/15
 */

/**
 ********************************************************************************
 * SQL SCHEMA
 ********************************************************************************

   DROP TABLE STUDENT CASCADE CONSTRAINTS;

   CREATE TABLE STUDENT
          (Student_name        VARCHAR(30)        NOT NULL,
           Student_id          CHAR(5)            NOT NULL,
           Course_name         VARCHAR(30)        NOT NULL,
           Year                SMALLINT           NOT NULL,
          PRIMARY KEY (Student_id) );

   DROP TABLE MODULE CASCADE CONSTRAINTS;

   CREATE TABLE MODULE
          (Module_name         VARCHAR(30)        NOT NULL,
           Module_code         VARCHAR(6)         NOT NULL,
           Department_name     VARCHAR(30)        NOT NULL,
          PRIMARY KEY (Module_code) );

   DROP TABLE HISTORY CASCADE CONSTRAINTS;

   CREATE TABLE HISTORY
          (Module_code         VARCHAR(6)         NOT NULL,
           Delivery_year       SMALLINT           NOT NULL,
           Organiser_name      VARCHAR(30)        NOT NULL,
          PRIMARY KEY (Module_code, Delivery_year),                    // No exam runs twice in the same year
          FOREIGN KEY (Module_code) REFERENCES MODULE(Module_code) );  // Ensure all taught modules exist

   DROP TABLE EXAM;

   CREATE TABLE EXAM
          (Student_id          CHAR(5)            NOT NULL,   // Existence contingent on the referenced HISTORY(Module_code, Delivery_year) combination
           Module_code         VARCHAR(6)         NOT NULL,
           Exam_year           SMALLINT           NOT NULL,
           Score               SMALLINT           NOT NULL,
          PRIMARY KEY (Student_id, Module_code),                      // Ensure a module is taken only once
          FOREIGN KEY (Student_id) REFERENCES STUDENT(Student_id),    // Ensure all exams are taken by extant students
          FOREIGN KEY (Module_code, Exam_year) REFERENCES HISTORY(Module_code, Delivery_year),
          CHECK ((Score >= 0) AND (Score <= 100)) );          // Set constraints on available marks

   #
   DROP TABLE PREREQUISITES;

   CREATE TABLE PREREQUISITES
          (Module_code         VARCHAR(6)         NOT NULL,
           Prerequisite_code   VARCHAR(6)         NOT NULL,
          PRIMARY KEY (Module_code, Prerequisite_code),              // Ensure prerequisites occur only once per module
          FOREIGN KEY (Module_code) REFERENCES MODULE(Module_code),  // Prerequisites exist only for extant modules
          FOREIGN KEY (Prerequisite_code) REFERENCES MODULE(Module_code) );  // Prerequisite modules must exist
   #

 */

import java.sql.*;
import java.io.*;

public class Querier {

/**
 ********************************************************************************
 * QUERY STATEMENTS
 ********************************************************************************
 *
 *  - ModulesByStudent:
 *     Prints a single line containing the student's ID, and the module codes the student has taken exams in.
 *     The STUDENT and EXAM tables are joined and ordered alphanumerically by the student's ID (and thus
 *     also by the module codes on each line).
 *
 *  - DepartmentalContributions:
 *     Prints all department names and the number of modules controlled by those departments, provided
 *     they have ran in at least one year.
 *     The MODULE and HISTORY tables are joined and ordered alphabetically; a WHERE clause establishes
 *     the existence of entries in the HISTORY table corresponding to each department, satisfying the
 *     condition of modules running in at least one year.
 *
 *  - ExaminedModules:
 *     Prints the codes of all examined modules, the number of exams taken for each module, and the average
 *     exam score in the year 2000.
 *
 *
 *  - BusyStudents:
 *     Prints the names of those students who have taken the largest number of exams.
 *     The WHERE clause considers two possibilities; the case where all students have taken precisely the
 *     same number of exams, and the more likely case where there is some variance in exam scores. In the
 *     the former case, all students with a score equal to the global average (i.e., all students) are
 *     selected; in the latter case, the former condition remains unsatisfied, and all students with exam
 *     scores strictly above the average are printed.
 *
 *  - QuietYears:
 *     Prints all years in which precisely one exam took place.
 *     The WHERE clause guarantees that only those exam years corresponding to modules with a singly entry
 *     in the HISTORY table are returned.
 *
 *  - IndependentModules:
 *     Prints the codes of all modules that do not have any direct prerequisites.
 *     A module code is selected on the simple condition that a corresponding code does not appear in the
 *     initial column of the PREREQUISITE table.
 *
 *  - DemandingModules:
 *     Prints the codes of all modules with the largest number of direct prerequisites.
 *     Unlike the 'BusyStudents' query, values greater than OR equal to the average value (here, the 
 *     average number of prerequisites for a module) are returned under the same condition. Thus, if there
 *     exists variance in the number of prerequisites for any existing modules, those modules with a perfectly
 *     average number of prerequisites will be selected. The average is determined by dividing the count of
 *     all entries in the PREREQUISITE table by the number of occurences of distinct module codes.
 *
 *  - GeneralPrerequisites:
 *     Prints each module code along with the number of historical prerequisites corresponding to that module.
 *     The subquery factor HIERARCHY obtains the historical dependencies from PREREQUISITES.
 *
 */

    static final String

        ModulesByStudent = "SELECT    STUDENT.Student_id, EXAM.Module_code "
                         + "FROM     (STUDENT LEFT JOIN EXAM ON STUDENT.Student_id = EXAM.Student_id) "
                         + "ORDER BY  STUDENT.Student_id ASC",

        DepartmentalContributions = "SELECT    MODULE.Department_name, COUNT(HISTORY.Module_code) "
                                  + "FROM     (MODULE LEFT JOIN HISTORY ON MODULE.Module_code = HISTORY.Module_code) "
                                  + "WHERE     EXISTS (SELECT    * "
                                  +                   "FROM      HISTORY H "
                                  +                   "WHERE     H.Module_code = MODULE.Module_code) "
                                  + "ORDER BY  MODULE.Department_name",

        ExaminedModules  = "SELECT    M.Module_code, COUNT(E.Module_code), AVG(E.Score) "
                         + "FROM      MODULE M, EXAM E "
                         + "WHERE     EXISTS (SELECT    * "
                         +                   "FROM      EXAM "
                         +                   "WHERE     Module_code = M.Module_code) "
                         +           "AND E.Module_code = M.Module_code "
                         +           "AND E.Year = 2000 "     // Score needs to be in year 2000 
                         + "ORDER BY  M.Module_code",

        BusyStudents = "SELECT    S.Student_name "
                     + "FROM      STUDENT S "
                     + "WHERE    (SELECT    AVG(Score) "
                     +           "FROM      EXAM "
                     +           "WHERE     NOT EXISTS (SELECT    * "
                     +                                 "FROM      EXAM E "
                     +                                 "WHERE     E.Score <> AVG(Score))) "
                     +                           "= (SELECT    E.Score "
                     +                              "FROM      EXAM E "
                     +                              "WHERE     E.Student_id = S.Student_id) "
                     +           "OR "
                     +          "(SELECT    AVG(Score) "
                     +           "FROM      EXAM) < (SELECT    E.Score "
                     +                              "FROM      EXAM E "
                     +                              "WHERE     E.Student_id = S.Student_id) "
                     + "ORDER BY  S.Student_name",

        QuietYears   = "SELECT    E.Exam_year "
                     + "FROM      EXAM E "
                     + "WHERE    (SELECT    COUNT(*) "
                     +           "FROM      HISTORY H "
                     +           "WHERE     H.Module_code = E.Module_code) = 1 "
                     + "ORDER BY  E.Exam_year ASC",

        IndependentModules   = "SELECT    M.Module_code "
                             + "FROM      MODULE M "
                             + "WHERE     NOT EXISTS (SELECT    * "
                             +                       "FROM      PREREQUISITES P "
                             +                       "WHERE     P.Module_code = M.Module_code) "
                             + "ORDER BY  M.Module_code",

        /*
        DemandingModules     = "SELECT    M.Module_code"
                             + "FROM      MODULE M"
                             + "WHERE    (SELECT    COUNT * AS CNT"
                             +           "FROM      PREREQUISITES P"
                             +           "GROUP BY  P.Module_code"
                             +           "HAVING    CNT > (SELECT    COUNT *"
                             +                            "FROM      PREREQUISITES) /"
                             +                           "(SELECT    COUNT(DISTINCT Module_code)"
                             +                            "FROM      PREREQUISITES))"
                             +                         "< (SELECT    COUNT *"
                             +                            "FROM      PREREQUISITES"
                             +                            "WHERE     Module_code = M.Module_code)"
                             + "ORDER BY  M.Module_code",
        */

        DemandingModules     = "SELECT    M.Module_code "
                             + "FROM      MODULE M "
                             + "WHERE    ((SELECT    COUNT(*) "
                             +            "FROM      PREREQUISITES) / "
                             +           "(SELECT    COUNT(DISTINCT Module_code) "
                             +            "FROM      PREREQUISITES)) <= (SELECT    COUNT(*) "
                             +                                          "FROM      PREREQUISITES P "
                             +                                          "WHERE     P.Module_code = M.Module_code) "
                             + "ORDER BY  M.Module_code",

        /*
        GeneralPrerequisites = "SELECT    DISTINCT Module_code "
                             + "FROM      PREREQUISITES "
                             + "WHERE     CONNECT_BY_ISCYCLE = 1 "
                             + "CONNECT BY NOCYCLE "
                             +           "Module_code = PRIOR Prerequisite_code "
                             + "ORDER BY  Module_code";
        */

        GeneralPrerequisites = "WITH      HIERARCHY" 
                             + "AS       (SELECT    P.*, CONNECT_BY_ROOT Module_code AS ROOT"
                             +           "FROM      PREREQUISITES P"
                             +           "CONNECT BY Module_code = PRIOR Prerequisite_code)"
                             + "SELECT    M.Module_code, LISTAGG (H.Prerequisite_code) WITHIN GROUP (ORDER BY H.Prerequisite_code)"
                             + "FROM      MODULE M LEFT JOIN HIERARCHY H ON H.ROOT = M.Module_code"
                             + "GROUP BY  M.Module_code"
                             + "ORDER BY  M.Module_code";

/*******************************************************************************/

    static Connection conn;
    static Statement  stmt;

    private static void printMenu() {
        System.out.println(
            "\n  MENU\n"
          + "(1) Modules by student\n"
          + "(2) Departmental contributions\n"
          + "(3) Examined modules\n"
          + "(4) Busy students\n"
          + "(5) Quiet years\n"
          + "(6) Independent modules\n"
          + "(7) Demanding modules\n"
          + "(8) General prerequisites\n"
          + "(0) Quit\n");
    }

   /**
    * printResult: print the requested data by executing the relevant prepared statement.
    * The value of 'colon' is TRUE if a printed colon is desired after the initial string of
    * retrieved data; FALSE otherwise. The integer 'col' specifies the quantity of distinct
    * database columns (each containing qualitatively distinct data).
    */

    public static void printResult(boolean colon, int col, String query) throws SQLException {

        ResultSet r = stmt.executeQuery(query);
        String result;

        while (r.next()) {

            /* Cycle through columns, printing data on a single line */
            for (int i = 1; i <= col; i++) {

                result = r.getString(i);
                System.out.print(result);

                if (colon == true) {          // Print a colon after the initial column if desired
                    System.out.print(": ");
                    colon = false;
                } else {
                    System.out.print(" ");
                }

                System.out.println();         // Print a new line, repeat process until query terminates

            }

        }

        System.out.println();

    }

   /**
    * readEntry, readLine: parse the user's response to printed instructions.
    * Adapted from docs.oracle.com.
    */
    
    private static String readEntry(String input) {
        try {
            StringBuffer buffer = new StringBuffer();
            System.out.println(input);
            System.out.flush();
            int c = System.in.read();
            while ((c != '\n') && (c != -1)) {
                buffer.append((char) c);
                c = System.in.read();
            }
            return buffer.toString().trim();
        } catch (IOException e) {
            return "";
        }
    }

    private static String readLine() {
        InputStreamReader ir = new InputStreamReader(System.in);
        BufferedReader    br = new BufferedReader(ir, 1);
        String line = "";
        try {
            line = br.readLine();
        } catch (IOException e) {
            System.out.println("Error reading input: IOException was thrown.");
            System.exit(1);
        }
        return line;
    }

    public static void main (String args[]) throws SQLException, IOException {

        try { Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
          System.out.println("Driver could not be loaded. " + e);
        }

        String dbacct, passwd;

        dbacct = readEntry("Enter database account: ");
        passwd = readEntry("Enter password: ");


        try { conn = DriverManager.getConnection("jdbc:oracle:thin:daisy.warwick.ac.uk:1521:daisy", dbacct, passwd);
        } catch (SQLException e) {
          System.out.println("Login failed: Invalid username or password.");
        }

        boolean flag = false;

        do {
            printMenu();
            /*
            String read = readEntry("Select an option: ");
            int option = Integer.parseInt(read);
            */
            System.out.print("Select an option: ");
            System.out.flush();
            String option = readLine();
            System.out.println();
            switch (option.charAt(0)) {
                case (1): printResult(true, 2, ModulesByStudent);
                          break;
                case (2): printResult(false, 2, DepartmentalContributions);
                          break;
                case (3): printResult(true, 3, ExaminedModules);
                          break;
                case (4): printResult(false, 1, BusyStudents);
                          break;
                case (5): printResult(false, 1, QuietYears);
                          break;
                case (6): printResult(false, 1, IndependentModules);
                          break;
                case (7): printResult(false, 1, DemandingModules);
                          break;
                case (8): printResult(false, 2, GeneralPrerequisites);
                          break;
                case (0): flag = true;
                          break;
                default:  System.out.println("Please select an option 1-8, or type 0 to quit.");
            }
        } while (!flag);

      stmt.close();
      conn.close();

    }

}