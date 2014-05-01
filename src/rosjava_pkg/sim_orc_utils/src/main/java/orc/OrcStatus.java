package orc;

import java.io.*;
import map.PolygonMap;

public class OrcStatus
{
    public Orc orc;
    public double[] sonarReadings;
    public int[] qeiPosition = new int[2];
    public double[] qeiVelocity = new double[2];
    public long utimeOrc;
    public OrcStatus(Orc orc, PolygonMap map, double x, double y, double theta) {
        this.orc = orc;
        this.sonarReadings = map.predictSonars(x, y, theta);
        qeiPosition[0] = orc.getEncoderPos(0);
        qeiPosition[1] = orc.getEncoderPos(1);
        qeiVelocity[0] = orc.getEncoderVel(0);
        qeiVelocity[1] = orc.getEncoderVel(1);
        utimeOrc = orc.getUTime();
    }
}
