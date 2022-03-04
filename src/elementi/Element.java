package elementi;

import java.io.Serializable;

import simulacija.Mapa;

public abstract class Element implements Serializable{
	
	private static final long serialVersionUID = 1L;
	protected int x;
	protected int y;
	protected Mapa mapa;
	
	Element(int x, int y, Mapa mapa){
		this.x = x;
		this.y = y;
		this.mapa = mapa;
	}	
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	
	@Override
	public String toString () {
			return  " ( " + x + ", " + y +" )";
	}
}
