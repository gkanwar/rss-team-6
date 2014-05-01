package orc;

public class SRF02
{
    Orc orc;
    int i2caddr;
    int index;
    static final int DEFAULT_I2C_ADDR = 112;
    static double SOUND_METERS_PER_SEC;

    final int[] sonarAddrs = {0x70, 0x72, 0x74, 0x76};
    
    public SRF02(final Orc orc, final int i2caddr) {
        super();
        this.orc = orc;
        this.i2caddr = i2caddr;
        this.index = -1;
        for (int i = 0; i < sonarAddrs.length; i++) {
            if (sonarAddrs[i] == i2caddr) {
                this.index = i;
                break;
            }
        }
        if (index == -1) {
            throw new RuntimeException("Invalid i2caddr: " + i2caddr);
        }
    }
    
    public SRF02(final Orc orc) {
        this(orc, 112);
    }
    
    public void ping() {
        // Nothing
    }
    
    public double readTime() {
        return 0.0;
    }
    
    public double readRange() {
        final OrcStatus status = orc.getStatus();
        return status.sonarReadings[index];
    }
    
    public double measure() {
        synchronized(orc) {
            this.ping();
        }
        try {
            Thread.sleep(70L);
        }
        catch (InterruptedException ex) {}
        synchronized(orc) {
            return this.readRange();
        }
    }
    
    static {
        SRF02.SOUND_METERS_PER_SEC = 343.0;
    }
}
