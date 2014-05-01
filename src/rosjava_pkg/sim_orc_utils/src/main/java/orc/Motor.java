package orc;

import java.io.*;

public class Motor
{
    Orc orc;
    int port;
    boolean invert;
    
    public Motor(final Orc orc, final int port, final boolean invert) {
        super();
        this.orc = orc;
        this.port = port;
        this.invert = invert;
    }
    
    public void setPWM(final double v) {
        this.orc.setMotorVel(this.port, v);
    }
    
    public double getPWM(final OrcStatus status) {
        return this.orc.getMotorVel(this.port);
    }
    
    public double getPWM() {
        return this.getPWM(this.orc.getStatus());
    }
}
