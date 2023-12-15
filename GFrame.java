import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

//class to make GUI and elements
public class GFrame extends JFrame implements ActionListener, MouseListener, KeyListener
{
    private LoginHandler lhandler;
    private JPanel loginPanel = new JPanel();                                       //creates a JPanel that will go in the loginPanel of the window
    private JPanel searchPanel = new JPanel();
    private JPanel specifyPanel = new JPanel();

    private JLabel idLabel = new JLabel("User ID");
    private JLabel pwdLabel = new JLabel("Password");

    private JTextField idField = new JTextField(10);
    private JPasswordField pwdField = new JPasswordField(10);
    private JTextField searchField = new JTextField(25);                 //initializes text field and gives it 50 characters to use

    private JButton searchButton = new JButton("Submit");                       //Button for user to do a query
    private JButton loginButton = new JButton("Login");                     //button to loginButton user text
    private JButton logoutButton = new JButton("Logout");                   //button to log out

    private JTable table;
    private JScrollPane view;                                                       //creates scrollpane for table

    private Connection connection;                                          //used to set up connection to SQL database
   
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem[] items = new JMenuItem[6];

    private JMenu transactionMenu;
    private JMenuItem[] transactionItems = new JMenuItem[7];

    private boolean isAdmin = false;    //new
    private boolean isMember = false;   //new
    private boolean loggedIn = false;   //new
    private boolean isConnected = false;

    private int queryIndex = -1;

    private JCheckBox awardCheckBox = new JCheckBox("Awards");
    private JCheckBox notStreamedCheckBox = new JCheckBox("Not Streamed");
    private JCheckBox actorCheckBox = new JCheckBox("Actor");
    private JCheckBox directorCheckBox = new JCheckBox("Director");
    private JCheckBox genreCheckBox = new JCheckBox("Genre");

    public GFrame()
    {
        setLayout(new FlowLayout());

        //create GUI components for login
        loginPanel.setLayout(new GridLayout(2,2,0,5));
        loginPanel.addKeyListener(this);
        loginPanel.add(idLabel);
        loginPanel.add(idField);
        idField.addKeyListener(this);
        loginPanel.add(pwdLabel);
        loginPanel.add(pwdField);
        pwdField.addKeyListener(this);

        add(loginPanel);
        add(loginButton);

        lhandler = new LoginHandler();
        loginButton.setActionCommand("LOG");
        loginButton.addActionListener(lhandler);

        //create GUI components for query
        searchButton.setEnabled(false);
        searchField.setEditable(false);
        searchField.addKeyListener(this);
        searchButton.setActionCommand("Search");
        searchButton.addActionListener(this);
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel);
        searchPanel.setVisible(false);

        // GUI components for searching
        awardCheckBox.setActionCommand("Award");
        awardCheckBox.addActionListener(this);
        notStreamedCheckBox.setActionCommand("Not Streamed");
        notStreamedCheckBox.addActionListener(this);
        actorCheckBox.setActionCommand("Actor");
        actorCheckBox.addActionListener(this);
        directorCheckBox.setActionCommand("Director");
        directorCheckBox.addActionListener(this);
        genreCheckBox.setActionCommand("Genre");
        genreCheckBox.addActionListener(this);
        specifyPanel.add(awardCheckBox);
        specifyPanel.add(notStreamedCheckBox);
        specifyPanel.add(actorCheckBox);
        specifyPanel.add(directorCheckBox);
        specifyPanel.add(genreCheckBox);
        add(specifyPanel);
        specifyPanel.setVisible(false);

        WindowHandler window = new WindowHandler();
        this.addWindowListener(window);

        setJMenuBar(setupJMenuBar());

