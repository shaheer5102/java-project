import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.PlotOrientation;

public class DataAnalyzerGUI extends JFrame {
    private JComboBox<String> yearComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<String> productComboBox;
    private JButton queryButton;
    private JTextArea resultArea;
    private DefaultCategoryDataset barDataset;
    private DefaultPieDataset yearPieDataset;
    private DefaultPieDataset monthPieDataset;
    private ChartPanel barChartPanel;
    private ChartPanel yearPieChartPanel;
    private ChartPanel monthPieChartPanel;
    private JTextField yearField;
    private JTextField monthField;
    private JTextField productField;
    private JTextField tjNcvField;
    private JButton insertButton;
    private JButton trendAnalysisButton;
    private JButton monthlyTrendAnalysisButton;

    public DataAnalyzerGUI() {
        setTitle("Data Analyzer");
        setSize(1280, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(5, 2));

        yearComboBox = new JComboBox<>();
        inputPanel.add(new JLabel("Year (Existing data):"));
        inputPanel.add(yearComboBox);

        monthComboBox = new JComboBox<>();
        inputPanel.add(new JLabel("Month (Existing data):"));
        inputPanel.add(monthComboBox);

        yearField = new JTextField();
        inputPanel.add(new JLabel("Year (New data):"));
        inputPanel.add(yearField);

        monthField = new JTextField();
        inputPanel.add(new JLabel("Month (New data):"));
        inputPanel.add(monthField);

        productField = new JTextField();
        inputPanel.add(new JLabel("Product (New data):"));
        inputPanel.add(productField);

        tjNcvField = new JTextField();
        inputPanel.add(new JLabel("TJ_NCV (New data):"));
        inputPanel.add(tjNcvField);

        inputPanel.add(new JLabel("Press to Get the records"));

        queryButton = new JButton("Query");
        queryButton.addActionListener(e -> performQuery());
        inputPanel.add(queryButton);

        inputPanel.add(new JLabel("Press to Post the record"));

        insertButton = new JButton("Insert");
        insertButton.addActionListener(e -> insertData());
        inputPanel.add(insertButton);

        productComboBox = new JComboBox<>();
        inputPanel.add(new JLabel("Select product for analysis:"));
        inputPanel.add(productComboBox);
        populateProductDropdown();

        trendAnalysisButton = new JButton("Yearly Trend Analysis");
        trendAnalysisButton.addActionListener(e -> showTrendAnalysisWindow());
        inputPanel.add(trendAnalysisButton);

        monthlyTrendAnalysisButton = new JButton("Monthly Trend Analysis");
        monthlyTrendAnalysisButton.addActionListener(e -> showMonthlyTrendAnalysisWindow());
        inputPanel.add(monthlyTrendAnalysisButton);

        add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea(5, 40);
        resultArea.setLineWrap(true); // Enable line wrapping
        resultArea.setPreferredSize(new Dimension(800, 400));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        add(scrollPane, BorderLayout.WEST);

        barDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart("Data Analysis",
                "Product",
                "Value",
                barDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        barChartPanel = new ChartPanel(barChart);
        barChartPanel.setPreferredSize(new Dimension(400, 400));
        add(barChartPanel, BorderLayout.SOUTH);

        yearPieDataset = new DefaultPieDataset();
        JFreeChart yearPieChart = ChartFactory.createPieChart("Year Distribution", yearPieDataset, true, true, false);
        yearPieChartPanel = new ChartPanel(yearPieChart);
        yearPieChartPanel.setPreferredSize(new Dimension(400, 400));
        add(yearPieChartPanel, BorderLayout.CENTER);

        monthPieDataset = new DefaultPieDataset();
        JFreeChart monthPieChart = ChartFactory.createPieChart("Month Distribution", monthPieDataset, true, true, false);
        monthPieChartPanel = new ChartPanel(monthPieChart);
        monthPieChartPanel.setPreferredSize(new Dimension(400, 400));
        add(monthPieChartPanel, BorderLayout.EAST);

        populateDropdowns();
    }

    private void populateProductDropdown() {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String sql = "SELECT DISTINCT product FROM OilProducts_monthlyDeliveries_2018";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    productComboBox.addItem(resultSet.getString("product"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private void populateDropdowns() {
        try (Connection connection = DatabaseConnector.getConnection()) {
            populateComboBox(connection, yearComboBox, "year", "OilProducts_monthlyDeliveries_2018");
            populateComboBox(connection, monthComboBox, "month", "OilProducts_monthlyDeliveries_2018");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void populateComboBox(Connection connection, JComboBox<String> comboBox, String columnName, String tableName) throws SQLException {
        String sql = "SELECT DISTINCT " + columnName + " FROM " + tableName;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                comboBox.addItem(resultSet.getString(columnName));
            }
        }
    }

    private void showTrendAnalysisWindow() {
        String selectedProduct = (String) productComboBox.getSelectedItem();
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Please select a product.");
            return;
        }

        TrendAnalysisWindow trendWindow = new TrendAnalysisWindow(selectedProduct);
        trendWindow.setVisible(true);
    }

    private void showMonthlyTrendAnalysisWindow() {
        String selectedProduct = (String) productComboBox.getSelectedItem();
        String selectedYear = (String) yearComboBox.getSelectedItem();
        if (selectedProduct == null || selectedYear == null) {
            JOptionPane.showMessageDialog(this, "Please select a product and a year.");
            return;
        }

        MonthlyTrendAnalysisWindow monthlyTrendWindow = new MonthlyTrendAnalysisWindow(selectedProduct, selectedYear);
        monthlyTrendWindow.setVisible(true);
    }

    private void performQuery() {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String selectedYear = (String) yearComboBox.getSelectedItem();
            String selectedMonth = (String) monthComboBox.getSelectedItem();

            String sql = "SELECT * FROM OilProducts_monthlyDeliveries_2018 WHERE year = ? AND month = ? ";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, selectedYear);
            statement.setString(2, selectedMonth);

            ResultSet resultSet = statement.executeQuery();

            String sql1 = "SELECT product, SUM(TJ_NCV) AS total_TJ_NCV FROM OilProducts_monthlyDeliveries_2018 WHERE year = ? GROUP BY product";
            PreparedStatement statement1 = connection.prepareStatement(sql1);
            statement1.setString(1, selectedYear);

            ResultSet resultSet1 = statement1.executeQuery();


            resultArea.setText(""); // Clear previous results
            barDataset.clear(); // Clear previous bar chart data
            yearPieDataset.clear(); // Clear previous year pie chart data
            monthPieDataset.clear(); // Clear previous month pie chart data

            resultArea.append("Year\tMonth\tProduct\tTJ_NCV\n");

            while (resultSet.next()) {
                // Display query results in the text area
                resultArea.append(resultSet.getString("year") + "\t" +
                        resultSet.getString("month") + "\t" +
                        resultSet.getString("product") + "\t" +
                        resultSet.getString("TJ_NCV") + "\n");

                // Add data to the bar chart dataset
                barDataset.addValue(resultSet.getDouble("TJ_NCV"), "Value", resultSet.getString("product"));

                // Add data to the month pie chart dataset
                monthPieDataset.setValue(resultSet.getString("product") + " (" + resultSet.getString("TJ_NCV") + ")", resultSet.getDouble("TJ_NCV"));
            }

            while(resultSet1.next()) {
                yearPieDataset.setValue(resultSet1.getString("product") + " (" + resultSet1.getDouble("total_TJ_NCV") + ")", resultSet1.getDouble("total_TJ_NCV"));
            }

            // Update the bar chart
            JFreeChart updatedBarChart = ChartFactory.createBarChart("Data Analysis",
                    "Product",
                    "Value",
                    barDataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false);

            barChartPanel.setChart(updatedBarChart);
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultArea.setText("Error executing query: " + ex.getMessage());
        }
    }

    private void insertData() {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String sql = "INSERT INTO OilProducts_monthlyDeliveries_2018 (year, month, product, TJ_NCV) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, yearField.getText());
            statement.setString(2, monthField.getText());
            statement.setString(3, productField.getText());
            statement.setDouble(4, Double.parseDouble(tjNcvField.getText()));

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                resultArea.setText("Data inserted successfully!");
                // Add the new year and month to the dropdown selections
                String newYear = yearField.getText();
                String newMonth = monthField.getText();

                DefaultComboBoxModel yearModel = (DefaultComboBoxModel) yearComboBox.getModel();
                if (yearModel.getIndexOf(newYear) == -1) {
                    yearComboBox.addItem(newYear);
                }

                // Check if the month already exists in the monthComboBox
                DefaultComboBoxModel monthModel = (DefaultComboBoxModel) monthComboBox.getModel();
                if (monthModel.getIndexOf(newMonth) == -1) {
                    monthComboBox.addItem(newMonth);
                }

                // Update pie charts with new data
                yearPieDataset.setValue(newYear, Double.parseDouble(tjNcvField.getText()));
                monthPieDataset.setValue(newMonth, Double.parseDouble(tjNcvField.getText()));
            } else {
                resultArea.setText("Failed to insert data.");
            }
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            resultArea.setText("Error inserting data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DataAnalyzerGUI gui = new DataAnalyzerGUI();
            gui.setVisible(true);
        });
    }
}

class DatabaseConnector {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=data;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASS = "myPassw0rd";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("SQL Server JDBC driver not found!");
        }

        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}

