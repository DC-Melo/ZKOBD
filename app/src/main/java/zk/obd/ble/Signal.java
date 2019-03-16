package zk.obd.ble;

public class Signal {
    private byte[] canTX=new byte[20];
    private int Tx_Len=1;
    private byte[] canRX= new byte[20];
    private int Rx_Len=0;
    private boolean isASC=false;//0-int,1-ASC
    private String VIN="未读到VIN号";
    private int startbyte;
    private int startbit;
    private int length;
    private String unit;
    private double factor=1;
    private double offset=0;
    private double signalvalue=0;


    public double getSignalvalue() {
        long num = 0;
        for (int ix = 0; ix < Rx_Len; ++ix) {
            num <<= 8;
            num |= (canRX[ix] & 0xff);
        }
/*        num<<=((startbyte-1)*8+startbit);
        num>>=(64-length);*/
        signalvalue=(double)num*factor+offset;
        return signalvalue;
    }

    public String getVIN() {
        String RxVIN = new String(canRX);
        if(Rx_Len>0) VIN=RxVIN.substring(0, Rx_Len);
        else VIN="未读到VIN号";
        //VIN=RxVIN.substring(7, 14)+RxVIN.substring(7+18, 14+18)+RxVIN.substring(7+18*2, 14+18*2)+RxVIN.substring(7+18*3, 14+18*3);
        return VIN;
    }

    public byte[] getCanTX() {
        byte[] can_tx = new byte[Tx_Len];
        System.arraycopy(canTX, 0, can_tx, 0, Tx_Len);
        return can_tx;
    }
    public int getRx_Len() {
        return Rx_Len;
    }

    public void setRx_Len(int rx_Len) {
        Rx_Len = rx_Len;
    }
    public void setCanTX(byte[] canTX) {
        this.canTX = canTX;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public byte[] getCanRX() {
        return canRX;
    }

    public void setCanRX(byte[] canRX) {
        System.arraycopy( this.canRX, 0,canRX, 0, Rx_Len);
    }

    public boolean isASC() {
        return isASC;
    }

    public void setASC(boolean ASC) {
        isASC = ASC;
    }

    public int getStartbyte() {
        return startbyte;
    }

    public void setStartbyte(int startbyte) {
        this.startbyte = startbyte;
    }

    public int getStartbit() {
        return startbit;
    }

    public void setStartbit(int startbit) {
        this.startbit = startbit;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

}
