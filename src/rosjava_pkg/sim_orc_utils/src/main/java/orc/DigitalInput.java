package orc;

public class DigitalInput
{
    Orc orc;
    int port;
    boolean invert;
    
    public DigitalInput(final Orc orc, final int port, final boolean pullup, final boolean invert) {
        super();
        this.orc = orc;
        this.port = port;
        this.invert = invert;
    }
    
    public boolean getValue() {
        return getValue(this.orc.getStatus());
    }

    public boolean getValue(final OrcStatus status) {
        return false;
    }
}
