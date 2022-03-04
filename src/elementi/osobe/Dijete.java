package elementi.osobe;

import simulacija.Mapa;

public class Dijete extends Osoba {
	
	private static final long serialVersionUID = 1L;

	public Dijete(int x, int y, int kucaID, Mapa mapa){
		super(x, y, "Dime" + rand.nextInt(100),
				"Dprezime" + rand.nextInt(100),
				rand.nextInt(18) + 2002,
				Pol.randomPol(), kucaID, mapa);
		 podesiGranice();
	}
	
	@Override
	public void podesiGranice() {

		X_GORNJA_GRANICA = mapa.VELICINA;
		X_DONJA_GRANICA = mapa.DONJA_GRANICA;
		Y_GORNJA_GRANICA = mapa.VELICINA;
		Y_DONJA_GRANICA = mapa.DONJA_GRANICA; 
	}
}
