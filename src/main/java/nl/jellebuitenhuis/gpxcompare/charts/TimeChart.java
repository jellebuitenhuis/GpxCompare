package nl.jellebuitenhuis.gpxcompare.charts;

import io.jenetics.jpx.WayPoint;
import nl.jellebuitenhuis.gpxcompare.GPXPanel;
import org.jfree.chart.*;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

import static org.jfree.data.general.DatasetUtils.findItemIndicesForX;

public class TimeChart extends JFrame {

  private double distance;
  private Crosshair xCrosshair;
  private Crosshair yCrosshair;
  private GPXPanel mapPanel;
  private Map<Double,WayPoint> distanceWayPointMap;

  public TimeChart(List<WayPoint> a1, List<WayPoint> a2, GPXPanel mapPanel) {
      super("Time Chart");
      this.mapPanel = mapPanel;
      this.distanceWayPointMap = new TreeMap<>();
      Map<Double, Double> timeDistanceMap = new TreeMap<>();
      distance = 0;
      for(int i = 0; i < a1.size(); i++)
      {
        if(a1.get(i) != null && a2.get(i) != null)
        {
          if(i != 0)
          {
            int j = 1;
            double oldDistance = distance;
            while(oldDistance == distance)
            {
              try
              {
                  if(a1.get(i-j) != null && a2.get(i-j) != null)
                  {
                      distance += a1.get(i).distance(a1.get(i - j)).doubleValue();
                      distanceWayPointMap.put(Math.floor(distance), a1.get(i));
                  }
                  j++;
              }
              catch (ArrayIndexOutOfBoundsException e)
              {
                  distance = oldDistance;
                  oldDistance += Double.MIN_VALUE;
              }
            }
          }
          double timeDifference = a1.get(i).getTime().get().toEpochSecond() - a2.get(i).getTime().get().toEpochSecond();
          timeDistanceMap.put(timeDifference, distance);
        }
      }
      XYDataset xydataset = createDataset(timeDistanceMap);
      JFreeChart jfreechart = createChart(xydataset);
      jfreechart.setRenderingHints(
          new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
      ChartPanel chartpanel = new ChartPanel(jfreechart);
      chartpanel.addChartMouseListener(new ChartMouseListener() {
          @Override
          public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {

          }

          @Override
          public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
              Rectangle2D r2d = chartpanel.getScreenDataArea();
              JFreeChart chart = chartMouseEvent.getChart();
              XYPlot xyPlot = (XYPlot)chart.getPlot();
              ValueAxis xAxis = xyPlot.getDomainAxis();
              double xValue = xAxis.java2DToValue(chartMouseEvent.getTrigger().getX(), r2d, RectangleEdge.BOTTOM);
              if (!xAxis.getRange().contains(xValue)) {
                  xValue = 0.0D / 0.0;
              }

              double yValue = findYValue(xyPlot.getDataset(), 0, xValue);
              if(distanceWayPointMap.containsKey(Math.floor(xValue)))
              {
                  mapPanel.setPerson1WayPoint(distanceWayPointMap.get(Math.floor(xValue)));
              }
              xCrosshair.setValue(xValue);
              yCrosshair.setValue(yValue);
          }
      });
      CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
      xCrosshair = new Crosshair(0.0D / 0.0, Color.GREEN, new BasicStroke(0.0F));
      xCrosshair.setLabelOutlineVisible(false);
      xCrosshair.setLabelBackgroundPaint(new Color(1f, 1f, 1f, 0f));
      xCrosshair.setLabelFont(xCrosshair.getLabelFont().deriveFont(20f));
      xCrosshair.setLabelGenerator(new CrosshairLabelGenerator() {
          @Override
          public String generateLabel(Crosshair crosshair) {
              return Math.round(crosshair.getValue()) + " meter";
          }
      });
      xCrosshair.setLabelVisible(true);
      yCrosshair = new Crosshair(0.0D / 0.0, Color.GREEN, new BasicStroke(0.0F));
      yCrosshair.setLabelOutlineVisible(false);
      yCrosshair.setLabelBackgroundPaint(new Color(1f, 1f, 1f, 0f));
      yCrosshair.setLabelFont(xCrosshair.getLabelFont().deriveFont(20f));      yCrosshair.setLabelGenerator(new CrosshairLabelGenerator() {
          @Override
          public String generateLabel(Crosshair crosshair) {
              return Math.round(crosshair.getValue()) + " seconds";
          }
      });
      yCrosshair.setLabelVisible(true);
      crosshairOverlay.addDomainCrosshair(xCrosshair);
      crosshairOverlay.addRangeCrosshair(yCrosshair);
      chartpanel.addOverlay(crosshairOverlay);
      try {
          Field mask = ChartPanel.class.getDeclaredField("panMask");
          mask.setAccessible(true);
          mask.set(chartpanel, 0);
      } catch (NoSuchFieldException | IllegalAccessException e) {
          e.printStackTrace();
      }
      chartpanel.addMouseWheelListener(arg0 -> chartpanel.restoreAutoRangeBounds());
      chartpanel.setDisplayToolTips(true);
      chartpanel.setMouseWheelEnabled(true);
      chartpanel.setMouseZoomable(true);
      chartpanel.setMaximumDrawHeight(99999);
      chartpanel.setMaximumDrawWidth(99999);
      chartpanel.setMinimumDrawHeight(1);
      chartpanel.setMinimumDrawWidth(1);
      setContentPane(chartpanel);
  }

    public static double findYValue(XYDataset dataset, int series, double x) {
        // delegate null check on dataset
        int[] indices = findItemIndicesForX(dataset, series, x);
        if (indices[0] == -1) {
            return Double.NaN;
        }
        if (indices[0] == indices[1]) {
            return dataset.getYValue(series, indices[0]);
        }
        double x0 = dataset.getXValue(series, indices[0]);
        double x1 = dataset.getXValue(series, indices[1]);
        double y0 = dataset.getYValue(series, indices[0]);
        double y1 = dataset.getYValue(series, indices[1]);
        return y0 + (y1 - y0) * (x - x0) / (x1 - x0);
    }

  /**
   * Creates the dataset to be used on the chart.
   */
  private XYDataset createDataset(Map<Double, Double> timeDistanceMap) {
      XYSeries xyseries = new XYSeries("Times");
      for(double t : timeDistanceMap.keySet())
      {
        xyseries.add((double) timeDistanceMap.get(t), t);
      }
      XYSeriesCollection xyseriescollection = new XYSeriesCollection();
      xyseriescollection.addSeries(xyseries);
      xyseriescollection.setIntervalWidth(0.0D);
      return xyseriescollection;
  }

  /**
   * Creates the chart to be used in the window frame.
   */
  private JFreeChart createChart(XYDataset xydataset) {
      JFreeChart jfreechart = null;
      jfreechart = ChartFactory.createXYLineChart(
              "Time Comparison", "Distance (m)", "Time difference (s)",
              xydataset, PlotOrientation.VERTICAL, false, true, false);
      XYPlot xyplot = (XYPlot) jfreechart.getPlot();
      xyplot.setDomainPannable(true);
      xyplot.setRangePannable(true);
      xyplot.getRenderer().setSeriesPaint(0, new Color(255, 0, 0));
      xyplot.setForegroundAlpha(0.65F);
      xyplot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
      xyplot.getRenderer().setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
      xyplot.getRenderer().setDefaultToolTipGenerator(new StandardXYToolTipGenerator() {
          @Override
          public String generateToolTip(XYDataset xyDataset, int i, int i1) {
                return xyDataset.getXValue(i,i1) + " " + xyDataset.getYValue(i,i1);
          }
      });

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
