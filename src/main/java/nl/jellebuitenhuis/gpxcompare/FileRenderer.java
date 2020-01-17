package nl.jellebuitenhuis.gpxcompare;

import io.jenetics.jpx.GPX;
import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class FileRenderer implements ListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        GPX gpx = null;
        if(value instanceof GPX)
        {
            gpx = (GPX) value;
        }
        String text = String.format("'%s'. Waypoints: %d",gpx.getName(),gpx.getTracks().get(0).getSegments().get(0).getPoints().size());
        JTextField jTextField = new JTextField(text);
        if(cellHasFocus)
        {
            jTextField.setBackground(Color.CYAN);
        }
        else {
            jTextField.setBackground(Color.WHITE);
        }
        return jTextField;
    }
}
