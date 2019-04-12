package zk.obd.been;

import android.widget.ScrollView;

public class CarInformationBean {
/*    private String carName;
    private String carInformation;*/

    private String signalname;
    private String signallable;
    private byte[] canTX=new byte[20];
    private int Tx_Len=1;
    private byte[] canRX= new byte[20];
    private int Rx_Len=0;
    private boolean isASC=false;//0-int,1-ASC
    private String ASCValue="";
    private int startbyte;
    private int startbit;
    private int length;
    private String unit;
    private double factor=1;
    private double offset=0;
    private double signalvalue=0;
    private int sendtimes=0;
    private boolean active=false;
    private boolean received=false;

    public CarInformationBean(String signalname,String signallable)
    {
        this.signalname=signalname;
        this.signallable=signallable;
    }

    public int getSendtimes() {
        return sendtimes;
    }

    public void setSendtimes(int sendtimes) {
        this.sendtimes = sendtimes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }
/*    public String getCarName() {
        return carName;
    }

    public String getCarInformation() {
        return carInformation;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public void setCarInformation(String carInformation) {
        this.carInformation = carInformation;
    }*/

    public String getSignalname() {
        return signalname;
    }
    public void setSignalname(String signalname) {
        this.signalname = signalname;
    }

    public String getSignallable() {
        return signallable;
    }

    public void setSignallable(String signallable) {
        this.signallable = signallable;
    }

    public String getSignalvalue() {
        long num = 0;
        for (int ix = 0; ix < Rx_Len; ++ix) {
            num <<= 8;
            num |= (canRX[ix] & 0xff);
        }
/*        num<<=((startbyte-1)*8+startbit);
        num>>=(64-length);*/
        signalvalue=(double)num*factor+offset;

        return String.valueOf(signalvalue);
    }

    public String getASCValue() {
        String RxASCValue = new String(canRX);
        if(Rx_Len>0) ASCValue=RxASCValue.substring(0, Rx_Len);
        else ASCValue="--";
        //VIN=RxVIN.substring(7, 14)+RxVIN.substring(7+18, 14+18)+RxVIN.substring(7+18*2, 14+18*2)+RxVIN.substring(7+18*3, 14+18*3);
        return ASCValue;
    }
    public void setASCValue(String ASCValue) {
        this.ASCValue=ASCValue;
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
        System.arraycopy( canRX, 0,this.canRX, 0, Rx_Len);
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
