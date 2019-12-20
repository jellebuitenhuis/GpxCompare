package com.gpxcreator;

import com.gpxcreator.gpxpanel.Waypoint;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * A chart for displaying a GPX element's speed profile.
 *
 * @author Matt Hoover
 */
@SuppressWarnings("serial")
public class TimeChart extends JFrame {

  private double distance = 0;

  /**
   * Constructs the {@link TimeChart} window.
   *
   * @param title         The chart window title.
   * @param headingPrefix The heading for the graphics on the chart.
   */
  public TimeChart(String title, String headingPrefix, Map<Waypoint, Waypoint> waypointMap) {
    super(title);
    XYDataset xydataset = createDataset(waypointMap);
    JFreeChart jfreechart = createChart(xydataset, waypointMap, headingPrefix);
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
  private XYDataset createDataset(Map<Waypoint, Waypoint> waypointMap) {
    XYSeries xyseries = new XYSeries("Times");
    Waypoint previousWaypoint = null;
    double time = 0;
    double previousTime = 0;
    for(Waypoint w : waypointMap.keySet())
    {
      if(previousWaypoint != null) {
        distance += GPXCreator.calculateDistance(previousWaypoint.getLat(), previousWaypoint.getLon(), w.getLat(), w.getLon());
      }
      time = (w.getTime().getTime() - waypointMap.get(w).getTime().getTime()) / 1000;
      xyseries.add(distance, time);
      previousWaypoint = w;
      previousTime = time;
    }
    XYSeriesCollection xyseriescollection = new XYSeriesCollection();
    xyseriescollection.addSeries(xyseries);
    xyseriescollection.setIntervalWidth(0.0D);
    return xyseriescollection;
  }

  /**
   * Creates the chart to be used in the window frame.
   */
  private JFreeChart createChart(XYDataset xydataset, Map<Waypoint, Waypoint> waypointMap, String headingPrefix) {
    JFreeChart jfreechart = null;
    jfreechart = ChartFactory.createXYLineChart(
        headingPrefix + " - " + "Times", "Distance (m)", "Time difference (s)",
        xydataset, PlotOrientation.VERTICAL, false, false, false);

    XYPlot xyplot = (XYPlot) jfreechart.getPlot();
    xyplot.getRenderer().setSeriesPaint(0, new Color(255, 0, 0));
    xyplot.setForegroundAlpha(0.65F);
    xyplot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));

    ValueAxis domainAxis = xyplot.getDomainAxis();
    domainAxis.setRange(0, distance);

    //double padding = maxDist / 10D;
    //double rangeMax = maxDist + padding;
    ValueAxis rangeAxis = xyplot.getRangeAxis();
    //rangeAxis.setRange(0, rangeMax);

    domainAxis.setTickMarkPaint(Color.black);
    domainAxis.setLowerMargin(0.0D);
    domainAxis.setUpperMargin(0.0D);
    rangeAxis.setTickMarkPaint(Color.black);
    return jfreechart;
  }
}
