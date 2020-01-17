package nl.jellebuitenhuis.gpxcompare;

import io.jenetics.jpx.*;
import nl.jellebuitenhuis.gpxcompare.charts.TimeChart;
import org.apache.commons.io.FilenameUtils;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.google.GoogleMapsTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GPXCompare {


    private JFrame frame;
    private JPanel glassPane;
    private JLabel glassPaneStatus;
    private static String lookAndFeel;
    private JToolBar toolBarMain;                              // NORTH
    private boolean fileIOHappening;
    private JButton btnFileNew;
    private JButton btnFileOpen;
    private JFileChooser chooserFileOpen;
    private JButton btnFileSave;
    private JButton btnEqualization;
    private JButton btnGapPenalty;
    private JButton btnEqualizationBool;
    private JFileChooser chooserFileSave;
    private File fileSave;
    private JButton btnObjectDelete;
    private JButton btnEditProperties;
    private JToggleButton tglPathFinder;
    private SwingWorker<Void, Void> pathFindWorker;
    private JToggleButton tglAddPoints;
    private JToggleButton tglDelPoints;
    private JToggleButton tglSplitTrackseg;
    private JButton btnCorrectEle;
    private JButton btnEleChart;
    private JButton btnSpeedChart;
    private JButton btnDistChart;
    private JButton btnTimeChart;
    protected JComboBox<String> comboBoxTileSource;
    private JLabel lblLat;
    private JTextField textFieldLat;
    private JLabel lblLon;
    private JTextField textFieldLon;
    private JToggleButton tglLatLonFocus;
    private JSplitPane splitPaneMain;
    private JSplitPane splitPaneSidebar;
    private JPanel containerLeftSidebarTop;
    private JPanel containerExplorerHeading;
    private JLabel labelExplorerHeading;
    private JPanel gpxFilePanel;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private JTree tree;
    private DefaultMutableTreeNode currSelection;
    private JPanel containerLeftSidebarBottom;
    private JPanel containerPropertiesHeading;
    private JLabel labelPropertiesHeading;
    private JScrollPane scrollPaneProperties;
    private DefaultTableModel tableModelProperties;
    private JTable tableProperties;
    private SimpleDateFormat sdf;
    private JPanel panelRoutingOptions;
    private JLabel lblMapQuestFoot;
    private JLabel lblMapQuestBike;
    private JLabel lblYOURSFoot;
    private JLabel lblYOURSBike;
    private List<JLabel> lblsRoutingOptions;
    private JPanel panelRoutingCancel;
    private JLabel lblRoutingCancel;
    private Color transparentYellow;
    private Color transparentGrey;
    private Color transparentRed;
    private GPXPanel mapPanel;
    private Cursor mapCursor;
    private File fileOpened;
    private DefaultListModel<GPX> gpxFiles;
    private JList jList;
    TimeChart timeChart;
    private Map<WayPoint, WayPoint> wayPointMap = new TreeMap<>();
    List<WayPoint> a1 = new ArrayList<>();
    List<WayPoint> a2 = new ArrayList<>();
    private int gapPenalty = 10;
    private double equalizeDistance = 5;

    private boolean doEqualize = true;

    private String[] tfLabels;
    private List<TileFactory> factories;

    public GPXCompare()
    {
        init();
    }

    public void init()
    {

        gpxFiles = new DefaultListModel<>();
        createMainFrame();

        createMap();

        createSideBar();

        createToolBar();

        frame.setVisible(true);
    }

    private void createMainFrame()
    {
        lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        /*
        Main frame creation
         */

        frame = new JFrame("GPX compare");
        transparentGrey = new Color(160, 160, 160, 192);
        transparentYellow = new Color(177, 177, 25, 192);
        transparentRed = new Color(177, 25, 25, 192);

        /*
        Glass Pane creation
         */

        glassPane = new JPanel();
        glassPane.setOpaque(false);
        frame.setGlassPane(glassPane);
        glassPane.setLayout(new BorderLayout());
        glassPaneStatus = new JLabel();
        glassPaneStatus.setHorizontalAlignment(SwingConstants.CENTER);
        glassPaneStatus.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        glassPaneStatus.setOpaque(true);
        glassPaneStatus.setBackground(transparentYellow);
        glassPaneStatus.setForeground(Color.BLACK);
        glassPane.add(glassPaneStatus, BorderLayout.SOUTH);
        ImageIcon icon = new ImageIcon(getClass().getResource("/gpx-creator.png"));
        frame.setIconImage(icon.getImage());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        int frameWidth = (screenWidth * 2) / 3;
        int frameHeight = (screenHeight * 2) / 3;
        frame.setBounds(((screenWidth - frameWidth) / 2), ((screenHeight - frameHeight) / 2), frameWidth, frameHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*
        Split the main frame in 2 frames
        */

        splitPaneMain = new JSplitPane();
        splitPaneMain.setContinuousLayout(true);
        frame.getContentPane().add(splitPaneMain, BorderLayout.CENTER);
    }

    private void createMap()
    {
        /*
        Create the map viewer
         */

        mapPanel = new GPXPanel();
        TileFactoryInfo info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.HYBRID);
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        factories = new ArrayList<>();

        TileFactoryInfo osmInfo = new OSMTileFactoryInfo();
        VirtualEarthTileFactoryInfo veInfo1 = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.HYBRID);
        VirtualEarthTileFactoryInfo veInfo2 = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP);
        VirtualEarthTileFactoryInfo veInfo3 = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.SATELLITE);

        factories.add(new DefaultTileFactory(veInfo1));
        factories.add(new DefaultTileFactory(veInfo2));
        factories.add(new DefaultTileFactory(veInfo3));
        factories.add(new DefaultTileFactory(osmInfo));
        tfLabels = new String[factories.size()];
        for (int i = 0; i < factories.size(); i++)
        {
            if(factories.get(i).getInfo() instanceof VirtualEarthTileFactoryInfo)
            {
                VirtualEarthTileFactoryInfo veInfo = (VirtualEarthTileFactoryInfo) factories.get(i).getInfo();
                tfLabels[i] = veInfo.getName() + " " + veInfo.getModeName();
            }
            else
            {
                tfLabels[i] = factories.get(i).getInfo().getName() + i;
            }
        }
        mapPanel.setTileFactory(tileFactory);
        tileFactory.setThreadPoolSize(8);
        GeoPosition marije = new GeoPosition(52.247324, 6.849832);
        mapPanel.setZoom(2);
        mapPanel.setAddressLocation(marije);

        mapPanel.setLayout(new BoxLayout(mapPanel, BoxLayout.X_AXIS));
        mapPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        mapPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        splitPaneMain.setRightComponent(mapPanel);

        /*
        Add mouse events for the map viewer
         */

        MouseInputListener mia = new PanMouseInputListener(mapPanel);
        mapPanel.addMouseListener(mia);
        mapPanel.addMouseMotionListener(mia);
        mapPanel.addMouseListener(new CenterMapListener(mapPanel));
        mapPanel.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapPanel));
        mapPanel.addKeyListener(new PanKeyListener(mapPanel));
    }

    private void createSideBar()
    {
        /*
        Split the sidebar
         */
        splitPaneSidebar = new JSplitPane();
        splitPaneSidebar.setMinimumSize(new Dimension(240, 25));
        splitPaneSidebar.setPreferredSize(new Dimension(240, 25));
        splitPaneSidebar.setContinuousLayout(true);
        splitPaneSidebar.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneMain.setLeftComponent(splitPaneSidebar);
        splitPaneSidebar.setDividerLocation(210);

        /*
        Sidebar top container
         */
        containerLeftSidebarTop = new JPanel();
        containerLeftSidebarTop.setPreferredSize(new Dimension(10, 100));
        containerLeftSidebarTop.setAlignmentY(Component.TOP_ALIGNMENT);
        containerLeftSidebarTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerLeftSidebarTop.setLayout(new BoxLayout(containerLeftSidebarTop, BoxLayout.Y_AXIS));
        splitPaneSidebar.setTopComponent(containerLeftSidebarTop);
        /*
        Sidebar top container explorer heading container
         */
        containerExplorerHeading = new JPanel();
        containerExplorerHeading.setPreferredSize(new Dimension(10, 23));
        containerExplorerHeading.setMinimumSize(new Dimension(10, 23));
        containerExplorerHeading.setMaximumSize(new Dimension(32767, 23));
        containerExplorerHeading.setAlignmentY(Component.TOP_ALIGNMENT);
        containerExplorerHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerExplorerHeading.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        containerExplorerHeading.setLayout(new BoxLayout(containerExplorerHeading, BoxLayout.Y_AXIS));
        containerExplorerHeading.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 0, 1, new Color(0, 0, 0)), new EmptyBorder(2, 5, 5, 5)));
        containerLeftSidebarTop.add(containerExplorerHeading);
        /*
        Sidebar explorer heading container text
         */
        labelExplorerHeading = new JLabel("Explorer");
        labelExplorerHeading.setAlignmentY(Component.TOP_ALIGNMENT);
        labelExplorerHeading.setMaximumSize(new Dimension(32767, 14));
        labelExplorerHeading.setHorizontalTextPosition(SwingConstants.LEFT);
        labelExplorerHeading.setHorizontalAlignment(SwingConstants.LEFT);
        labelExplorerHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        containerExplorerHeading.add(labelExplorerHeading);

        /*
         GPX files panel
        */
        UIManager.put("ScrollBar.minimumThumbSize", new Dimension(16, 16)); // prevent Windows L&F scroll thumb bug
        gpxFilePanel = new JPanel();
        gpxFilePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        gpxFilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        gpxFilePanel.setBorder(new LineBorder(new Color(0, 0, 0)));
        containerLeftSidebarTop.add(gpxFilePanel);

        /*
        GPX files list
         */

        jList = new JList(gpxFiles);
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if(SwingUtilities.isRightMouseButton(e)) {
                    int row = jList.locationToIndex(e.getPoint());
                    jList.setSelectedIndex(row);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(SwingUtilities.isRightMouseButton(e))
                {
                    int row = jList.locationToIndex(e.getPoint());
                    jList.setSelectedIndex(row);
                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem menuItem = new JMenuItem("Delete");
                    menuItem.addActionListener(e1 -> {
                        GPX removeGPX = (GPX) jList.getSelectedValue();
                        gpxFiles.removeElement(removeGPX);
                        mapPanel.removeGPXFile(removeGPX);
                        if(gpxFiles.size() < 2)
                        {
                            resetMap();
                        }
                    });
                    popup.add(menuItem);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
                else if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
                {
                    GPX selectedGPX = (GPX) jList.getSelectedValue();
                    WayPoint w = selectedGPX.getTracks().get(0).getSegments().get(0).getPoints().get(0);
                    mapPanel.setCenter(mapPanel.getTileFactory().geoToPixel(new GeoPosition(w.getLatitude().toDegrees(), w.getLongitude().toDegrees()), mapPanel.getZoom()));
                }
            }
        };
        FileRenderer fr = new FileRenderer();
        jList.setCellRenderer(fr);
        jList.addMouseListener(mouseListener);
        gpxFilePanel.add(jList);

        /*
        Bottom sidebar container
         */
        containerLeftSidebarBottom = new JPanel();
        containerLeftSidebarBottom.setAlignmentY(Component.TOP_ALIGNMENT);
        containerLeftSidebarBottom.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerLeftSidebarBottom.setLayout(new BoxLayout(containerLeftSidebarBottom, BoxLayout.Y_AXIS));
        splitPaneSidebar.setBottomComponent(containerLeftSidebarBottom);

        /*
        Bottom sidebar properties
        */
        containerPropertiesHeading = new JPanel();
        containerPropertiesHeading.setMaximumSize(new Dimension(32767, 23));
        containerPropertiesHeading.setMinimumSize(new Dimension(10, 23));
        containerPropertiesHeading.setPreferredSize(new Dimension(10, 23));
        containerPropertiesHeading.setAlignmentY(Component.TOP_ALIGNMENT);
        containerPropertiesHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerPropertiesHeading.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        containerPropertiesHeading.setLayout(new BoxLayout(containerPropertiesHeading, BoxLayout.Y_AXIS));
        containerPropertiesHeading.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 0, 1, (Color) new Color(0, 0, 0)), new EmptyBorder(2, 5, 5, 5)));
        containerLeftSidebarBottom.add(containerPropertiesHeading);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

        /*
        Properties header
        */
        labelPropertiesHeading = new JLabel("Properties");
        labelPropertiesHeading.setMaximumSize(new Dimension(32767, 14));
        labelPropertiesHeading.setHorizontalTextPosition(SwingConstants.LEFT);
        labelPropertiesHeading.setHorizontalAlignment(SwingConstants.LEFT);
        labelPropertiesHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelPropertiesHeading.setAlignmentY(0.0f);
        containerPropertiesHeading.add(labelPropertiesHeading);

        /*
        Properties Table
        */
        tableModelProperties = new DefaultTableModel(new Object[]{"Name", "Value"}, 0);
        tableProperties = new JTable(tableModelProperties);
        tableProperties.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tableProperties.setAlignmentY(Component.TOP_ALIGNMENT);
        tableProperties.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableProperties.setBorder(new EmptyBorder(0, 0, 0, 0));
        tableProperties.setFillsViewportHeight(true);
        tableProperties.setTableHeader(null);
        tableProperties.setEnabled(false);
        tableProperties.getColumnModel().setColumnMargin(0);

        /*
        Properties table scrolling
        */
        scrollPaneProperties = new JScrollPane(tableProperties);
        scrollPaneProperties.setAlignmentY(Component.TOP_ALIGNMENT);
        scrollPaneProperties.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPaneProperties.setBorder(new LineBorder(new Color(0, 0, 0)));
        containerLeftSidebarBottom.add(scrollPaneProperties);

    }

    private void createToolBar()
    {
        /*
        Toolbar
        */
        toolBarMain = new JToolBar();
        toolBarMain.setLayout(new BoxLayout(toolBarMain, BoxLayout.X_AXIS));
        toolBarMain.setFloatable(false);
        toolBarMain.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        frame.getContentPane().add(toolBarMain, BorderLayout.NORTH);

        /*
        Open File Button
        */
        int chooserWidth = (frame.getWidth() * 8) / 10;
        int chooserHeight = (frame.getHeight() * 8) / 10;
        chooserWidth = Math.min(864, chooserWidth);
        chooserHeight = Math.min(539, chooserHeight);

        FileNameExtensionFilter gpxFilter = new FileNameExtensionFilter("GPX files (*.gpx)", "gpx");
        btnFileOpen = new JButton("");
        chooserFileOpen = new JFileChooser() {
            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                JDialog dialog = super.createDialog(parent);
                dialog.setIconImage(new ImageIcon(getClass().getResource("/file-open.png")).getImage());
                return dialog;
            }
        };
        chooserFileOpen.setCurrentDirectory(
                new File("C:\\Users\\Jelle\\Projects\\GPSCompare")); // TODO change this before deployment
        chooserFileOpen.addChoosableFileFilter(gpxFilter);
        chooserFileOpen.setFileFilter(gpxFilter);
        chooserFileOpen.setPreferredSize(new Dimension(chooserWidth, chooserHeight));
        btnFileOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });
        btnFileOpen.setToolTipText("<html>Open GPX file</html>");
        btnFileOpen.setFocusable(false);
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/file-open.png"));
        btnFileOpen.setIcon(imageIcon);
        btnFileOpen.setDisabledIcon(
                new ImageIcon(getClass().getResource("/file-open-disabled.png")));
        toolBarMain.add(btnFileOpen);

        /* Time Chart Button
         * --------------------------------------------------------------------------------------------------------- */
        btnTimeChart = new JButton("");
        btnTimeChart.setToolTipText("View time difference");
        btnTimeChart.setIcon(new ImageIcon(getClass().getResource("/elevation-chart.png")));
        btnTimeChart.setEnabled(false);
        btnTimeChart.setDisabledIcon(new ImageIcon(getClass().getResource("/elevation-chart-disabled.png")));
        btnTimeChart.setFocusable(false);
        btnTimeChart.addActionListener(e -> buildChart("/com/gpxcreator/icons/elevation-chart.png"));
        toolBarMain.add(btnTimeChart);

        JComboBox combo = new JComboBox(tfLabels);
        combo.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                TileFactory factory = factories.get(combo.getSelectedIndex());
                TileFactoryInfo info = factory.getInfo();
                mapPanel.setTileFactory(factory);
            }
        });
        toolBarMain.add(Box.createGlue());
        toolBarMain.add(combo);

        /* Equalization settings Button
         * --------------------------------------------------------------------------------------------------------- */
        btnEqualization = new JButton("Equalization");
        btnEqualization.setToolTipText("Change Equalization Setting");
        btnEqualization.setIcon(new ImageIcon(getClass().getResource("/equalization.png")));
        btnEqualization.setFocusable(false);
        btnEqualization.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeEqualization();
            }
        });
        toolBarMain.add(Box.createGlue());
        toolBarMain.add(btnEqualization);

        /* GapPenalty settings Button
         * --------------------------------------------------------------------------------------------------------- */
        btnGapPenalty = new JButton("Gap Penalty");
        btnGapPenalty.setToolTipText("Change Gap Penalty Setting");
        btnGapPenalty.setIcon(new ImageIcon(getClass().getResource("/settings.png")));
        btnGapPenalty.setFocusable(false);
        btnGapPenalty.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeGapPenalty();
            }
        });
        toolBarMain.add(btnGapPenalty);

        /* Equalization On/Off settings Button
         * --------------------------------------------------------------------------------------------------------- */
        btnEqualizationBool = new JButton("Equalization On/Off");
        btnEqualizationBool.setToolTipText("Equalization On/Off");
        btnEqualizationBool.setIcon(new ImageIcon(getClass().getResource("/equalization-bool.png")));
        btnEqualizationBool.setFocusable(false);
        btnEqualizationBool.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeEqualizationBool();
            }
        });
        toolBarMain.add(btnEqualizationBool);
    }

    private void changeEqualizationBool()
    {
        int equalize = JOptionPane.showConfirmDialog(null,"<html>By default all points on the tracks are equalized over the whole track." +
                "This means that every n meters there will be a point for both tracks, This makes matching a lot cleaner.<br> If you want to " +
                "turn this off, select 'No'.</html>","Do equalize?",JOptionPane.YES_NO_OPTION);
        if(equalize == JOptionPane.YES_OPTION)
        {
            doEqualize = true;
            btnEqualizationBool.setIcon(new ImageIcon(getClass().getResource("/equalization-bool.png")));
        }
        else if(equalize == JOptionPane.NO_OPTION)
        {
            doEqualize = false;
            btnEqualizationBool.setIcon(new ImageIcon(getClass().getResource("/equalization-bool-disabled.png")));
        }
        rebuildFiles();
    }

    private void changeGapPenalty()
    {
        String eqDist = JOptionPane.showInputDialog(null,"<html>Here you can change the gap penalty used in the Needleman-Wunsch algorithm. " +
                "By default this is set to 10. A higher number means that points further apart will be considered aligned, that is, they are at the same place.<br>" +
                "If you have issues with your alignment, try changing this number. A good value is in the same ballpark as the accuracy of your GPS" +
                ", usually a bit higher.</html>","Change gap penalty",JOptionPane.INFORMATION_MESSAGE);
        try {
            gapPenalty = Integer.parseInt(eqDist);
            if(gapPenalty <= 0)
            {
                gapPenalty = 10;
                JOptionPane.showMessageDialog(frame,
                        "<html>Numbers of 0 or lower don't work, please try again.</html>",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
        catch (NullPointerException ignored)
        {

        }
        catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(frame,
                    "<html>Not a number, please try again.</html>",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
        rebuildFiles();
    }

    private void changeEqualization()
    {
        String eqDist = JOptionPane.showInputDialog(null,"<html>By default all trackpoints are equalized. This means " +
                "that every n meters a point will be placed. This helps make the alignment of the two tracks less noisy." +
                "<br>A higher number means that fewer points will be placed on the map, your track will be less accurate. " +
                "A lower number means more points, but also more computation time. <br><i>This settings has no effect if you've turned equalization off!</i></html>","Change equalization distance",JOptionPane.INFORMATION_MESSAGE);
        try {
            equalizeDistance = Double.parseDouble(eqDist);
            if(equalizeDistance <= 0)
            {
                equalizeDistance = 10;
                JOptionPane.showMessageDialog(frame,
                        "<html>Numbers of 0 or lower don't work, please try again.</html>",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
        catch (NullPointerException ignored)
        {

        }
        catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(frame,
                    "<html>Not a number, please try again.</html>",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
        rebuildFiles();
    }

    private void rebuildFiles()
    {
        gpxFiles.clear();
        resetMap();
    }

    private void resetMap()
    {
        mapPanel.setCompareLists(null,null);
        btnTimeChart.setEnabled(false);
        btnTimeChart.validate();
        btnTimeChart.repaint();
        btnFileOpen.setEnabled(true);
        btnFileOpen.validate();
        btnFileOpen.repaint();
        mapPanel.person1WayPoint = null;
        mapPanel.person2WayPoint = null;
        if(timeChart != null) timeChart.setVisible(false);
    }

    /**
     * Builds the selected chart type and displays the new window frame.
     */
    public void buildChart(String iconPath) {
            timeChart = new TimeChart(a1, a2, mapPanel);
            InputStream in = GPXCompare.class.getResourceAsStream(iconPath);
            if (in != null) {
                timeChart.setIconImage(new ImageIcon(iconPath).getImage());
            }
            timeChart.setSize(frame.getWidth() - 150, frame.getHeight() - 100);
            timeChart.setLocationRelativeTo(frame);
            timeChart.setVisible(true);
    }

    private void openFile()
    {
        int returnVal = chooserFileOpen.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileOpened = chooserFileOpen.getSelectedFile();
            if(FilenameUtils.getExtension(fileOpened.getAbsolutePath()).equals("gpx")) {
                parseFile(fileOpened);
            }
            else
            {
                JOptionPane.showMessageDialog(frame,
                        "<html>The selected file is not a .gpx file or it has been corrupted, please try again.</html>",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
        if(gpxFiles.size() == 2)
        {
            btnFileOpen.setEnabled(false);
            btnTimeChart.setEnabled(true);
        }
    }

    private void parseFile(File fileOpened)
    {
        GPX gpx = null;
        try {
            gpx = GPX.read(fileOpened.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(doEqualize)
        {
            gpx = equalizeWaypoints(gpx);
        }
        gpx.setName(fileOpened.getName());
        mapPanel.addGPXFile(gpx);
        gpxFiles.addElement(gpx);
        List<Track> tracks = gpx.getTracks();
        List<TrackSegment> segments = tracks.get(0).getSegments();
        TrackSegment trackSegment = segments.get(0);
        List<WayPoint> segmentPoints = trackSegment.getPoints();
        List<GeoPosition> positions = new ArrayList<>();
        for(WayPoint w : segmentPoints)
        {
            positions.add(new GeoPosition(w.getLatitude().toDegrees(), w.getLongitude().toDegrees()));
        }
        mapPanel.zoomToBestFit(new HashSet<>(positions), 1.0);
        if(gpxFiles.size() == 2)
        {
            matchTracks();
        }
    }

    private void matchTracks()
    {
        GPX gpx1 = gpxFiles.get(0);
        GPX gpx2 = gpxFiles.get(1);
        List<WayPoint> waypoints1 = gpx1.getTracks().get(0).getSegments().get(0).getPoints();
        List<WayPoint> waypoints2 = gpx2.getTracks().get(0).getSegments().get(0).getPoints();
        alignTracks(waypoints1, waypoints2,-gapPenalty);
    }

    private void alignTracks(List<WayPoint> wayPoints1, List<WayPoint> wayPoints2, int gap_penalty)
    {
        int[][] f = new int[wayPoints1.size()][wayPoints2.size()];
        for(int i = 0; i < wayPoints1.size();i++)
        {
            f[i][0] = gap_penalty * i;
        }

        for(int i = 0; i < wayPoints2.size();i++)
        {
            f[0][i] = gap_penalty * i;
        }

        for(int i = 1; i < wayPoints1.size();i++)
        {
            WayPoint t1 = wayPoints1.get(i);
            for(int j = 1; j < wayPoints2.size();j++)
            {
                WayPoint t2 = wayPoints2.get(j);
                int match = f[i - 1][j - 1] + matchDistance(t1,t2);
                int delete = f[i - 1][j] + gap_penalty;
                int insert = f[i][j - 1] + gap_penalty;
                f[i][j] = Math.max(match, Math.max(delete, insert));
            }
        }

        a1 = new ArrayList<>();
        a2 = new ArrayList<>();
        int i = wayPoints1.size() -1;
        int j = wayPoints2.size() -1;
        boolean overlap = false;

        while (i > 0 || j > 0)
        {
            if(i > 0 && j > 0 && f[i][j] == f[i-1][j-1] + matchDistance(wayPoints1.get(i), wayPoints2.get(j)))
            {
                overlap = true;
                a1.add(0,wayPoints1.get(i));
                a2.add(0, wayPoints2.get(j));
                i--;
                j--;
            }
            else if( i > 0 && f[i][j] == f[i-1][j] + gap_penalty)
            {
                a1.add(0,wayPoints1.get(i));
                a2.add(0, null);
                i--;
            }
            else if(j > 0 && f[i][j] == f[i][j-1] + gap_penalty)
            {
                a1.add(0, null);
                a2.add(0, wayPoints2.get(j));
                j--;
            }
        }
        if(!overlap)
        {
            JOptionPane.showMessageDialog(frame,
                    "<html><h1>No alignment has been found between these 2 files.</h1> Are you sure they overlap? <br> As a result nothing has been drawn on the map and " +
                            "the files have been unloaded. Please try again.</html>",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            mapPanel.removeGPXFile(gpxFiles.get(0));
            mapPanel.removeGPXFile(gpxFiles.get(1));
            gpxFiles.clear();
        }
        else
        {
            mapPanel.setCompareLists(a1,a2);
        }

    }

    private int matchDistance(WayPoint t1, WayPoint t2)
    {
        double coef = Math.cos(t1.getLatitude().toRadians());
        double x = t1.getLatitude().toDegrees() - t2.getLatitude().toDegrees();
        double y = (t1.getLongitude().toDegrees() - t2.getLongitude().toDegrees()) * coef;
        double distance = Math.sqrt(x * x + y * y) * (2 * Math.PI * 6378.137 * 1000) / 360;
        return (int) -distance;
    }

    private GPX equalizeWaypoints(GPX gpx)
    {
        List<WayPoint> wayPoints = gpx.getTracks().get(0).getSegments().get(0).getPoints();
        List<WayPoint> disPoints = new ArrayList<>();
        double d = 0;
        int i = 0;
        WayPoint p1;
        WayPoint p2;

        int j = 1;
        while (i < wayPoints.size()) {
            if (i == 0) {
                disPoints.add(wayPoints.get(i));
                i++;
                continue;
            }
            if (d == 0) {
                p1 = disPoints.get(disPoints.size() - 1);
            } else {
                p1 = wayPoints.get(i - 1);
            }
            p2 = wayPoints.get(i);
            double coef = Math.cos(p1.getLatitude().toRadians());
            double x = p1.getLatitude().toDegrees() - p2.getLatitude().toDegrees();
            double y = (p1.getLongitude().toDegrees() - p2.getLongitude().toDegrees()) * coef;
            d += Math.sqrt(x*x+y*y)*(2*Math.PI*6378.137 * 1000)/360;

            if (d >= equalizeDistance) {
                double bearing = calculateBearing(p1, p2);
                double moves = d/equalizeDistance;
                Optional<ZonedDateTime> p1Optional = p1.getTime();
                Optional<ZonedDateTime> p2Optional = p2.getTime();
                long p1Time = p1Optional.get().toEpochSecond();
                long p2Time = p2Optional.get().toEpochSecond();
                long timeDiff = p2Time - p1Time;
                double timeStep = (timeDiff/moves);
                WayPoint p2_copy = p2.toBuilder().build();
                p2_copy = moveWaypoint(-(d-equalizeDistance),bearing,p2_copy);
                long newTime = (long) (p1Time + (timeStep * j));
                ZonedDateTime newZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(newTime), p1Optional.get().getZone());
                p2_copy = p2_copy.toBuilder().time(newZonedDateTime).build();
                disPoints.add(p2_copy);
                d = 0;
                j++;
            } else {
                i++;
                j = 1;
            }
        }


        GPX newGPX = GPX.builder().addTrack(track ->
            track.addSegment(segment -> {
                for(WayPoint w : disPoints)
                {
                    segment.addPoint(w);
                }
            })
        ).build();

        return newGPX;
    }

    public WayPoint moveWaypoint(double distance, double angle, WayPoint wayPoint)
    {
        double earthDegree = (2*Math.PI*6378.137*1000) / 360;
        double coef = Math.cos(wayPoint.getLatitude().toRadians());
        double verticalDistDiff = Math.sin(Math.toRadians(90 - angle)) / earthDegree;
        double horizontalDistDiff = Math.cos(Math.toRadians(90 - angle)) / earthDegree;
        double latDiff = distance * verticalDistDiff;
        double lonDiff = distance * horizontalDistDiff / coef;
        WayPoint newWaypoint = wayPoint.toBuilder().lat(wayPoint.getLatitude().toDegrees()+latDiff).lon(wayPoint.getLongitude().toDegrees()+lonDiff).build();
        return newWaypoint;
    }

    public double calculateBearing(WayPoint p1, WayPoint p2)
    {
        double lat1R = p1.getLatitude().toRadians();
        double lat2R = p2.getLatitude().toRadians();
        double dlon = p2.getLongitude().toRadians() - p1.getLongitude().toRadians();
        double y = Math.sin(dlon) * Math.cos(lat2R);
        double x = Math.cos(lat1R) * Math.sin(lat2R) - Math.sin(lat1R) * Math.cos(lat2R) * Math.cos(dlon);
        return Math.toDegrees(Math.atan2(y,x));
    }

    public void setGapPenalty(int gapPenalty) {
        this.gapPenalty = gapPenalty;
    }

    public void setEqualizeDistance(double equalizeDistance) {
        this.equalizeDistance = equalizeDistance;
    }

    public void setDoEqualize(boolean doEqualize) {
        this.doEqualize = doEqualize;
    }
}

