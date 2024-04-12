import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MonthlyTrendAnalysisWindow extends JFrame {
    public MonthlyTrendAnalysisWindow(String product, String year) {
        setTitle("Monthly Trend Analysis for " + product + " in " + year);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Fetch data for the selected product and year
        DefaultCategoryDataset dataset = fetchProductMonthlyTrendData(product, year);

        // Create and display the chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Product Monthly Trend Analysis",
                "Month",
                "TJ_NCV",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        add(chartPanel);
    }

    private DefaultCategoryDataset fetchProductMonthlyTrendData(String product, String year) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection connection = DatabaseConnector.getConnection()) {
            String sql = "SELECT month, SUM(TJ_NCV) AS total_TJ_NCV FROM OilProducts_monthlyDeliveries_2018 WHERE product = ? AND year = ? GROUP BY month";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, product);
                statement.setString(2, year);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        dataset.addValue(resultSet.getDouble("total_TJ_NCV"), "TJ_NCV", resultSet.getString("month"));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return dataset;
    }
}
