package util;

import java.io.Serializable;

public class Alarm implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private int x;
	private int y;
	private int IDkuce;
	
	public Alarm(int x, int y, int ID) {
		this.x =x;
		this.y = y;
		this.IDkuce = ID;
	}
		
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getIDkuce() {
		return IDkuce;
	}
	public void setIDkuce(int iDkuce) {
		IDkuce = iDkuce;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null) {
			Alarm alarm = (Alarm)obj;
			return( this.x == alarm.getX() && this.y == alarm.getY() && this.getIDkuce() == alarm.getIDkuce());
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "ZARAZENA osoba na poziciji (" + x + "," + y + ") , ID kuce: "+ IDkuce ;
	}
}
