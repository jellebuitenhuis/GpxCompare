package com.gpxcreator.gpxpanel;

import java.util.Date;

/**
 * The GPX "wpt" element.
 *
 * @author Matt Hoover
 */
public class Waypoint implements Comparable<Waypoint>{

  private double lat;
  private double lon;
  private double ele;
  private Date time;
  protected String name;
  protected String desc;
  protected String type;

  /**
   * Constructs a {@link Waypoint}.
   *
   * @param lat Latitude.
   * @param lon Longitude.
   */
  public Waypoint(double lat, double lon) {
    this.lat = lat;
    this.lon = lon;
    this.time = null;
    this.name = "";
    this.desc = "";
    this.type = "";
  }

  @Override
  public String  toString() {
    return "Waypoint{" +
            "lat=" + lat +
            ", lon=" + lon +
            ", ele=" + ele +
            ", time=" + time +
            ", name='" + name + '\'' +
            ", desc='" + desc + '\'' +
            ", type='" + type + '\'' +
            '}';
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public double getEle() {
    return ele;
  }

  public void setEle(double ele) {
        /*if (ele == -32768 && this.ele != 0) { // if SRTM data is missing, and GPS logged data exists, leave it as is
            return;
        }*/
    this.ele = ele;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public int compareTo(Waypoint o) {
    if(time.before(o.getTime()))
    {
      return -1;
    }
    else if(time.after(o.getTime()))
    {
      return 1;
    }
    return 0;
  }
}
