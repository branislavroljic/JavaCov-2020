package elementi.osobe;

import elementi.OgranicenoPokretan;
import simulacija.Mapa;

public class Stari extends Osoba implements OgranicenoPokretan{
	
	private static final long serialVersionUID = 1L;
	private static final int OPSEG_KRETANJA = 3;
	private int opsegKretanja;
	
	public Stari(int x, int y, int kucaID, Mapa mapa){
		super(x, y, "Sime" + rand.nextInt(100) ,
				"Sprezime" + rand.nextInt(100),
				rand.nextInt(55) + 1900,
				Pol.randomPol(), kucaID, mapa);
		podesiOpsegKretanja();
		podesiGranice();
	}
	@Override
	public void podesiOpsegKretanja() {
		opsegKretanja = OPSEG_KRETANJA;
	}
	
	@Override
	public void podesiGranice() {
		
		if (x < opsegKretanja) {
			X_GORNJA_GRANICA = 2 * opsegKretanja + 1;
			X_DONJA_GRANICA = mapa.DONJA_GRANICA;
		} else if (mapa.VELICINA - 1 - x < opsegKretanja) {
			X_GORNJA_GRANICA = mapa.VELICINA;
			X_DONJA_GRANICA = mapa.VELICINA - 2 * opsegKretanja - 1;
		} else {
			X_GORNJA_GRANICA = x + opsegKretanja + 1;
			X_DONJA_GRANICA = x - opsegKretanja;
		}
		if (y < opsegKretanja) {
			Y_GORNJA_GRANICA = 2 * opsegKretanja + 1;
			Y_DONJA_GRANICA = mapa.DONJA_GRANICA;
		} else if (mapa.VELICINA - 1 - y < opsegKretanja) {
			Y_GORNJA_GRANICA = mapa.VELICINA;
			Y_DONJA_GRANICA = mapa.VELICINA - 2 * opsegKretanja - 1;
		} else {
			Y_GORNJA_GRANICA = y + opsegKretanja + 1;
			Y_DONJA_GRANICA = y - opsegKretanja;
		}
	}
}

