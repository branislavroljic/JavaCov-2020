package elementi.zdravstveniObjekti;

import elementi.NepokretniElement;
import elementi.osobe.Osoba;
import simulacija.Mapa;

public class KontrolniPunkt extends NepokretniElement{
	
	private static final long serialVersionUID = 1L;

	public static final double GRANICA_TEMPERATURE = 37.0;
	
	//opseg u kom kontrolni punkt provjerava osobe
	public static final int OPSEG = 1;
	
	public KontrolniPunkt(int x, int y, Mapa mapa) {
		super(x,y, mapa);
	}
	
	/*
	 * provjera da li osoba proslijedjena kao parametar ima temperaturu
	 */
	public static boolean imaTemperaturu(Osoba osoba) {
		return osoba.getTemperatura() > GRANICA_TEMPERATURE;		
	}
	
	@Override
	public String toString() {
		return "Kontrolni punkt na " + super.toString();
	}
	
	/*
	 * preuzimanje osobe sa zadatim koordinatama
	 *na jednom polju moze biti samo jedan element, pa kako osoba moze doci "na" kontrolni punkt
	 *ona ce imati koordinate tog KP, ali na tom polju ce biti samo KP
	 *na ovaj nacin, trazeci poklapanje koordinata osobe, "uzimamo" osobu sa kontrolnog punkta
	 */
	public Osoba uzmiOsobu(int x, int y) {
		for(Osoba o : mapa.osobe) {
			if(o.getX() == x && o.getY() == y) {
				return o;
			}
		}
		return null;
	}
}
