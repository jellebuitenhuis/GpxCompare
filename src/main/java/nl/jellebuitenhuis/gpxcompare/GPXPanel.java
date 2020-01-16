package nl.jellebuitenhuis.gpxcompare;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GPXPanel extends JXMapViewer {

    private List<GPX> gpxFiles;

    private List<WayPoint> a1;
    private List<WayPoint> a2;

    public GPXPanel()
    {
        super();
        gpxFiles = new ArrayList<>();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d = (Graphics2D) g2d.create();
        // convert from viewport to world bitmap
        Rectangle rect = getViewportBounds();
        g2d.translate(-rect.x, -rect.y);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        paintFiles(g2d);

    }

    private void paintFiles(Graphics g2d) {
        super.paintComponent(g2d);
        for(GPX gpx : gpxFiles)
        {
            paintPath((Graphics2D)g2d,gpx.getTracks());
        }
    }

    private void paintPath(Graphics2D g2d, List<Track> tracks)
    {
        super.paintComponent(g2d);
        g2d.setStroke(new BasicStroke(5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        BufferedImage imgPathPt = null;
        try {
            imgPathPt = ImageIO.read(new File("src/main/java/nl/jellebuitenhuis/gpxcompare/icons/waypoint.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int lastX = 0;
        int lastY = 0;
        boolean first = true;
        List<TrackSegment> segments = tracks.get(0).getSegments();
        TrackSegment trackSegment = segments.get(0);
        List<WayPoint> segmentPoints = trackSegment.getPoints();
        List<GeoPosition> positions = new ArrayList<>();
        for (WayPoint w : segmentPoints) {
            positions.add(new GeoPosition(w.getLatitude().toDegrees(), w.getLongitude().toDegrees()));
        }
        for (GeoPosition gp : positions) {
            // convert geo-coordinate to world bitmap pixel
            Point2D pt = getTileFactory().geoToPixel(gp, getZoom());

            if (first) {
                first = false;
            } else {
//                g2d.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
//                g2d.drawImage(imgPathPt, (int) pt.getX() - 9,(int) pt.getY() - 28, null);
//                g2d.setColor(Color.BLUE);
//                g2d.fillOval((int) pt.getX(),(int)pt.getY(),5,5);
            }

            lastX = (int) pt.getX();
            lastY = (int) pt.getY();
        }

        if(a1 != null && a2 != null)
        {
            for(int i = 0; i < a1.size(); i++)
            {
                if(a1.get(i) != null && a2.get(i) != null)
                {
                    Point2D pt = getTileFactory().geoToPixel(new GeoPosition(a1.get(i).getLatitude().toDegrees(), a1.get(i).getLongitude().toDegrees()), getZoom());
                    g2d.setColor(Color.CYAN);
                    g2d.fillOval((int) pt.getX(),(int)pt.getY(),5,5);
                    pt = getTileFactory().geoToPixel(new GeoPosition(a2.get(i).getLatitude().toDegrees(), a2.get(i).getLongitude().toDegrees()), getZoom());
                    g2d.setColor(Color.BLUE);
                    g2d.fillOval((int) pt.getX(),(int)pt.getY(),5,5);
                }
                else if(a1.get(i) != null && a2.get(i) == null)
                {
                    Point2D pt = getTileFactory().geoToPixel(new GeoPosition(a1.get(i).getLatitude().toDegrees(), a1.get(i).getLongitude().toDegrees()), getZoom());
                    g2d.setColor(Color.RED);
                    g2d.fillOval((int) pt.getX(),(int)pt.getY(),5,5);
                }
                else if(a1.get(i) == null && a2.get(i) != null)
                {
                    Point2D pt = getTileFactory().geoToPixel(new GeoPosition(a2.get(i).getLatitude().toDegrees(), a2.get(i).getLongitude().toDegrees()), getZoom());
                    g2d.setColor(Color.ORANGE);
                    g2d.fillOval((int) pt.getX(),(int)pt.getY(),5,5);
                }
            }
        }

    }

    public List<GPX> getGpxFiles()
    {
        return gpxFiles;
    }

    public void addGPXFile(GPX file)
    {
        gpxFiles.add(file);
    }

    public void removeGPXFile(GPX file)
    {
        gpxFiles.remove(file);
    }

    public void setCompareLists(List<WayPoint> a1, List<WayPoint> a2)
    {
        this.a1 = a1;
        this.a2 = a2;
    }
}
