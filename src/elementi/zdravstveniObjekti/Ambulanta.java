package elementi.zdravstveniObjekti;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import elementi.NepokretniElement;
import elementi.osobe.Osoba;
import simulacija.Mapa;
import simulacija.Simulacija;
import util.Alarm;
import util.NumBuffer;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class Ambulanta extends NepokretniElement {
	
	private static final long serialVersionUID = 1L;
	private static int id = 0;
	private int ID = ++id;
	//direktorijum u kome se nalazi fajl u koji se upisuju podaci o zarazenim osobama
	private static File zarazeniFile = new File(Simulacija.zarazeniDir + File.separator + "zarazeni.txt");
	private transient Timer tempTimer;
	private static final int intervalMjerenja = 20;
	private final int KAPACITET = (int) (Simulacija.BROJ_STANOVNIKA * ((new Random().nextInt(6) + 10) / 100.0f));

	//abmulantna vozila dostupna u ambulanti
	private List<AmbulantnoVozilo> vozila = new ArrayList<>();
	
	//mapa u kojoj se cuvaju posljednje tri temperature svake osobe
	private HashMap<Osoba, NumBuffer<Double>> bolesnici = new HashMap<>(KAPACITET);

	public Ambulanta(int x, int y, Mapa mapa) {
		super(x, y, mapa);
		startTimer();
	}
	
	public int getKapacitet() {
		return KAPACITET;
	}

	public int getID() {
		return ID;
	}

	public boolean mozePrimitiPacijenta() {
		return vozila.size() > 0 && bolesnici.size() < KAPACITET;
	}

	public List<AmbulantnoVozilo> getAmbulantnaVozila() {
		return vozila;
	}

	public int getBrojZarazenih() {
		return bolesnici.size();
	}

	public void dodajVozila(List<AmbulantnoVozilo> AmbVozila) {
		vozila.addAll(AmbVozila);
	}

	/*
	 * dodavanje vozila u ambulantu i preuzimanje bolesnika ukoliko vozilo nije prazno
	 */
	public void dodajVozilo(AmbulantnoVozilo vozilo) {
		if (vozilo == null) {
			return;
		}
		vozila.add(vozilo);
		if (!vozilo.isPrazno()) {
			Osoba bolesnik;
			bolesnici.put(bolesnik = vozilo.getBolesnik(), new NumBuffer<Double>(3));
			bolesnici.get(bolesnik).add(bolesnik.getTemperatura());

			//setujem koordinate na poziciju ambulante
			bolesnik.setX(this.getX());
			bolesnik.setY(this.getY());
			vozilo.setX(this.getX());
			vozilo.setY(this.getY());

			//vadjenje bolesnika iz vozila
			vozilo.setBolesnik(null);
			
			Simulacija.glavnaForma.stigloObavjestenje("Vozilo uslo u ambulantu!", true);
			
			//upis informacija u fajl
			upisiInfoUFajl();
		}
	}

	/*
	 * provjera srednje vrijednosti posljednje tri temperature
	 */
	public void izmjeriTemperature() {
		Iterator<Map.Entry<Osoba, NumBuffer<Double>>> it = bolesnici.entrySet().iterator();
		Map.Entry<Osoba, NumBuffer<Double>> ozdravljeni;
		while (it.hasNext()) {
			
			//mjerenje nove vrijednosti
			ozdravljeni = it.next();
			ozdravljeni.getValue().add(ozdravljeni.getKey().getTemperatura());
			//ako nisu izvrsena bar tri mjerenja		
			if (ozdravljeni.getValue().getBrojElemenata() < ozdravljeni.getValue().getVelicina()) {
				return;
				//ukoliko je prosjecna temp manja od 37, osoba se oporavila
			} else if (ozdravljeni.getValue().sum(Double::sum) / (double) ozdravljeni.getValue().getVelicina() < 37.0) {
				it.remove();
				ozdravljeni.getKey().setStanje(Osoba.Stanje.OPORAVLJEN);
				Simulacija.glavnaForma.stigloObavjestenje("OZDRAVIO JE " + ozdravljeni.getKey(), true);				
				Simulacija.brojOporavljenih++;

				upisiInfoUFajl();
			}
		}
	}

	/*
	 * periodicno mjerenje temperatura osoba koje su u ambulanti
	 */
	public void startTimer() {
		tempTimer = new Timer();
		tempTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				izmjeriTemperature();
			}
		}, 0, TimeUnit.SECONDS.toMillis(intervalMjerenja));

	}
	
	//zaustavljanje tajmera
		public void stopTimer() {
			tempTimer.cancel();
			tempTimer.purge();
		}


	/*
	 * slanje vozila po zarazenog na naredbu sistema za nadzor
	 */
	public void saljiVozilo(Alarm alarm) {
		AmbulantnoVozilo vozilo = vozila.get(new Random().nextInt(vozila.size()));
		vozilo.setAlarm(alarm);
		vozilo.run();
	}

	/*
	 *  upisivanje informacija o broju zarazenih u ambulantama u dijeljeni fajl
	 *  ambulanta#ID#datum#zarazeni#brojZarazenih
	 */
	private void upisiInfoUFajl() {
		SimpleDateFormat datumFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(zarazeniFile, true));
			pw.println("ambulanta#" + ID + "#" + datumFormat.format(new Date()) + "#zarazeni#" + getBrojZarazenih());
			pw.close();

		} catch (IOException e) {
			Logger.getLogger(Ambulanta.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	@Override
	public String toString() {
		return "Ambulanta " + ID + " " + super.toString(); 
	}
}
