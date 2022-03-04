package elementi.zdravstveniObjekti;

import java.util.Collections;
import java.util.List;
import elementi.Polje;
import elementi.osobe.Osoba;
import simulacija.Mapa;
import simulacija.Simulacija;
import util.Alarm;

public class AmbulantnoVozilo extends Vozilo {
	
	private static final long serialVersionUID = 1L;
	//osoba koja se "prevozi" u vozilu
	private Osoba bolesnik = null;
	//putanja po kojoj se krece vozilo
	private List<Polje> putanja = null;
	//preuzeti alarm sa steka
	private Alarm alarm;
	//brzina kretanja vozila
	private static final int brzina = 300;

	public AmbulantnoVozilo(int x, int y, Mapa mapa) {
		super(x, y, brzina, mapa);
	}
	
	public Alarm getAlarm() {
		return alarm;
	}
	public void setAlarm(Alarm alarm) {
		this.alarm = alarm;
	}

	public Osoba getBolesnik() {
		return bolesnik;
	}
	
	public void setBolesnik(Osoba bolesnik) {
		this.bolesnik = bolesnik;
	}

	public boolean isPrazno() {
		return bolesnik == null;
	}
	
	public List<Polje> getPutanja(){
		return putanja;
	}
	
	public void setPutanja(List<Polje> putanja) {
		this.putanja = putanja;
	}

	@Override
	public String toString() {
		return "Ambulantno vozilo " + (isPrazno() ? "bez bolesnika " : "sa bolesnikom ") + super.toString();
	}

	@Override
	public void podesiGranice() {
		X_GORNJA_GRANICA = mapa.VELICINA;
		X_DONJA_GRANICA = mapa.DONJA_GRANICA;
		Y_GORNJA_GRANICA = mapa.VELICINA;
		Y_DONJA_GRANICA = mapa.DONJA_GRANICA;
	}

	/*
	 * odlazak vozila po zarazenog, i transport istog u ambulantu
	 */
	public void run() {
		//isklucujem buttone
		Simulacija.glavnaForma.sviButtonOFF();
		
		putanja = mapa.nadjiNajkraciPut(this, mapa.polje(alarm.getX(), alarm.getY()).getElement(), true, null, null);
		if (putanja != null) {
			//prvo polje je ambulanta, posljednje je pozicija osobe
			Polje ambulantaPolje = putanja.remove(0);
			Polje osobaPolje = putanja.remove(putanja.size() - 1);

			//iniciram kretanje
			kretanjePoPutanji(putanja);

			//ako je zarazeni na kontrolnom punktu, vrijednost polja ne setujemo na null
			if (mapa.kontrolniPunktNa(osobaPolje.getX(), osobaPolje.getY())) {
				this.setBolesnik(
						((KontrolniPunkt) osobaPolje.getElement()).uzmiOsobu(osobaPolje.getX(), osobaPolje.getY()));
			} else {
				this.setBolesnik((Osoba) osobaPolje.getElement());
				osobaPolje.setElement(null);
			}

			//preokretanje putanje
			Collections.reverse(putanja);
			//iniciram kretanje
			kretanjePoPutanji(putanja);
			
			((Ambulanta) ambulantaPolje.getElement()).dodajVozilo(this);

			this.setuPokretu(false);
			Simulacija.glavnaForma.izmijenjenaMatrica(this.toString());
			//omogucim buttone
			Simulacija.glavnaForma.sviButtonON();
		}
	}
}
