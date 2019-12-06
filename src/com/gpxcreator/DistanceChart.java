package com.gpxcreator;

import com.gpxcreator.gpxpanel.Waypoint;
import com.gpxcreator.gpxpanel.WaypointGroup;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openstreetmap.gui.jmapviewer.OsmMercator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * A chart for displaying a GPX element's speed profile.
 *
 * @author Matt Hoover
 */
@SuppressWarnings("serial")
public class DistanceChart extends JFrame {

  private double maxDist;

  /**
   * Constructs the {@link DistanceChart} window.
   *
   * @param title         The chart window title.
   * @param headingPrefix The heading for the graphics on the chart.
   */
  public DistanceChart(String title, String headingPrefix, ArrayList<Double> distances) {
    super(title);
    maxDist = 0;
    XYDataset xydataset = createDataset(distances);
    JFreeChart jfreechart = createChart(xydataset, distances, headingPrefix);
    jfreechart.setRenderingHints(
        new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
    ChartPanel chartpanel = new ChartPanel(jfreechart);
    chartpanel.setMaximumDrawHeight(99999);
    chartpanel.setMaximumDrawWidth(99999);
    chartpanel.setMinimumDrawHeight(1);
    chartpanel.setMinimumDrawWidth(1);
    setContentPane(chartpanel);
  }

  /**
   * Creates the dataset to be used on the chart.
   */
  private XYDataset createDataset(ArrayList<Double> distances) {
    XYSeries xyseries = new XYSeries("Distances");
    for (int i = 0; i < distances.size();i++){
        xyseries.add(i, distances.get(i));
        maxDist = Math.max(distances.get(i), maxDist);
    }
    XYSeriesCollection xyseriescollection = new XYSeriesCollection();
    xyseriescollection.addSeries(xyseries);
    xyseriescollection.setIntervalWidth(0.0D);
    return xyseriescollection;
  }

  /**
   * Creates the chart to be used in the window frame.
   */
  private JFreeChart createChart(XYDataset xydataset, ArrayList<Double> distances, String headingPrefix) {
    JFreeChart jfreechart = null;
    jfreechart = ChartFactory.createXYLineChart(
        headingPrefix + " - " + "Distances", "Point (int)", "Distance (meter)",
        xydataset, PlotOrientation.VERTICAL, false, false, false);

    XYPlot xyplot = (XYPlot) jfreechart.getPlot();
    xyplot.getRenderer().setSeriesPaint(0, new Color(255, 0, 0));
    xyplot.setForegroundAlpha(0.65F);
    xyplot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));

    ValueAxis domainAxis = xyplot.getDomainAxis();
    domainAxis.setRange(0, distances.size());

    double padding = maxDist / 10D;
    double rangeMax = maxDist + padding;
    ValueAxis rangeAxis = xyplot.getRangeAxis();
    rangeAxis.setRange(0, rangeMax);

    domainAxis.setTickMarkPaint(Color.black);
    domainAxis.setLowerMargin(0.0D);
    domainAxis.setUpperMargin(0.0D);
    rangeAxis.setTickMarkPaint(Color.black);
    return jfreechart;
  }
}
