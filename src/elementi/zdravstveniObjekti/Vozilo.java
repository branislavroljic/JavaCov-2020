package elementi.zdravstveniObjekti;

import elementi.PokretniElement;
import simulacija.Mapa;

public abstract class Vozilo extends PokretniElement{
	
	private static final long serialVersionUID = 1L;
	private boolean uPokretu = false;
	private boolean active = true;
	
	public Vozilo(int x, int y, int brzinaKretanja, Mapa mapa) {
		super(x,y, brzinaKretanja, mapa);
	}

	public boolean isuPokretu() {
		return uPokretu;
	}

	public void setuPokretu(boolean uPokretu) {
		this.uPokretu = uPokretu;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
