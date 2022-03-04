package elementi.osobe;

import elementi.OgranicenoPokretan;
import simulacija.Mapa;

public class Odrasli extends Osoba implements OgranicenoPokretan{
	
	private static final long serialVersionUID = 1L;
	private int opsegKretanja;
	
	public Odrasli(int x, int y, int kucaID, Mapa mapa){
		super(x, y, "Oime" + rand.nextInt(100) ,
				"Oprezime" + rand.nextInt(100),
				rand.nextInt(47) + 1955,
				Pol.randomPol(), kucaID, mapa);
		podesiOpsegKretanja();
		podesiGranice();
	}

	@Override
	public void podesiOpsegKretanja() {
		opsegKretanja = (int) (mapa.VELICINA * (25 / 100.0f));
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
