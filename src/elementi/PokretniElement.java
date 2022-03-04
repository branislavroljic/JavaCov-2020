package elementi;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import elementi.zdravstveniObjekti.AmbulantnoVozilo;
import simulacija.Mapa;
import simulacija.Simulacija;

public abstract class PokretniElement extends Element{

	private static final long serialVersionUID = 1L;
	protected int brzinaKretanja;
	//granice unutar kojih se moze kretati objekat
	protected  int X_GORNJA_GRANICA;
	protected  int Y_GORNJA_GRANICA;
	protected  int X_DONJA_GRANICA;
	protected  int Y_DONJA_GRANICA;
	
	//moguci pravci kretanja za osobu
	public enum Pravac{
		//U_MJESTU, ako osoba odluci da ne ide nigdje, ili ne moze ici
		U_MJESTU(0,0),
		GORE(-1, 0) ,
		LIJEVO (0, -1),
		DESNO(0, 1), 
		DOLJE (1, 0),
		GORE_DESNO (-1 , 1),
		GORE_LIJEVO (-1, -1),
		DOLJE_DESNO (1, 1),
		DOLJE_LIJEVO (1, -1);
		private final int x;
		private final int y;
		
		private Pravac(int x ,int y) {
			this.x = x;
			this.y = y;
		}
		
		 public int getX() { return x; }
		 public int getY() { return y; }

	};

	public PokretniElement(int x, int y, Mapa mapa) {
		super(x, y, mapa);
		this.brzinaKretanja = new Random().nextInt(1000) + 1000;
	}

	public PokretniElement(int x, int y, int brzinaKretanja, Mapa mapa) {
		super(x, y, mapa);
		this.brzinaKretanja = brzinaKretanja;
	}
	
	public int getBrzinaKretanja() {
		return brzinaKretanja;
	}

	/*
	 * podesavanje granica u kojima se objekat moze kretati
	 */
	public abstract void podesiGranice();
	
	/*
	 * objekat se krece po putanji koja je proslijedjena kao parametar
	 * koristi se prilikom kretanja osoba kuci odnosno ambulantnih vozila do zarazenih osoba i nazad
	 */
	public void kretanjePoPutanji(List<Polje> putanja) {

		Polje prethodno = null;
		for (Polje trenutno : putanja) {
			while (Simulacija.isZaustavljena()) {
				synchronized (Simulacija.lock) {
					try {
						Simulacija.lock.wait();
					} catch (InterruptedException e) {
						Logger.getLogger(AmbulantnoVozilo.class.getName()).log(Level.SEVERE, null, e);
					}
				}
			}
			if (prethodno != null) {
				prethodno.setElement(null);
			}
			trenutno.setElement(this);
			prethodno = trenutno;

			x = trenutno.getX();
			y = trenutno.getY();

			Simulacija.glavnaForma.izmijenjenaMatrica(this.toString());
			try {
				Thread.sleep(brzinaKretanja);
			} catch (InterruptedException e) {
				Logger.getLogger(AmbulantnoVozilo.class.getName()).log(Level.SEVERE, null, e);
			}
		}
		if (prethodno != null) {
			prethodno.setElement(null);
		}
	}
}


