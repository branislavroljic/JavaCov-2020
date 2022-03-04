package elementi;

import java.io.Serializable;

public class Polje implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Element element = null;
	private int x;
	private int y;
	
	public Polje(int x , int y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public Polje(Element element) {
		this.element = element;
	}
	
	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Polje) {
			Polje temp  = (Polje)obj;
			return x == temp.getX() && y == temp.getY();
		}
		return false;
	}
}