        setUpMainFrame();                                                   //initializes window
    }

    //function to set up the window and put it on the screen
    public void setUpMainFrame()
    {
        //tools to get screen size
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = (tk.getScreenSize());

        //sets window size to be half width and height in queryPanel of screen
        setSize(d.width/2, d.height/2);
        setLocation(d.width/4, d.height/4);

        //sets the title of the window
        setTitle("Scuffed Flicks");

        //makes it so when the window is closed the program terminates
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //makes sure it is visible
        setVisible(true);
        setResizable(false);
    }

    JMenuBar setupJMenuBar()
    {
        menuBar = new JMenuBar();
        
        menu = new JMenu("Explore");
        menu.add(setupMenuItem("Browse", 0));
        menu.add(setupMenuItem("Sequels", 1));
        menu.add(setupMenuItem("Streaming", 2));
        menu.add(setupMenuItem("Members Videos", 3));
        menu.add(setupMenuItem("Trend", 4));
        menu.add(setupMenuItem("Top 10", 5));
        menuBar.add(menu);

        transactionMenu = new JMenu("Transactions");
        transactionMenu.add(setupTransactionMenuItem("Edit Profile", 0));
        transactionMenu.add(setupTransactionMenuItem("Add Member", 1));
        transactionMenu.add(setupTransactionMenuItem("Remove Member", 2));
        transactionMenu.add(setupTransactionMenuItem("Add Movie", 3));
        transactionMenu.add(setupTransactionMenuItem("Add Series", 4));
        transactionMenu.add(setupTransactionMenuItem("Remove Movie", 5));
        transactionMenu.add(setupTransactionMenuItem("Remove Series", 6));
        menuBar.add(transactionMenu);
        logoutButton.setActionCommand("LOUT");
        logoutButton.addActionListener(this);
        menuBar.add(logoutButton);


        menuBar.setVisible(false);
        
        return menuBar;
    }

    // helper method for setupJMenuBar
    JMenuItem setupMenuItem(String label, int index)
    {       
        items[index] = new JMenuItem(label);
        items[index].setActionCommand(label);
        items[index].addActionListener(this);
        
        return items[index];
    }

    // helper method for setupJMenuBar
    JMenuItem setupTransactionMenuItem(String label, int index)
    {       
        transactionItems[index] = new JMenuItem(label);
        transactionItems[index].setActionCommand(label);
        transactionItems[index].addActionListener(this);
        
        return transactionItems[index];
    }


    // method called by this method performed
    // handles any gui component changes and uses query handler to execute the sql queries
    void search(int index)
    {
        // this is general browsing of media
        // if else is to check for combinations of check boxes being selected
        if(index == 0)
        {
            queryIndex = 0;
            searchPanel.setVisible(true);
            searchButton.setEnabled(true);
            searchField.setEditable(true);
            specifyPanel.setVisible(true);
            if(view != null)
            {
                view.setVisible(false);
            }
            QueryHandler qhandler = new QueryHandler();
            String s = searchField.getText();
            if(!s.equals("") && !actorCheckBox.isSelected() && !directorCheckBox.isSelected() && !genreCheckBox.isSelected()) 
            {
                qhandler.getQueryTable("SELECT DISTINCT m.title AS Title, m.IMDB, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE c.cast_name LIKE '" + s + "%' OR d.director_name LIKE '" + s + "%' OR m.genre LIKE '" + s + "%' OR m.title LIKE '" + s + "%'");
                searchField.setText("");
            }
            else if(actorCheckBox.isSelected() && !awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                qhandler.getQueryTable("SELECT c.cast_name AS Actor, m.title AS Title, m.IMDB, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE c.cast_name LIKE '" + s + "%'");
            }
            else if(directorCheckBox.isSelected() && !awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE d.director_name LIKE '" + s + "%'");
            }
            else if(genreCheckBox.isSelected() && !awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                qhandler.getQueryTable("SELECT title AS Title, IMDB, genre AS Genre, release_date AS 'Release Date' FROM Group3.media WHERE genre LIKE '" + s + "%'");
            }
            else if(actorCheckBox.isSelected() && awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards, c.cast_name AS Actor, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE mv.awards != '' AND c.cast_name LIKE '" + s + "%'");
            }
            else if(actorCheckBox.isSelected() && notStreamedCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, c.cast_name AS Actor, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND c.cast_name LIKE '" + s + "%'");
            }
            else if(directorCheckBox.isSelected() && awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE mv.awards != '' AND d.director_name LIKE '" + s + "%'");
            }
            else if(directorCheckBox.isSelected() && notStreamedCheckBox.isSelected() && !actorCheckBox.isSelected())
            {
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND d.director_name LIKE '" + s + "%'");
            }
            else if(genreCheckBox.isSelected() && awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards, genre AS Genre, release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.movies mv WHERE mv.awards != '' AND genre LIKE '" + s + "%'");
            }
            else if(genreCheckBox.isSelected() && notStreamedCheckBox.isSelected() && !actorCheckBox.isSelected())
            {
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, genre AS Genre, release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND genre LIKE '" + s + "%'");
            }
            else if(actorCheckBox.isSelected() && awardCheckBox.isSelected() && notStreamedCheckBox.isSelected())
            {
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards, c.cast_name AS Actor, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != '' AND c.cast_name LIKE '" + s + "%'");
            }
            else if(directorCheckBox.isSelected() && awardCheckBox.isSelected() && notStreamedCheckBox.isSelected())
            {
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != '' AND d.director_name LIKE '" + s + "%'");
            }
            else if(genreCheckBox.isSelected() && awardCheckBox.isSelected() && notStreamedCheckBox.isSelected())
            {
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards, genre AS Genre, release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != '' AND genre LIKE '" + s + "%'");
            }
        }

        // browsing media by sequels
        else if(index == 1) 
        {
            queryIndex = 1;
            // set search function available to users
            searchPanel.setVisible(true);
            searchButton.setEnabled(true);
            searchField.setEditable(true);
            specifyPanel.setVisible(false);
            if(view != null)
            {
                view.setVisible(false);
            }
            if(!searchField.getText().equals(""))
            {
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT IMDB, sequel AS Sequel, m.release_date AS 'Release Date' FROM Group3.movie_sequel NATURAL JOIN Group3.media m WHERE movie_sequel.prequel LIKE \"" + searchField.getText() + "%\"");
            }
            else
            {
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT IMDB, sequel AS Sequel, m.release_date AS 'Release Date' FROM Group3.movie_sequel NATURAL JOIN Group3.media m");   
            }
            searchField.setText("");
        }

        // viewing users streaming history
        else if(index == 2)
        {
            queryIndex = 2;
            // set search function unavailable to users
            searchPanel.setVisible(true);
            searchButton.setEnabled(false);
            searchField.setEditable(false);
            specifyPanel.setVisible(false);
            if(view != null)
            {
                view.setVisible(false);
            }
            QueryHandler qhandler = new QueryHandler();
            qhandler.getQueryTable("SELECT date_format(timestamp, '%m/%d/%Y') AS 'Streamed On', username AS 'User', m.title AS Title, m.IMDB, m.release_date AS 'Release Date' FROM Group3.streams s NATURAL JOIN Group3.media m NATURAL JOIN Group3.timestamp WHERE username = '" + lhandler.id + "'");
        }

        // view members who have streamed a particular video
        else if(index == 3)
        {
            queryIndex = 3;
            // set search function available to users
            searchPanel.setVisible(true);
            searchButton.setEnabled(true);
            searchField.setEditable(true);
            specifyPanel.setVisible(false);
            if(view != null)
            {
                view.setVisible(false);
            }
            if(!searchField.getText().equals("")) 
            {
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT date_format(timestamp, '%m/%d/%Y') AS 'Streamed On', username AS 'User' , title AS Viewed, IMDB FROM Group3.streams NATURAL JOIN Group3.media NATURAL JOIN Group3.timestamp WHERE title LIKE '" + searchField.getText() + "%'");
            }
            searchField.setText("");
        }

        // streaming trend in last 24 hours
        else if(index == 4)
        {
            queryIndex = 4;
            // set search function unavailable to users
            searchPanel.setVisible(true);
            searchButton.setEnabled(false);
            searchField.setEditable(false);
            specifyPanel.setVisible(false);
            if(view != null)
            {
                view.setVisible(false);
            }
            QueryHandler qhandler = new QueryHandler();   
            qhandler.getQueryTable("SELECT m.IMDB, m.title, COUNT(*) AS Streamed FROM Group3.streams AS s NATURAL JOIN Group3.timestamp AS t NATURAL JOIN Group3.media AS m WHERE t.timestamp >= CURRENT_TIMESTAMP - INTERVAL 24 HOUR GROUP BY m.IMDB, m.title");
        }

        // top 10 movies/series based on streaming history of last month
        else if(index == 5)
        {
            queryIndex = 5;
            // set search function unavailable to users
            searchPanel.setVisible(true);
            searchButton.setEnabled(false);
            searchField.setEditable(false);
            specifyPanel.setVisible(false);
            if(view != null)
            {
                view.setVisible(false);
            }
            QueryHandler qhandler = new QueryHandler();
            qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, COUNT(*) AS Streams FROM Group3.media m NATURAL JOIN Group3.streams s NATURAL JOIN Group3.timestamp t WHERE t.timestamp >= CURRENT_TIMESTAMP - INTERVAL 1 MONTH GROUP BY m.title, m.IMDB ORDER BY Streams DESC LIMIT 10");
        }

        // remove member 
        // has a popup to confirm delete
        else if(index == 6)
        {
            // get member info from database to confirm delete
            try 
            {
                // build a string of info from db
                String message = "";
                Statement stmn;
                ResultSet resultSet;

                // get username and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT username FROM Group3.member WHERE username = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "Username: " + resultSet.getObject(1).toString() + "\n";

                // get member id and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT member_id FROM Group3.member WHERE username = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "Member Id: " + resultSet.getObject(1).toString() + "\n";

                // get plan name and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT plan_name FROM Group3.member WHERE username = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "Plan Name: " + resultSet.getObject(1).toString() + "\n";

                int option = JOptionPane.showConfirmDialog(this, message, "Delete?", JOptionPane.YES_NO_OPTION);
        
                if (option == JOptionPane.YES_OPTION) 
                {
                    // remove member from database
                    // member has to be removed from both tables due to pk
                    stmn = connection.createStatement();
                    stmn.executeUpdate("DELETE FROM Group3.member WHERE username = '" + searchField.getText() + "'");
                    stmn = connection.createStatement();
                    stmn.executeUpdate("DELETE FROM Group3.user WHERE username = '" + searchField.getText() + "'");
                }
            }
            catch(SQLException sqle) 
            {
                 JOptionPane.showMessageDialog(null, sqle.getMessage(), "Delete error!", JOptionPane.ERROR_MESSAGE);
            }
            searchField.setText("");
        }

        // remove movie
        // has a popup to confirm delete
        else if(index == 7)
        {
            // get movie info from database to confirm delete
            try 
            {
                // build a string of info from db
                String message = "";
                Statement stmn;
                ResultSet resultSet;

                // get IMDB and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT IMDB FROM Group3.media WHERE IMDB = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "IMDB: " + resultSet.getObject(1).toString() + "\n";

                // get title and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT title FROM Group3.media WHERE IMDB = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "Title: " + resultSet.getObject(1).toString() + "\n";

                // get release date and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT release_date FROM Group3.media WHERE IMDB = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "Release Date: " + resultSet.getObject(1).toString() + "\n";

                // get genre and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT genre FROM Group3.media WHERE IMDB = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "Genre: " + resultSet.getObject(1).toString() + "\n";

                // get awards and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT awards FROM Group3.movies WHERE IMDB = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "Awards: " + resultSet.getObject(1).toString() + "\n";

                int option = JOptionPane.showConfirmDialog(this, message, "Delete?", JOptionPane.YES_NO_OPTION);
        
                if (option == JOptionPane.YES_OPTION) 
                {
                    // remove movie from database
                    // movie has to be removed from both tables due to pk
                    stmn = connection.createStatement();
                    stmn.executeUpdate("DELETE FROM Group3.movies WHERE IMDB = '" + searchField.getText() + "'");
                    stmn = connection.createStatement();
                    stmn.executeUpdate("DELETE FROM Group3.media WHERE IMDB = '" + searchField.getText() + "'");
                }
            }
            catch(SQLException sqle) 
            {
                 JOptionPane.showMessageDialog(null, sqle.getMessage(), "Delete error!", JOptionPane.ERROR_MESSAGE);
            }
            searchField.setText("");
        }

        // remove series
        // has a popup to confirm delete
        else if(index == 8)
        {
            // get series info from database to confirm delete
            try 
            {
                // build a string of info from db
                String message = "";
                Statement stmn;
                ResultSet resultSet;

                // get IMDB and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT IMDB FROM Group3.media WHERE IMDB = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "IMDB: " + resultSet.getObject(1).toString() + "\n";

                // get title and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT title FROM Group3.media WHERE IMDB = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "Title: " + resultSet.getObject(1).toString() + "\n";

                // get release date and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT release_date FROM Group3.media WHERE IMDB = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "Release Date: " + resultSet.getObject(1).toString() + "\n";

                // get genre and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT genre FROM Group3.media WHERE IMDB = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "Genre: " + resultSet.getObject(1).toString() + "\n";

                // get season and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT season FROM Group3.series WHERE IMDB = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "Season: " + resultSet.getObject(1).toString() + "\n";

                // get episode and add to popup string
                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT episode FROM Group3.series WHERE IMDB = '" + searchField.getText() + "'");
                resultSet.next();
                message = message + "Episode: " + resultSet.getObject(1).toString() + "\n";

                int option = JOptionPane.showConfirmDialog(this, message, "Delete?", JOptionPane.YES_NO_OPTION);
        
                if (option == JOptionPane.YES_OPTION) 
                {
                    // remove series from database
                    // series has to be removed from both tables due to pk
                    stmn = connection.createStatement();
                    stmn.executeUpdate("DELETE FROM Group3.series WHERE IMDB = '" + searchField.getText() + "'");
                    stmn = connection.createStatement();
                    stmn.executeUpdate("DELETE FROM Group3.media WHERE IMDB = '" + searchField.getText() + "'");
                }
            }
            catch(SQLException sqle) 
            {
                 JOptionPane.showMessageDialog(null, sqle.getMessage(), "Delete error!", JOptionPane.ERROR_MESSAGE);
            }
            searchField.setText("");
        }
    }

    
    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals("Search"))
        {
            search(queryIndex);
        }

        // handles browsing by award
        else if(e.getActionCommand().equals("Award"))
        {
            if(awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected() && !actorCheckBox.isSelected() && !directorCheckBox.isSelected() && !genreCheckBox.isSelected())
            {
                searchField.setEditable(false);
                searchButton.setEnabled(false);
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards FROM Group3.media m NATURAL JOIN Group3.movies mv WHERE mv.awards != ''");
            }
            else if(!awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected() && !actorCheckBox.isSelected() && !directorCheckBox.isSelected() && !genreCheckBox.isSelected())
            {
                if(view != null)
                {
                    view.setVisible(false);
                }
                searchField.setText("");
                searchField.setEditable(true);
                searchButton.setEnabled(true);
            }
            else if(awardCheckBox.isSelected() && notStreamedCheckBox.isSelected() && !actorCheckBox.isSelected() && !directorCheckBox.isSelected() && !genreCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != ''");
            }
            else if(notStreamedCheckBox.isSelected() && !awardCheckBox.isSelected() && !actorCheckBox.isSelected() && !directorCheckBox.isSelected() && !genreCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title FROM Group3.media m NATURAL JOIN Group3.user WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "')");
            }
            else if(notStreamedCheckBox.isSelected() && actorCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, c.cast_name AS Actor, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND c.cast_name LIKE '" + s + "%'");
            }
            else if(notStreamedCheckBox.isSelected() && directorCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND d.director_name LIKE '" + s + "%'");
            }
            else if(notStreamedCheckBox.isSelected() && genreCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, genre AS Genre, release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND genre LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && actorCheckBox.isSelected() && notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards, c.cast_name AS Actor, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != '' AND c.cast_name LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && directorCheckBox.isSelected() && notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != '' AND d.director_name LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && genreCheckBox.isSelected() && notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards, genre AS Genre, release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != '' AND genre LIKE '" + s + "%'");
            }
            else if(actorCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT c.cast_name AS Actor, m.title AS Title, m.IMDB, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE c.cast_name LIKE '" + s + "%'");
            }
            else if(actorCheckBox.isSelected() && awardCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards, c.cast_name AS Actor, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE mv.awards != '' AND c.cast_name LIKE '" + s + "%'");
            }
            else if(directorCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE d.director_name LIKE '" + s + "%'");
            }
            else if(directorCheckBox.isSelected() && awardCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE mv.awards != '' AND d.director_name LIKE '" + s + "%'");
            }
            else if(genreCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT title AS Title, IMDB, genre AS Genre, release_date AS 'Release Date' FROM Group3.media WHERE genre LIKE '" + s + "%'");
            }
            else if(genreCheckBox.isSelected() && awardCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards, genre AS Genre, release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.movies mv WHERE mv.awards != '' AND genre LIKE '" + s + "%'");
            }
        }

        // handles browsing by not streamed
        else if(e.getActionCommand().equals("Not Streamed"))
        {
            if(notStreamedCheckBox.isSelected() && !awardCheckBox.isSelected() && !actorCheckBox.isSelected() && !directorCheckBox.isSelected() && !genreCheckBox.isSelected())
            {
                searchField.setEditable(false);
                searchButton.setEnabled(false);
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title FROM Group3.media m NATURAL JOIN Group3.user WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "')");
            }
            else if(!notStreamedCheckBox.isSelected() && !awardCheckBox.isSelected() && !actorCheckBox.isSelected() && !directorCheckBox.isSelected() && !genreCheckBox.isSelected())
            {
                if(view != null)
                {
                    view.setVisible(false);
                }
                searchField.setText("");
                searchField.setEditable(true);
                searchButton.setEnabled(true);
            }
            else if(awardCheckBox.isSelected() && notStreamedCheckBox.isSelected() && !actorCheckBox.isSelected() && !directorCheckBox.isSelected() && !genreCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != ''");
            }
            else if(awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected() && !actorCheckBox.isSelected() && !directorCheckBox.isSelected() && !genreCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards FROM Group3.media m NATURAL JOIN Group3.movies mv WHERE mv.awards != ''");
            }
            else if(awardCheckBox.isSelected() && actorCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards, c.cast_name AS Actor, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE mv.awards != '' AND c.cast_name LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && directorCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE mv.awards != '' AND d.director_name LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && genreCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards, genre AS Genre, release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.movies mv WHERE mv.awards != '' AND genre LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && actorCheckBox.isSelected() && notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards, c.cast_name AS Actor, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != '' AND c.cast_name LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && directorCheckBox.isSelected() && notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != '' AND d.director_name LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && genreCheckBox.isSelected() && notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards, genre AS Genre, release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != '' AND genre LIKE '" + s + "%'");
            }
            else if(actorCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT c.cast_name AS Actor, m.title AS Title, m.IMDB, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE c.cast_name LIKE '" + s + "%'");
            }
            else if(actorCheckBox.isSelected() && notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, c.cast_name AS Actor, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND c.cast_name LIKE '" + s + "%'");
            }
            else if(directorCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE d.director_name LIKE '" + s + "%'");
            }
            else if(directorCheckBox.isSelected() && notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND d.director_name LIKE '" + s + "%'");
            }
            else if(genreCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT title AS Title, IMDB, genre AS Genre, release_date AS 'Release Date' FROM Group3.media WHERE genre LIKE '" + s + "%'");
            }
            else if(genreCheckBox.isSelected() && notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, genre AS Genre, release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND genre LIKE '" + s + "%'");
            }
        }

        // handles browsing by actor
        else if(e.getActionCommand().equals("Actor"))
        {
            if(actorCheckBox.isSelected() && !awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT c.cast_name AS Actor, m.title AS Title, m.IMDB, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE c.cast_name LIKE '" + s + "%'");
            }
            else if(!awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                if(view != null)
                {
                    view.setVisible(false);
                }
                searchField.setText("");
            }
            else if(awardCheckBox.isSelected() && !actorCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                searchField.setEditable(false);
                searchButton.setEnabled(false);
                searchField.setText("");
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards FROM Group3.media m NATURAL JOIN Group3.movies mv WHERE mv.awards != ''");
            }
            else if(awardCheckBox.isSelected() && actorCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                searchField.setEditable(true);
                searchButton.setEnabled(true);
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards, c.cast_name AS Actor, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE mv.awards != '' AND c.cast_name LIKE '" + s + "%'");
            }
            else if(notStreamedCheckBox.isSelected() && !actorCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                searchField.setEditable(false);
                searchButton.setEnabled(false);
                searchField.setText("");
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title FROM Group3.media m NATURAL JOIN Group3.user WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "')");
            }
            else if(notStreamedCheckBox.isSelected() && actorCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                searchField.setEditable(true);
                searchButton.setEnabled(true);
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, c.cast_name AS Actor, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND c.cast_name LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && notStreamedCheckBox.isSelected() && actorCheckBox.isSelected())
            {
                searchField.setEditable(true);
                searchButton.setEnabled(true);
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards, c.cast_name AS Actor, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_cast mc NATURAL JOIN Group3.cast_ c WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != '' AND c.cast_name LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && notStreamedCheckBox.isSelected() && !actorCheckBox.isSelected())
            {
                searchField.setEditable(false);
                searchButton.setEnabled(false);
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != ''");
            }
            directorCheckBox.setSelected(false);
            genreCheckBox.setSelected(false);
        }

        // handles browsing by director
        else if(e.getActionCommand().equals("Director"))
        {
            if(directorCheckBox.isSelected() && !awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE d.director_name LIKE '" + s + "%'");
            }
            else if(!awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                if(view != null)
                {
                    view.setVisible(false);
                }
                searchField.setText("");
            }
            else if(awardCheckBox.isSelected() && !directorCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                searchField.setEditable(false);
                searchButton.setEnabled(false);
                searchField.setText("");
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards FROM Group3.media m NATURAL JOIN Group3.movies mv WHERE mv.awards != ''");
            }
            else if(awardCheckBox.isSelected() && directorCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                searchField.setEditable(true);
                searchButton.setEnabled(true);
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE mv.awards != '' AND d.director_name LIKE '" + s + "%'");
            }
            else if(notStreamedCheckBox.isSelected() && !directorCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                searchField.setEditable(false);
                searchButton.setEnabled(false);
                searchField.setText("");
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title FROM Group3.media m NATURAL JOIN Group3.user WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "')");
            }
            else if(notStreamedCheckBox.isSelected() && directorCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                searchField.setEditable(true);
                searchButton.setEnabled(true);
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND d.director_name LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && notStreamedCheckBox.isSelected() && directorCheckBox.isSelected())
            {
                searchField.setEditable(true);
                searchButton.setEnabled(true);
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards, d.director_name AS ‘Director’, m.release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv NATURAL JOIN Group3.media_director md NATURAL JOIN Group3.director d WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != '' AND d.director_name LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && notStreamedCheckBox.isSelected() && !directorCheckBox.isSelected())
            {
                searchField.setEditable(false);
                searchButton.setEnabled(false);
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != ''");
            }
            actorCheckBox.setSelected(false);
            genreCheckBox.setSelected(false);
        }

        // handles browsing by genre
        else if(e.getActionCommand().equals("Genre"))
        {
            if(genreCheckBox.isSelected() && !awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT title AS Title, IMDB, genre AS Genre, release_date AS 'Release Date' FROM Group3.media WHERE genre LIKE '" + s + "%'");
            }
            else if(!awardCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                if(view != null)
                {
                    view.setVisible(false);
                }
            }
            else if(awardCheckBox.isSelected() && !genreCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                searchField.setEditable(false);
                searchButton.setEnabled(false);
                searchField.setText("");
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards FROM Group3.media m NATURAL JOIN Group3.movies mv WHERE mv.awards != ''");
            }
            else if(awardCheckBox.isSelected() && genreCheckBox.isSelected() && !notStreamedCheckBox.isSelected())
            {
                searchField.setEditable(true);
                searchButton.setEnabled(true);
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT m.title AS Title, m.IMDB, mv.awards, genre AS Genre, release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.movies mv WHERE mv.awards != '' AND genre LIKE '" + s + "%'");
            }
            else if(notStreamedCheckBox.isSelected() && !genreCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                searchField.setEditable(false);
                searchButton.setEnabled(false);
                searchField.setText("");
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title FROM Group3.media m NATURAL JOIN Group3.user WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "')");
            }
            else if(notStreamedCheckBox.isSelected() && genreCheckBox.isSelected() && !awardCheckBox.isSelected())
            {
                searchField.setEditable(true);
                searchButton.setEnabled(true);
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, genre AS Genre, release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND genre LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && notStreamedCheckBox.isSelected() && genreCheckBox.isSelected())
            {
                searchField.setEditable(true);
                searchButton.setEnabled(true);
                QueryHandler qhandler = new QueryHandler();
                String s = searchField.getText();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards, genre AS Genre, release_date AS 'Release Date' FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != '' AND genre LIKE '" + s + "%'");
            }
            else if(awardCheckBox.isSelected() && notStreamedCheckBox.isSelected() && !genreCheckBox.isSelected())
            {
                searchField.setEditable(false);
                searchButton.setEnabled(false);
                QueryHandler qhandler = new QueryHandler();
                qhandler.getQueryTable("SELECT DISTINCT m.IMDB, m.title AS Title, mv.awards FROM Group3.media m NATURAL JOIN Group3.user NATURAL JOIN Group3.movies mv WHERE m.IMDB NOT IN ( SELECT sm.IMDB FROM Group3.streams sm WHERE sm.username = '" + lhandler.id + "') AND mv.awards != ''");
            }
            actorCheckBox.setSelected(false);
            directorCheckBox.setSelected(false);
        }

        // (member ability)
        // browsing media - search results can be modified by checkboxes
        else if(e.getActionCommand().equals("Browse"))
        {
           search(0);
        }

        // (member ability)
        // list of movie sequels
        else if(e.getActionCommand().equals("Sequels")) 
        {
            search(1);
        }

        // (member ability)
        // view user streaming history
        else if(e.getActionCommand().equals("Streaming"))
        {
            search(2);
        }

        // (admin ability)
        // view members who havce streamed a particular video
        else if(e.getActionCommand().equals("Members Videos"))
        {
             search(3);
        }

        // (admin ability)
        // view streaming trend in last 24 hours
        else if(e.getActionCommand().equals("Trend"))
        {
             search(4);
        }

        // (admin ability)
        // view top 10 movies/series based on streaming history of last month
        else if(e.getActionCommand().equals("Top 10"))
        {
             search(5);
        }

        // (member ability)
        // edit user information
        else if(e.getActionCommand().equals("Edit Profile"))
        {
            new EditDialog();
            if(view != null)
            {
                view.setVisible(false);
            }
            awardCheckBox.setSelected(false);
            notStreamedCheckBox.setSelected(false);
            actorCheckBox.setSelected(false);
            directorCheckBox.setSelected(false);
            genreCheckBox.setSelected(false);
        }

        // (admin ability)
        else if(e.getActionCommand().equals("Add Member"))
        {
            new AddMemberDialog();
            if(view != null)
            {
                view.setVisible(false);
            }
            awardCheckBox.setSelected(false);
            notStreamedCheckBox.setSelected(false);
            actorCheckBox.setSelected(false);
            directorCheckBox.setSelected(false);
            genreCheckBox.setSelected(false);
        }

        // (admin ability)
        else if(e.getActionCommand().equals("Remove Member"))
        {
            queryIndex = 6;
            if(view != null)
            {
                view.setVisible(false);
            }
            awardCheckBox.setSelected(false);
            notStreamedCheckBox.setSelected(false);
            actorCheckBox.setSelected(false);
            directorCheckBox.setSelected(false);
            genreCheckBox.setSelected(false);
            searchPanel.setVisible(true);
            searchButton.setEnabled(true);
            searchField.setEditable(true);
            specifyPanel.setVisible(false);
        }

        // (admin ability)
        else if(e.getActionCommand().equals("Add Movie"))
        {
            new AddMovieDialog();
            if(view != null)
            {
                view.setVisible(false);
            }
            awardCheckBox.setSelected(false);
            notStreamedCheckBox.setSelected(false);
            actorCheckBox.setSelected(false);
            directorCheckBox.setSelected(false);
            genreCheckBox.setSelected(false);
            searchPanel.setVisible(false);
            searchButton.setEnabled(false);
            searchField.setEditable(false);
            specifyPanel.setVisible(false);
        }

        // (admin ability)
        else if(e.getActionCommand().equals("Add Series"))
        {
            new AddSeriesDialog();
            if(view != null)
            {
                view.setVisible(false);
            }
            awardCheckBox.setSelected(false);
            notStreamedCheckBox.setSelected(false);
            actorCheckBox.setSelected(false);
            directorCheckBox.setSelected(false);
            genreCheckBox.setSelected(false);
            searchPanel.setVisible(false);
            searchButton.setEnabled(false);
            searchField.setEditable(false);
            specifyPanel.setVisible(false);
        }

        // (admin ability)
        else if(e.getActionCommand().equals("Remove Movie"))
        {
            queryIndex = 7;
            if(view != null)
            {
                view.setVisible(false);
            }
            awardCheckBox.setSelected(false);
            notStreamedCheckBox.setSelected(false);
            actorCheckBox.setSelected(false);
            directorCheckBox.setSelected(false);
            genreCheckBox.setSelected(false);
            searchPanel.setVisible(true);
            searchButton.setEnabled(true);
            searchField.setEditable(true);
            specifyPanel.setVisible(false);
        }

        // (admin ability)
         else if(e.getActionCommand().equals("Remove Series"))
        {
            queryIndex = 8;
            if(view != null)
            {
                view.setVisible(false);
            }
            awardCheckBox.setSelected(false);
            notStreamedCheckBox.setSelected(false);
            actorCheckBox.setSelected(false);
            directorCheckBox.setSelected(false);
            genreCheckBox.setSelected(false);
            searchPanel.setVisible(true);
            searchButton.setEnabled(true);
            searchField.setEditable(true);
            specifyPanel.setVisible(false);
        }

        // handles gui side of logging out
        else if(e.getActionCommand().equals("LOUT"))
        {
            loginPanel.setVisible(true);
            loginButton.setVisible(true);
            if(menuBar!= null)
                menuBar.setVisible(false);
            if(searchPanel!= null)
                searchPanel.setVisible(false);
            if(specifyPanel!= null)
                specifyPanel.setVisible(false);
            if(view!= null)
            {
                view.setVisible(false);
            }
            if(items[0] != null)
                items[0].setVisible(true);
            if(items[1] != null)
                items[1].setVisible(true);
            if(items[2] != null)
                items[2].setVisible(true);
            if(items[3] != null)
                items[3].setVisible(true);
            if(items[4]!= null)
                items[4].setVisible(true);
            if(items[5]!= null)
                items[5].setVisible(true);
            if(transactionItems[0] != null)
                transactionItems[0].setVisible(true);
            if(transactionItems[1]!= null)
                transactionItems[1].setVisible(true);
            if(transactionItems[2]!= null)
                transactionItems[2].setVisible(true);
            if(transactionItems[3]!= null)
                transactionItems[3].setVisible(true);
            if(transactionItems[4]!= null)
                transactionItems[4].setVisible(true);
            if(transactionItems[5]!= null)
                transactionItems[5].setVisible(true);
            if(transactionItems[6]!= null)
                transactionItems[6].setVisible(true);

            loggedIn = false;
            isMember = false;
            isAdmin = false;
        }

    }
    
    // called by edit option from action performed above
    // gets user's information from database and allows user to edit it
    private class EditDialog extends JDialog implements ActionListener 
    {
        JTextField nameTF, passwordTF, phoneTF, streetTF, cityTF, stateTF, zipTF, emailTF;
        JButton saveButton;

        EditDialog() 
        {
            setupLayout();
            setupMainDialog();
        }
        
        void setupLayout() 
        {
            JPanel panel = new JPanel();

            try 
            {
                Statement stmn;
                ResultSet resultSet;

                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT name FROM Group3.user WHERE username = '" + lhandler.id + "'");
                resultSet.next();
                nameTF = new JTextField((String)resultSet.getObject(1), 25);

                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT password FROM Group3.user WHERE username = '" + lhandler.id + "'");
                resultSet.next();
                passwordTF = new JTextField((String)resultSet.getObject(1), 25);

                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT phone_num FROM Group3.user WHERE username = '" + lhandler.id + "'");
                resultSet.next();
                phoneTF = new JTextField(resultSet.getObject(1).toString(), 25);

                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT street_name FROM Group3.user WHERE username = '" + lhandler.id + "'");
                resultSet.next();
                streetTF = new JTextField((String)resultSet.getObject(1), 25);

                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT city FROM Group3.user WHERE username = '" + lhandler.id + "'");
                resultSet.next();
                cityTF = new JTextField((String)resultSet.getObject(1), 25);

                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT state FROM Group3.user WHERE username = '" + lhandler.id + "'");
                resultSet.next();
                stateTF = new JTextField((String)resultSet.getObject(1), 25);

                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT zipcode FROM Group3.user WHERE username = '" + lhandler.id + "'");
                resultSet.next();
                zipTF = new JTextField(resultSet.getObject(1).toString(), 25);

                stmn = connection.createStatement();
                resultSet = stmn.executeQuery("SELECT email FROM Group3.user WHERE username = '" + lhandler.id + "'");
                resultSet.next();
                emailTF = new JTextField((String)resultSet.getObject(1), 25);
            }
            catch(SQLException sqle) 
            {
                 JOptionPane.showMessageDialog(null, sqle.getMessage(), "Edit error!", JOptionPane.ERROR_MESSAGE);
            }
            
            saveButton = new JButton("Save");
            saveButton.addActionListener(this);

            JLabel nameLbl = new JLabel("Name");
            JLabel passwordLbl = new JLabel("Password");
            JLabel phoneLbl = new JLabel("Phone");
            JLabel streetLbl = new JLabel("Street");
            JLabel cityLbl = new JLabel("City");
            JLabel stateLbl = new JLabel("State");
            JLabel zipLbl = new JLabel("Zip");
            JLabel emailLbl = new JLabel("Email");
            
            GroupLayout layout = new GroupLayout(panel);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);
            panel.setLayout(layout);
                 
            GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
            hGroup.addGroup(layout.createParallelGroup()
                .addComponent(nameLbl)
                .addComponent(passwordLbl)
                .addComponent(phoneLbl)
                .addComponent(streetLbl)
                .addComponent(cityLbl)
                .addComponent(stateLbl)
                .addComponent(zipLbl)
                .addComponent(emailLbl)
                .addComponent(saveButton));
            hGroup.addGroup(layout.createParallelGroup()
               .addComponent(nameTF)
                .addComponent(passwordTF)
                .addComponent(phoneTF)
                .addComponent(streetTF)
                .addComponent(cityTF)
                .addComponent(stateTF)
                .addComponent(zipTF)
                .addComponent(emailTF));
            layout.setHorizontalGroup(hGroup);
            
            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
            vGroup.addGroup(layout.createParallelGroup().addComponent(nameLbl).addComponent(nameTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(passwordLbl).addComponent(passwordTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(phoneLbl).addComponent(phoneTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(streetLbl).addComponent(streetTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(cityLbl).addComponent(cityTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(stateLbl).addComponent(stateTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(zipLbl).addComponent(zipTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(emailLbl).addComponent(emailTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(saveButton));
            layout.setVerticalGroup(vGroup);

            add(panel);
        }

        
        void setupMainDialog() 
        {
            Toolkit tk;
            Dimension d;
        
            tk = Toolkit.getDefaultToolkit();
            d = tk.getScreenSize();
        
            setSize(d.width/3, d.height/2);
            setLocation(d.width/3, d.height/3);
            
            setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setVisible(true);
        }
            

        @Override
        public void actionPerformed(ActionEvent e) 
        {
            try 
            {
                // get info from text fields and update the info in database
                String editStr = "UPDATE Group3.user SET name = '" + nameTF.getText() + "', password = '" + passwordTF.getText() + "', phone_num = '" + phoneTF.getText() + "', street_name = '" + streetTF.getText() + "', city = '" + cityTF.getText() + "', state = '" + stateTF.getText() + "', zipcode = '" + zipTF.getText() + "', email = '" + emailTF.getText() + "' WHERE username = '" + lhandler.id +"'";
                Statement stmn = connection.createStatement();
                stmn.executeUpdate(editStr);
            }
            catch(SQLException sqlee) 
            {
                JOptionPane.showMessageDialog(null, sqlee.getMessage(), "Edit error!", JOptionPane.ERROR_MESSAGE);
            }

            dispose();
        }
    }

    // called by add member option from gframe action performed
    // allows admin to add member to database
    private class AddMemberDialog extends JDialog implements ActionListener 
    {
        JTextField usernameTF, idTF, planTF;
        JButton saveButton;

        AddMemberDialog() 
        {
            setupLayout();
            setupMainDialog();
        }
        
        void setupLayout() 
        {
            JPanel panel = new JPanel();

            usernameTF = new JTextField(25);
            idTF = new JTextField(25);
            planTF = new JTextField(25);
           
            saveButton = new JButton("Save");
            saveButton.addActionListener(this);

            JLabel usernameLbl = new JLabel("Username");
            JLabel idLbl = new JLabel("Member ID");
            JLabel planLbl = new JLabel("Plan Name");
            
            GroupLayout layout = new GroupLayout(panel);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);
            panel.setLayout(layout);
            
            GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
            hGroup.addGroup(layout.createParallelGroup()
                .addComponent(usernameLbl)
                .addComponent(idLbl)
                .addComponent(planLbl)
                .addComponent(saveButton));
            hGroup.addGroup(layout.createParallelGroup()
                .addComponent(usernameTF)
                .addComponent(idTF)
                .addComponent(planTF));
            layout.setHorizontalGroup(hGroup);
            
            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
            vGroup.addGroup(layout.createParallelGroup().addComponent(usernameLbl).addComponent(usernameTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(idLbl).addComponent(idTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(planLbl).addComponent(planTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(saveButton));
            layout.setVerticalGroup(vGroup);

            add(panel);
        }

        
        void setupMainDialog() 
        {
            Toolkit tk;
            Dimension d;
        
            tk = Toolkit.getDefaultToolkit();
            d = tk.getScreenSize();
        
            setSize(d.width/3, d.height/3);
            setLocation(d.width/3, d.height/3);
            
            setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setVisible(true);
        }
            

        @Override
        public void actionPerformed(ActionEvent e) 
        {
            // check for empty text fields and that plan name is valid
            if(!usernameTF.getText().equals("") && !idTF.getText().equals("") && !planTF.getText().equals("") && (planTF.getText().equals("Basic") || planTF.getText().equals("Extra"))) 
            {
                try 
                {
                    // get info from text fields
                    // insert into user and member tables 
                    Integer.parseInt(idTF.getText());
                    String insertStr = "INSERT INTO Group3.user VALUES ('" + usernameTF.getText() + "', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)";
                    Statement stmn = connection.createStatement();
                    stmn.executeUpdate(insertStr);

                    insertStr = "INSERT INTO Group3.member VALUES ('" + usernameTF.getText() + "', '" + idTF.getText() + "', '" + planTF.getText() + "')";
                    stmn = connection.createStatement();
                    stmn.executeUpdate(insertStr);
                    dispose();
                }
                catch(SQLException sqlee) 
                {
                    JOptionPane.showMessageDialog(null, sqlee.getMessage(), "Edit error!", JOptionPane.ERROR_MESSAGE);
                }
                catch(NumberFormatException nfe) 
                {
                    JOptionPane.showMessageDialog(null, "Enter a number in id field", null, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // called by add movie option from gframe action performed
    // allows admin to add movie to database
    private class AddMovieDialog extends JDialog implements ActionListener 
    {
        JTextField imdbTF, titleTF, releaseDateTF, genreTF, awardsTF;
        JButton saveButton;

        AddMovieDialog() 
        {
            setupLayout();
            setupMainDialog();
        }
        
        void setupLayout() 
        {
            JPanel panel = new JPanel();

            imdbTF = new JTextField(25);
            titleTF = new JTextField(25);
            releaseDateTF = new JTextField(25);
            genreTF = new JTextField(25);
            awardsTF = new JTextField(25);
           
            saveButton = new JButton("Save");
            saveButton.addActionListener(this);

            JLabel imdbLbl = new JLabel("IMDB");
            JLabel titleLbl = new JLabel("Title");
            JLabel releaseDateLbl = new JLabel("Release Date");
            JLabel genreLbl = new JLabel("Genre");
            JLabel awardsLbl = new JLabel("Awards");
            
            GroupLayout layout = new GroupLayout(panel);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);
            panel.setLayout(layout);
            
            GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
            hGroup.addGroup(layout.createParallelGroup()
                .addComponent(imdbLbl)
                .addComponent(titleLbl)
                .addComponent(releaseDateLbl)
                .addComponent(genreLbl)
                .addComponent(awardsLbl)
                .addComponent(saveButton));
            hGroup.addGroup(layout.createParallelGroup()
                .addComponent(imdbTF)
                .addComponent(titleTF)
                .addComponent(releaseDateTF)
                .addComponent(genreTF)
                .addComponent(awardsTF));
            layout.setHorizontalGroup(hGroup);
            
            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
            vGroup.addGroup(layout.createParallelGroup().addComponent(imdbLbl).addComponent(imdbTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(titleLbl).addComponent(titleTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(releaseDateLbl).addComponent(releaseDateTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(genreLbl).addComponent(genreTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(awardsLbl).addComponent(awardsTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(saveButton));
            layout.setVerticalGroup(vGroup);

            add(panel);
        }

        
        void setupMainDialog() 
        {
            Toolkit tk;
            Dimension d;
        
            tk = Toolkit.getDefaultToolkit();
            d = tk.getScreenSize();
        
            setSize(d.width/3, d.height/3);
            setLocation(d.width/3, d.height/3);
            
            setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setVisible(true);
        }
            

        @Override
        public void actionPerformed(ActionEvent e) 
        {
            // check for empty text field (primary key)
            if(!imdbTF.getText().equals("")) 
            {
                try 
                {
                    // get info from text fields
                    // insert into media and movies tables
                    String insertStr = "INSERT INTO Group3.media VALUES ('" + imdbTF.getText() + "', '" + titleTF.getText() + "', '" + releaseDateTF.getText() + "', '" + genreTF.getText() + "')";
                    Statement stmn = connection.createStatement();
                    stmn.executeUpdate(insertStr);

                    insertStr = "INSERT INTO Group3.movies VALUES ('" + imdbTF.getText() + "', '" + awardsTF.getText() + "')";
                    stmn = connection.createStatement();
                    stmn.executeUpdate(insertStr);
                    dispose();
                }
                catch(SQLException sqlee) 
                {
                    JOptionPane.showMessageDialog(null, sqlee.getMessage(), "Add movie error!", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // called by add series option from gframe action performed
    // allows admin to add series to database
    private class AddSeriesDialog extends JDialog implements ActionListener 
    {
        JTextField imdbTF, titleTF, releaseDateTF, genreTF, seasonTF, episodeTF;
        JButton saveButton;

        AddSeriesDialog() 
        {
            setupLayout();
            setupMainDialog();
        }
        
        void setupLayout() 
        {
            JPanel panel = new JPanel();

            imdbTF = new JTextField(25);
            titleTF = new JTextField(25);
            releaseDateTF = new JTextField(25);
            genreTF = new JTextField(25);
            seasonTF = new JTextField(25);
            episodeTF = new JTextField(25);
           
            saveButton = new JButton("Save");
            saveButton.addActionListener(this);

            JLabel imdbLbl = new JLabel("IMDB");
            JLabel titleLbl = new JLabel("Title");
            JLabel releaseDateLbl = new JLabel("Release Date");
            JLabel genreLbl = new JLabel("Genre");
            JLabel seasonLbl = new JLabel("Season");
            JLabel episodeLbl = new JLabel("Episode");
            
            GroupLayout layout = new GroupLayout(panel);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);
            panel.setLayout(layout);
                
            GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
            hGroup.addGroup(layout.createParallelGroup()
                .addComponent(imdbLbl)
                .addComponent(titleLbl)
                .addComponent(releaseDateLbl)
                .addComponent(genreLbl)
                .addComponent(seasonLbl)
                .addComponent(episodeLbl)
                .addComponent(saveButton));
            hGroup.addGroup(layout.createParallelGroup()
                .addComponent(imdbTF)
                .addComponent(titleTF)
                .addComponent(releaseDateTF)
                .addComponent(genreTF)
                .addComponent(seasonTF)
                .addComponent(episodeTF));
            layout.setHorizontalGroup(hGroup);
            
            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
            vGroup.addGroup(layout.createParallelGroup().addComponent(imdbLbl).addComponent(imdbTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(titleLbl).addComponent(titleTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(releaseDateLbl).addComponent(releaseDateTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(genreLbl).addComponent(genreTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(seasonLbl).addComponent(seasonTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(episodeLbl).addComponent(episodeTF));
            vGroup.addGroup(layout.createParallelGroup().addComponent(saveButton));
            layout.setVerticalGroup(vGroup);

            add(panel);
        }

        
        void setupMainDialog() 
        {
            Toolkit tk;
            Dimension d;
        
            tk = Toolkit.getDefaultToolkit();
            d = tk.getScreenSize();
        
            setSize(d.width/3, d.height/3);
            setLocation(d.width/3, d.height/3);
            
            setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setVisible(true);
        }
            

        @Override
        public void actionPerformed(ActionEvent e) 
        {
            // check for empty text field (primary key)
            if(!imdbTF.getText().equals("")) 
            {
                try 
                {
                    // get info from text fields
                    // insert into media and series tables
                    Integer.parseInt(seasonTF.getText());
                    Integer.parseInt(episodeTF.getText());
                    String insertStr = "INSERT INTO Group3.media VALUES ('" + imdbTF.getText() + "', '" + titleTF.getText() + "', '" + releaseDateTF.getText() + "', '" + genreTF.getText() + "')";
                    Statement stmn = connection.createStatement();
                    stmn.executeUpdate(insertStr);

                    insertStr = "INSERT INTO Group3.series VALUES ('" + imdbTF.getText() + "', '" + seasonTF.getText() + "', '" + episodeTF.getText() + "')";
                    stmn = connection.createStatement();
                    stmn.executeUpdate(insertStr);
                    dispose();
                }
                catch(SQLException sqlee) 
                {
                    JOptionPane.showMessageDialog(null, sqlee.getMessage(), "Add series error!", JOptionPane.ERROR_MESSAGE);
                }
                catch(NumberFormatException nfe) 
                {
                    JOptionPane.showMessageDialog(null, "Enter a number in season field and episode field", null, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    //inner class for handling logins
    private class LoginHandler implements ActionListener  //lots of new
    {
        
        //JDBC driver name and database URL - for MySQL
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://group3-db.cmtlerer1fja.us-east-1.rds.amazonaws.com";
        String id;

        LoginHandler() 
        {
            if(isConnected == false)
            {
                try
                {
                    //Load the JDBC driver to allow connections with the database
                    Class.forName(driver);
                    //connect to database
                    connection = DriverManager.getConnection(url, "cole", "group3-db");

                    isConnected = true;
                }
                catch(ClassNotFoundException ex)
                {
                    JOptionPane.showMessageDialog(null, "Failed to load JDBC driver");
                    System.exit(1);
                }
                catch(SQLException ex)
                {
                    JOptionPane.showMessageDialog(null, "Failed to load JDBC driver");
                    System.exit(1);
                }
            }
        }

        public void actionPerformed(ActionEvent e)
        {
            if(e.getActionCommand() == "LOG")
            {
                id = idField.getText();
                char[] p = pwdField.getPassword();
                String pwd = new String(p);


                //Check if the user is an admin, member, or both
                String query;
                Statement stmn; 
                ResultSet resultSet;

                try
                {

                    query = "SELECT password FROM Group3.user WHERE username = \"" + id + "\"";
                    stmn = connection.createStatement();
                    resultSet = stmn.executeQuery(query);

                    String row;
                    resultSet.next();

                    do
                    {
                        row = (String) resultSet.getObject(1);

                        if(!pwd.equals(row))
                        {

                            JOptionPane.showMessageDialog(null, "Incorrect Password!", "Login error!", JOptionPane.ERROR_MESSAGE);

                            return;
                        }
                        
                    }while(resultSet.next());

                    query = "SELECT username FROM Group3.admin";
                    stmn = connection.createStatement();
                    resultSet = stmn.executeQuery(query);

                    resultSet.next();

                    do
                    {
                        row = (String) resultSet.getObject(1);


                        if(id.toLowerCase().equals(row.toLowerCase()))
                        {
                            System.out.println("is Admin");
                            isAdmin = true;
                            break;
                        }

                    } while(resultSet.next());

                    query = "SELECT username FROM Group3.member";
                    stmn = connection.createStatement();
                    resultSet = stmn.executeQuery(query);

                    resultSet.next();
                    do
                    {
                        row = (String) resultSet.getObject(1);


                        if(id.toLowerCase().equals(row.toLowerCase()))
                        {
                            System.out.println("is Member");
                            isMember = true;
                            break;
                        }
                    } while(resultSet.next());

                    loggedIn = true;

                }
                catch(SQLException ex)
                {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Login error!", JOptionPane.ERROR_MESSAGE);
                }

                // set up gui component visibility
                if(isAdmin || isMember)
                {
                    loginPanel.setVisible(false);
                    loginButton.setVisible(false);
                    menuBar.setVisible(true);

                    idField.setText("");
                    pwdField.setText("");
                }

                if(!isAdmin && !isMember)
                {
                    JOptionPane.showMessageDialog(null, "Invlaid Credentials");
                }

                // customize gui for admin
                else if(isAdmin && !isMember)
                {

                    items[0].setVisible(false);
                    items[1].setVisible(false);
                    items[2].setVisible(false);
                    transactionItems[0].setVisible(false);
                }

                // customize gui for member
                else if(isMember && !isAdmin)
                {

                    items[3].setVisible(false);
                    items[4].setVisible(false);
                    items[5].setVisible(false);
                    transactionItems[1].setVisible(false);
                    transactionItems[2].setVisible(false);
                    transactionItems[3].setVisible(false);
                    transactionItems[4].setVisible(false);
                    transactionItems[5].setVisible(false);
                    transactionItems[6].setVisible(false);
                }
            }
        }
    }

    //private class for handling query
    private class QueryHandler
    {
        public void getQueryTable(String query)
        {
            Statement statement;
            ResultSet resultSet;

            try
            {
                statement = connection.createStatement();
                resultSet = statement.executeQuery(query);

                //if there are no records, display as such
                if(!resultSet.next())
                {
                    JOptionPane.showMessageDialog(null, "No records found");
                    searchField.setText("");
                    if(view != null)
                    {
                        view.setVisible(false);
                    }
                    awardCheckBox.setSelected(false);
                    notStreamedCheckBox.setSelected(false);
                    actorCheckBox.setSelected(false);
                    directorCheckBox.setSelected(false);
                    genreCheckBox.setSelected(false);
                    return;
                }
                else
                {
                    //columnNames holds the column names of the query results
                    Vector<Object> columnNames = new Vector<Object>();

                    //rows a Vector holding more vectors of values representing certain rows of the query results
                    Vector<Object> rows = new Vector<Object>();

                    //get column headers
                    ResultSetMetaData metaData = resultSet.getMetaData();

                    for(int i = 1; i <= metaData.getColumnCount(); i++)
                    {
                        columnNames.addElement(metaData.getColumnName(i));                  //prints column names
                    }
                    
                    //get row data
                    do
                    {
                        Vector<Object> currentRow = new Vector<Object>();
                        for(int i = 1; i <= metaData.getColumnCount(); i++)
                        {
                            currentRow.addElement(resultSet.getObject(i));
                        }
                        
                        rows.addElement(currentRow);
                    } while(resultSet.next()); //moves cursor to next record
                
                    if(view != null)
                    {
                        getContentPane().remove(view);
                    }

                    //display table with ResultSet Contents
                    table = new JTable((Vector)rows, columnNames);
                    table.setPreferredScrollableViewportSize(new Dimension(400, 10 * table.getRowHeight()));
                    table.setDefaultEditor(Object.class, null);
                    table.setCellSelectionEnabled(true);

                    table.addMouseListener(new MouseAdapter() 
                    {
                        URI uri;

                        public void mouseClicked(MouseEvent e) 
                        {

                            int x = table.getSelectedRow();
                            int y = table.getSelectedColumn();

                            // clickable link to imdb page in jtable
                            if(table.getColumnName(y).equals("IMDB"))
                            {
                                try 
                                {
                                    uri = new URI((String)table.getValueAt(x, y));
                                } 
                                catch (URISyntaxException e1) {
                
                                    e1.printStackTrace();
                                }

                                open(uri);
                            }
                            // click on row in jtable to "stream it"
                            // it will update the appropriate tables in the database to "stream" the media
                            else 
                            {
                                for (int n=0; n < table.getColumnCount(); n++) 
                                {
                                    if(table.getColumnName(n).equals("IMDB")) 
                                    {
                                        String imdb = (String)table.getModel().getValueAt(x, n);

                                        try 
                                        {
                                            String str = "INSERT INTO Group3.timestamp (timestamp, timestamp_id) SELECT CURRENT_TIMESTAMP(), MAX(timestamp_id) + 1 FROM Group3.timestamp";
                                            Statement stmn = connection.createStatement();
                                            stmn.executeUpdate(str);

                                            str = "INSERT INTO Group3.streams (username, timestamp_id, IMDB) VALUES ('" + lhandler.id + "', (SELECT MAX(timestamp_id) FROM Group3.timestamp), '" + imdb + "')";
                                            stmn = connection.createStatement();
                                            stmn.executeUpdate(str);

                                            JOptionPane.showMessageDialog(null, "Streamed", null, JOptionPane.INFORMATION_MESSAGE);
                                        }
                                        catch(SQLException sqle) 
                                        {
                                            JOptionPane.showMessageDialog(null, sqle.getMessage(), "Stream insert error!", JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                }
                            }
                        }
                    });

                    view = new JScrollPane(table);
                    getContentPane().add(view);
                    validate();
                }
                statement.close();
            }

            catch(SQLException ex)
            {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Query error!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class WindowHandler extends WindowAdapter
    {
        public void windowClosing(WindowEvent e)
        {
            try
            {
                if(connection != null)
                {
                    connection.close();
                }
            }

            catch(SQLException ex)
            {
                JOptionPane.showMessageDialog(null, "Unable to disconnect!");
            }
            System.exit(0);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) 
    {
        throw new UnsupportedOperationException("Unimplemented method 'mouseClicked'");
    }

    @Override
    public void mousePressed(MouseEvent e) 
    {
        throw new UnsupportedOperationException("Unimplemented method 'mousePressed'");
    }

    @Override
    public void mouseReleased(MouseEvent e) 
    {
        throw new UnsupportedOperationException("Unimplemented method 'mouseReleased'");
    }

    @Override
    public void mouseEntered(MouseEvent e) 
    {
        throw new UnsupportedOperationException("Unimplemented method 'mouseEntered'");
    }

    @Override
    public void mouseExited(MouseEvent e) 
    {
        throw new UnsupportedOperationException("Unimplemented method 'mouseExited'");
    }

    private static void open(URI uri) 
    {
        if(Desktop.isDesktopSupported()) 
        {
            try 
            {
                Desktop.getDesktop().browse(uri);
            } 
            catch (IOException e) {}
        } 
        else {}
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) 
    {
        int keyCode = e.getKeyCode();

        if(keyCode == 10)
        {
            if(loggedIn == false)
            {
                for(ActionListener a: loginButton.getActionListeners()) {
                    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "LOG") {   
                    });
                }
            }
            else if(loggedIn == true)
            {
                for(ActionListener a: searchButton.getActionListeners()) 
                {
                    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Search") {});
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}