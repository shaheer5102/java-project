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

public class TrendAnalysisWindow extends JFrame {
    public TrendAnalysisWindow(String product) {
        setTitle("Trend Analysis for " + product);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Fetch data for the selected product
        DefaultCategoryDataset dataset = fetchProductTrendData(product);

        // Create and display the chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Product Trend Analysis",
                "Year",
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

    private DefaultCategoryDataset fetchProductTrendData(String product) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection connection = DatabaseConnector.getConnection()) {
            String sql = "SELECT year, SUM(TJ_NCV) AS total_TJ_NCV FROM OilProducts_monthlyDeliveries_2018 WHERE product = ? GROUP BY year";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, product);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        dataset.addValue(resultSet.getDouble("total_TJ_NCV"), "TJ_NCV", resultSet.getString("year"));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return dataset;
    }
}
