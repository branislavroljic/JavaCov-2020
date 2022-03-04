package simulacija;

import application.forme.GlavnaForma;
import elementi.osobe.Dijete;
import elementi.osobe.Odrasli;
import elementi.osobe.Osoba;
import elementi.osobe.Osoba.Pol;
import elementi.osobe.Osoba.Stanje;
import elementi.osobe.Stari;
import elementi.zdravstveniObjekti.Ambulanta;
import util.Alarm;
import util.ZarazeniFileWatcher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Simulacija {
	private Mapa mapa;	
	public static Object lock = new Object();
	public static Random random = new Random();
	
	//ukupan broj zarazenih od starta aplikacije
	public static int brojZarazenih = 0;
	public static int brojOporavljenih = 0;
	
	private static boolean zaustavljena = false;
	private static boolean ambulantaProlazi = false;
	
	//Timer za mijenjanje temperature osobama
	private Timer tempTimer;
	private ZarazeniFileWatcher fileWatcher;
	
	//gui aplikacija
	public static GlavnaForma glavnaForma;	
	
	private int brojStarih, brojOdraslih, brojDjece, brojKuca, brojKontrolnihPunktova, brojAmbulantnihVozila;
	private int brojAmbulanti = 4;

	public static int BROJ_STANOVNIKA;

	//direktorijum koji sadrzi fajl u kojem se upisuju informacije o broju zarazenih
	public static final Path zarazeniDir = Paths.get("." + File.separator + "zarazeniDir");

	// fajl u koji se serijalizuje mapa grada i objekti na njoj
	public static final Path SERIJALIZOVANI_FAJL = Paths.get(System.getProperty("user.dir") 
																+ File.separator + "JavaKov.ser");

	
	public Simulacija(int brojStarih, int brojOdraslih, int brojDjece, int brojKuca,
			int brojKontrolnihPunktova, int brojAmbulantnihVozila) {
		super();
		this.brojStarih = brojStarih;
		this.brojOdraslih = brojOdraslih;
		this.brojDjece = brojDjece;
		this.brojKuca = brojKuca;
		this.brojKontrolnihPunktova = brojKontrolnihPunktova;
		this.brojAmbulantnihVozila = brojAmbulantnihVozila;
		BROJ_STANOVNIKA = brojDjece + brojOdraslih + brojStarih;
		mapa = new Mapa();	
		mapa.postaviKontrolnePunktove(brojKontrolnihPunktova);
		mapa.postaviKuceNaMap(brojKuca);
		mapa.dodajOsobeUKuce(brojDjece, brojOdraslih, brojStarih);
		mapa.postaviAmbulante(brojAmbulanti, brojAmbulantnihVozila, true);		
	}
	
	public Simulacija(Mapa mapa) {
		this.mapa = mapa;
		mapa.postaviKontrolnePunktove(brojKontrolnihPunktova);
		mapa.postaviKuceNaMap(brojKuca);
		mapa.dodajOsobeUKuce(brojDjece, brojOdraslih, brojStarih);
		mapa.postaviAmbulante(4, brojAmbulantnihVozila, true);
	}
	
	public Mapa getMapa() {
		return mapa;
	}
	public void setMapa(Mapa mapa) {
		this.mapa = mapa;
	}
	
	public int getBrojStarih() {
		return brojStarih;
	}

	public void setBrojStarih(int brojStarih) {
		this.brojStarih = brojStarih;
	}

	public int getBrojOdraslih() {
		return brojOdraslih;
	}

	public void setBrojOdraslih(int brojOdraslih) {
		this.brojOdraslih = brojOdraslih;
	}

	public int getBrojDjece() {
		return brojDjece;
	}

	public void setBrojDjece(int brojDjece) {
		this.brojDjece = brojDjece;
	}

	public int getBrojKuca() {
		return brojKuca;
	}

	public void setBrojKuca(int brojKuca) {
		this.brojKuca = brojKuca;
	}

	public int getBrojKontrolnihPunktova() {
		return brojKontrolnihPunktova;
	}

	public void setBrojKontrolnihPunktova(int brojKontrolnihPunktova) {
		this.brojKontrolnihPunktova = brojKontrolnihPunktova;
	}

	public int getBrojAmbulantnihVozila() {
		return brojAmbulantnihVozila;
	}

	public void setBrojAmbulantnihVozila(int brojAmbulantnihVozila) {
		this.brojAmbulantnihVozila = brojAmbulantnihVozila;
	}
	
	public int getBrojAmbulanti() {
		return brojAmbulanti;
	}

	public void setBrojAmbulanti(int brojAmbulanti) {
		this.brojAmbulanti = brojAmbulanti;
	}
	
	public synchronized boolean isAmbulantaProlazi() {
		return ambulantaProlazi;
	}

	public synchronized void setAmbulantaProlazi(boolean prolazi) {
		ambulantaProlazi = prolazi;
		synchronized(lock) {
			lock.notifyAll();
		}
	}
	
	//nit zaduzena za pokretanje osoba
	Thread pokretacOsoba = new Thread() {
	@Override
		public void run() {
			while(true) {
				while(isZaustavljena()) {
					synchronized (lock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							Logger.getLogger(Simulacija.class.getName()).log(Level.SEVERE, null, e);
						}
					}				
				}
				//dok ambulanta prolazi osobe se ne mogu kretati
				while(isAmbulantaProlazi()) {
					synchronized (lock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							Logger.getLogger(Simulacija.class.getName()).log(Level.SEVERE, null, e);
						}
					}
				}
				//da ne mozemo predvidjeti redoslijed
				Collections.shuffle(mapa.osobe);
				for(int i = 0; i < mapa.osobe.size() && !isAmbulantaProlazi(); i++) {
					if(isZaustavljena())
						break;
					Osoba osoba = mapa.osobe.get(i);
						osoba.run();

					}
				}
			}
		
	};
	
	//nit zaduzena za pokretanje ambulantnih vozila
	Thread pokretacKola = new Thread() {
			@Override
			public void run() {
				while(true) {
					while(!isAmbulantaProlazi()) {
						synchronized(lock) {
							try {
								lock.wait();
							} catch (InterruptedException e) {
								Logger.getLogger(Simulacija.class.getName()).log(Level.SEVERE, null, e);
							}
						}
					}
					obradiAlarm();
					setAmbulantaProlazi(false);
					synchronized (lock) {
						lock.notify();			
					}
				}		
			}
		};
	
	//timer za mijenjanje temperature osobama
	public void startTimer() {
		tempTimer = new Timer();
		tempTimer.schedule(new TimerTask() {
			@Override
			public void run(){
				mapa.osobe.forEach(o -> o.setTemperatura(new Random().nextDouble() + 36.5));
				}
			}, 0, Osoba.PROMJENA_TEMP*1000);
	}
   
	//zaustavljanje tajmera
	public void stopTimer() {
		tempTimer.cancel();
		tempTimer.purge();
	}

	public static void setGlavnaForma(GlavnaForma mwc) {
		Simulacija.glavnaForma = mwc;
	}
	
	//uzimanje alarma sa steka
	public Alarm getAlarm() {
		try {
			return mapa.alarmi.getLast();
		}catch(NoSuchElementException e) {
			return null;
		}	
	}	
	public Alarm uzmiAlarmNaObradu() {
		return mapa.alarmi.pollLast();
	}
	
	
	//obrada alarma koji se nalaze na steku
	public void obradiAlarm() {
		Alarm obrada;
		while ((obrada = getAlarm()) != null) {
			Ambulanta ambulanta = mapa.nadjiNajblizuAmbulantu(obrada);
			if (ambulanta != null) {
				ambulanta.saljiVozilo(uzmiAlarmNaObradu());
			} else {
				Simulacija.glavnaForma.stigloObavjestenje("SVE AMBULANTE POPUNJENE!", false);
				break;
			}
		}
	}
	
	/*
	 * filteri za pronalazenje grupa osoba sa odredjenim karakteristikama
	 */
	public static Predicate<Osoba> trenutnoZarazeni = o -> Stanje.ZARAZEN.equals(o.getStanje());
	public static  Predicate<Osoba> oporavljeni = o -> Stanje.OPORAVLJEN.equals(o.getStanje())
										|| Stanje.OPORAVLJEN_U_IZOLACIJI.equals(o.getStanje());
	public static  Predicate<Osoba> muskiFilter = o -> Pol.MUSKI.equals(o.getPol());
	public static  Predicate<Osoba> zeneFilter = o -> Pol.ZENSKI.equals(o.getPol());
	public static  Predicate<Osoba> odrasliFilter = o -> o instanceof Odrasli;
	public static  Predicate<Osoba> stariFilter = o -> o instanceof Stari;
	public static  Predicate<Osoba> djecaFilter = o -> o instanceof Dijete;
	
	
	//filtriranje osoba po filterima i vracanje broja osoba koje zadovoljavaju filtere
	@SuppressWarnings("unchecked")
	public long getBrojOsobaPoFilteru(Predicate<Osoba>... filteri) {
		return mapa.osobe.stream().filter(o -> {
			boolean isOk = true;
			for (Predicate<Osoba> f : filteri){
				isOk = isOk && f.test(o);
			}
			return isOk;
		}).count();
	}
	
	//pokretanje simulacije prvi put, na pritisak Start button-a
	public void startSimulacija() {
		try {
			if (!new File(zarazeniDir.toString()).exists())
				Files.createDirectory(zarazeniDir);
		} catch (IOException e) {
			Logger.getLogger(Simulacija.class.getName()).log(Level.SEVERE, null, e);
		}

		if(zarazeniDir.resolve("zarazeni.txt").toFile().exists()) {
			zarazeniDir.resolve("zarazeni.txt").toFile().delete();
		}
		try {
			new Thread(fileWatcher = new ZarazeniFileWatcher(zarazeniDir, "zarazeni.txt", mapa)).start();
		} catch (IOException e) {
			Logger.getLogger(Simulacija.class.getName()).log(Level.SEVERE, null, e);
		}

		setZaustavljena(false);
		setAmbulantaProlazi(false);
		pokretacKola.setDaemon(true);
		pokretacOsoba.setDaemon(true);
		pokretacKola.start();
		pokretacOsoba.start();
	}
	
	//deserijalizacija i pokretanje
	public void pokreniSimulaciju() {
		
		Mapa mapa = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SERIJALIZOVANI_FAJL.toFile()));
			mapa = (Mapa)ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			Logger.getLogger(Simulacija.class.getName()).log(Level.SEVERE, null, e);
		}
		
		this.setMapa(mapa);
		fileWatcher.setMapa(mapa);
		if(mapa == null) {
			System.exit(1);
		}
		mapa.ambulante.forEach(a -> a.startTimer());
		setZaustavljena(false);
	}
	
	//serijalizacija i zaustavljanje
	public void zaustaviSimulaciju() {
		
		setZaustavljena(true);
		mapa.ambulante.forEach(a -> a.stopTimer());
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SERIJALIZOVANI_FAJL.toFile()));
			oos.writeObject(mapa);
			oos.close();
		} catch (IOException e) {
			Logger.getLogger(Simulacija.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	public static synchronized boolean isZaustavljena() {
		return zaustavljena;
	}

	public static synchronized void setZaustavljena(boolean zaust) {
		zaustavljena = zaust;
		if(zaustavljena == false) {
			synchronized (lock) {
				lock.notifyAll();
			}
		}
	}

}

	
