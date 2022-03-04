package simulacija;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import elementi.Element;
import elementi.PokretniElement.Pravac;
import elementi.kuce.Kuca;
import elementi.Polje;
import elementi.osobe.Dijete;
import elementi.osobe.Odrasli;
import elementi.osobe.Osoba;
import elementi.osobe.Stari;
import elementi.zdravstveniObjekti.Ambulanta;
import elementi.zdravstveniObjekti.AmbulantnoVozilo;
import elementi.zdravstveniObjekti.KontrolniPunkt;
import util.Alarm;
import java.awt.geom.Point2D;

public class Mapa implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public final int VELICINA = new Random().nextInt(16) + 15;
	public final int DONJA_GRANICA = 0;
	private Polje[][] map = new Polje[VELICINA][VELICINA];
	
	
	public HashMap<Integer, List<Osoba>> kucaOsobe = new HashMap<>();
	public HashMap<Integer, Kuca> kuce = new HashMap<>();
	public List<Osoba> osobe = new ArrayList<>();
	public List<Ambulanta> ambulante = new ArrayList<>();
	public LinkedBlockingDeque<Alarm> alarmi = new LinkedBlockingDeque<>();

	public Mapa() {
		for (int i = 0; i < VELICINA; i++) {
			for (int j = 0; j < VELICINA; j++) {
				map[i][j] = new Polje(i, j);
			}
		}
	}
	
	//stavljanje alarma na stek
	public void setAlarm(Alarm alarm) {
		alarmi.offer(alarm);
		posaljiOsobeKuci(alarm.getIDkuce());
	}
	
	//slanje osoba kuci, koristi se kada se registruje zarazeni,
	//da sve ukucane kuce ciji je on clan posaljemo kuci
	private void posaljiOsobeKuci(int ID) {
		kucaOsobe.get(ID).stream().filter(o -> !o.isUKuci())
				.forEach(o -> {
			if(!o.getStanje().equals(Osoba.Stanje.ZARAZEN)) {
				o.setStanje(Osoba.Stanje.POSLAN_KUCI);
			}});
		
		List<Osoba> osobeTemp = kucaOsobe.get(ID).stream().filter(o -> o.isUKuci()).collect(Collectors.toList());
		osobeTemp.parallelStream().forEach(o -> o.setStanje(Osoba.Stanje.U_IZOLACIJI));
	}


	public Polje polje(int x, int y) {
		return map[x][y];
	}

	/*
	 * funkcija za provjeru postojanja elemenata koji zadovoljavaju Predicate
	 * x , y su koordinate polja, opseg je udaljenost od datog polja na kojoj se provjerava postojanje
	 * elemenata koji zadovoljavaju Predicate filter
	 */
	public int objektiUOpsegu(int x, int y, int opseg, Predicate<Element> filter) {
		int brojObjekata = 0;
		for (int i = x - opseg; i < x + opseg + 1; i++) {
			for (int j = y - opseg; j < y + opseg + 1; j++) {
				if (i >= DONJA_GRANICA && j >= DONJA_GRANICA && i < VELICINA && j < VELICINA) {
					if (!praznoPoljeNa(i, j) && filter.test(map[i][j].getElement()))
						brojObjekata++;
				}
			}
		}
		return brojObjekata;
	}

	/*
	 * pronalazak najblize ambulante koja nije puna
	 */
	public Ambulanta nadjiNajblizuAmbulantu(Alarm alarm) {
		List<Ambulanta> najblizaAmbulanta =ambulante.stream()
				.sorted((a, e) -> Double.compare(Point2D.distance(a.getX(), a.getY(), alarm.getX(), alarm.getY()),
												Point2D.distance(e.getX(), e.getY(), alarm.getX(), alarm.getY())))
				.collect(Collectors.toList());

		for (Ambulanta amb : najblizaAmbulanta) {
			if (amb.mozePrimitiPacijenta()) {
				return amb;
			}
		}
		return null;
	}
	
	
	
	/*
	 * postavljanje ambulanti na mapu, ako je prvo postavljanje, postavljaju se 4 ambulante na odgovarajuce pozicije
	 * vozila se dodaju kruzno
	 * ako je drugo postavljanje, trazi se prva slobodna pozicija u redu ili koloni te se postavlja ambulanta sa
	 * jednim vozilom
	 * vracena vrijednost je broj postavljenih ambulanti
	 */
	public synchronized int postaviAmbulante(int brojAmbulanti, int brojAmbulantnihVozila,
			boolean prvoPostavljanje) {
		int zahtijevaniBrojAmbulanti = brojAmbulanti;
		if (prvoPostavljanje) {
			ambulante.addAll(Arrays.asList(new Ambulanta(0, 0, this), new Ambulanta(VELICINA - 1, 0, this),
										new Ambulanta(0, VELICINA - 1, this), new Ambulanta(VELICINA - 1, VELICINA - 1, this)));
			ambulante.forEach(a -> map[a.getX()][a.getY()].setElement(a));

			for (Ambulanta a : ambulante) {
				a.dodajVozilo(new AmbulantnoVozilo(a.getX(), a.getY(), this));
				brojAmbulantnihVozila--;
				if (brojAmbulantnihVozila <= 0)
					break;
			}
		} else {
			for (int i = 0; i < VELICINA; i++) {
				for (int j = 0; j < VELICINA; j++) {
					if(i == VELICINA - 1 && j == VELICINA - 1) 
						return zahtijevaniBrojAmbulanti - brojAmbulanti;
					if (i == 0 || i == VELICINA - 1 || j == 0 || j == VELICINA - 1) {
						if (praznoPoljeNa(i, j)) {
							Ambulanta a = new Ambulanta(i, j, this);
							ambulante.add(a);
							map[i][j].setElement(a);
							a.dodajVozilo(new AmbulantnoVozilo(a.getX(), a.getY(), this));
							brojAmbulanti--;
							if (brojAmbulanti == 0)
								return zahtijevaniBrojAmbulanti;
							
						}
					}
				}
			}
		}
		return zahtijevaniBrojAmbulanti;
	}

	//postavljanje kuca na mapu
	public void postaviKuceNaMap(int brojKuca) {
		int x, y;
		Random random = new Random();
		for (int i = 0; i < brojKuca; i++) {
			while (!(praznoPoljeNa(x = random.nextInt(VELICINA - 2) + 1, y = random.nextInt(VELICINA - 2) + 1)))
				;		
			Kuca kuca = new Kuca(x, y, this);
			map[x][y].setElement(kuca);
			 kuce.put(kuca.getID(), kuca);
			 kucaOsobe.put(kuca.getID(), new ArrayList<>());
		}
	}

	/*
	 * dodavanje ukucana u kucu, dodaju se odrasli dok ih ne nestane, ako ih nestane, dodaju se stari;
	 * nakon popunjavanja svih kuca sa po jednom odraslom ili starom osobom, dodaju se preostale odrasle osobe, stari
	 * i djeca u kuce na slucajan nacin
	 */
	public void dodajOsobeUKuce(int brojDjece, int brojOdraslih, int brojStarih) {

		int brojOdr = 0;
		int brojStr = 0;

		Iterator<Map.Entry<Integer, List<Osoba>>> itr =  kucaOsobe.entrySet().iterator();

		while (itr.hasNext() && brojOdr < brojOdraslih) {
			Map.Entry<Integer, List<Osoba>> e = itr.next();
			Kuca kuca =  kuce.get(e.getKey());
			e.getValue().add(new Odrasli(kuca.getX(), kuca.getY(), kuca.getID(), this));
			brojOdr++;
		}
		if (itr.hasNext()) {
			while (itr.hasNext()) {
				Map.Entry<Integer, List<Osoba>> e = itr.next();
				Kuca kuca =  kuce.get(e.getKey());
				e.getValue().add(new Stari(kuca.getX(), kuca.getY(), kuca.getID(), this));
				brojStr++;
			}
		}
		int brojKuca =  kuce.size();
		Random rand = new Random();
		
		//ako je ostalo odraslih, dodajemo ih random
		while (brojOdr < brojOdraslih) {
			int randInt = rand.nextInt(brojKuca);
			Kuca randKuca =  kuce.get(randInt);
			 kucaOsobe.get(randInt).add(new Odrasli(randKuca.getX(), randKuca.getY(), randInt, this));
			brojOdr++;
		}
		//ako je ostalo starih, dodajemo ih random
		while (brojStr < brojStarih) {
			int randInt = rand.nextInt(brojKuca);
			Kuca randKuca =  kuce.get(randInt);
			 kucaOsobe.get(randInt).add(new Stari(randKuca.getX(), randKuca.getY(), randInt, this));
			brojStr++;
		}
		//dodajemo djecu u kuce random
		while (brojDjece > 0) {
			int randInt = rand.nextInt(brojKuca);
			Kuca randKuca =  kuce.get(randInt);
			 kucaOsobe.get(randInt).add(new Dijete(randKuca.getX(), randKuca.getY(), randInt, this));
			brojDjece--;
		}

		for (List<Osoba> listaOsoba :  kucaOsobe.values()) {
			 osobe.addAll(listaOsoba);
		}

	}

	//postavljanje kontrolnih punktova
	public void postaviKontrolnePunktove(int brojPunktova) {
		int x, y;
		Random random = new Random();
		for (int i = 0; i < brojPunktova; i++) {
			while (!(praznoPoljeNa(x = random.nextInt(VELICINA - 2) + 1, y = random.nextInt(VELICINA - 2) + 1)))
				;
			KontrolniPunkt kp = new KontrolniPunkt(x, y, this);
			map[x][y].setElement(kp);
		}
	}

	/*
	 * public void printMap() { for (int i = 0; i < VELICINA; i++) { for (int j = 0;
	 * j < VELICINA; j++) { if ((praznoPoljeNa(i, j))) System.out.print("|___|");
	 * else System.out.print(map[i][j].getElement() + ""); } System.out.println(); }
	 * 
	 * System.out.println(); }
	 */
	
	/*
	 * Funkcija za pronalazenje najkraceg puta od pocetniElement do krajnjiElement
	 * parametar prioritet oznacava da li se prilikom generisanja putanje mora izbjeci postojanje Elemenata koji zadovoljavaju Predicate filter
	 * u okolini sirine opseg
	 */
	public List<Polje> nadjiNajkraciPut(Element pocetniElement, Element krajnjiElement, boolean prioritet, Predicate<Element> filter, Integer opseg) {

		Map<Polje, Polje> roditelji = new HashMap<>();
		
		Polje pocetak = map[pocetniElement.getX()][pocetniElement.getY()];
		Polje kraj = map[krajnjiElement.getX()][krajnjiElement.getY()];

		// prolazak kroz svako Polje koristeci BFS dok se ne dostigne destinaciju
		List<Polje> temp = new ArrayList<Polje>();
		temp.add(pocetak);
		roditelji.put(pocetak, null);
 
		boolean dostignutaDestinacija = false;
		while (temp.size() > 0 && !dostignutaDestinacija) {
			Polje trenutnoPolje = temp.remove(0);
			List<Polje> djeca = getChildren(trenutnoPolje);
			//boolean nadjenoPolje = false;
			for (Polje dijete : djeca) {
				// svako Polje moze biti posjeceno jedanput
				if (!roditelji.containsKey(dijete)) {
					roditelji.put(dijete, trenutnoPolje);
					
					if (prioritet) {
						if (dijete.getElement() == null) {
							temp.add(dijete);
						}
					} else {
						//filter za provjeru postojanja osoba u okolini, uklucuje uoptrebu filtera koji
						//je poslan kao parametar, te promjeru da li je pronadjeni element != elementa koji
						//se treba kretati
							Predicate<Element> filter1 = o -> !o.equals(pocetniElement) && filter.test(o);
							if (dijete.getElement() == null
									&& objektiUOpsegu(dijete.getX(), dijete.getY(), opseg, filter1) == 0) {
								temp.add(dijete);
							}
						}
					//ako je dostignut kraj
					if (dijete.equals(kraj)) {
						dostignutaDestinacija = true;
						break;
					}
				}
			}
		}
		
		//nije moguce doci do kraja, ova situacija se moze desiti npr. ako
		//odrasla ili stara osoba zeli ici kuci, a kuca je okruzena ostalim osobama, pa nema nacina da udje
		if(!dostignutaDestinacija) {
			return null;
		}

		//biranje najkraceg puta
		Polje element = kraj;
		List<Polje> putanja = new ArrayList<Polje>();
		while (element != null) {
			putanja.add(0, element);
			element = roditelji.get(element);
		}
		return putanja;
	}

	/*
	 * Generisanje mogucih pravaca kretanja za element na polju roditelj
	 */
	public List<Polje> getChildren(Polje roditelj) {
		List<Polje> dijete = new ArrayList<Polje>();
		int x = roditelj.getX();
		int y = roditelj.getY();
		List<Pravac> pravci = EnumSet.allOf(Pravac.class).parallelStream()
				.filter(p -> x + p.getX() >= DONJA_GRANICA && x + p.getX() < VELICINA)
				.filter(p -> y + p.getY() >= DONJA_GRANICA && y + p.getY() < VELICINA)
				.collect(Collectors.toList());
		for (Pravac pravac : pravci) {
			dijete.add(map[x + pravac.getX()][y + pravac.getY()]);
		}
		return dijete;
	}

	/*
	 * provjera postojanja objekata na odgovarajucim pozicijama
	 */
	public boolean praznoPoljeNa(int x, int y) {
		return map[x][y].getElement() == null;
	}

	public boolean kucaNa(int x, int y) {
		return map[x][y].getElement() instanceof Kuca;
	}

	public boolean ambulantaNa(int x, int y) {
		return map[x][y].getElement() instanceof Ambulanta;
	}

	public boolean kontrolniPunktNa(int x, int y) {
		return map[x][y].getElement() instanceof KontrolniPunkt;
	}

	public boolean ambulantnoVoziloNa(int x, int y) {
		return map[x][y].getElement() instanceof AmbulantnoVozilo;
	}

	/*
	 * na jednom polju moze biti samo jedan element, metoda se moze upotrijebiti da bi se izbjeglo setovanje nepokretnih elemenata na kojima je osoba 
	 * na null, jer ako se osoba nalazi na nekom od njih, ona ce imati koordinate tog elementa, ali stvarna vrijednost polja ce biti dati nepokretni
	 * element(kuca, ambulanta, KP), pa zato da provjerimo postojanje osobe na nekoj poziciji  provjeravamo njene koordinate
	 * a ne vrijednost polja
	 */
	public boolean osobaNa(int x, int y) {
		return map[x][y].getElement() instanceof Osoba;
	}

	public boolean dijeteNa(int x, int y) {
		return map[x][y].getElement() instanceof Dijete;
	}

	public boolean odrasliNa(int x, int y) {
		return map[x][y].getElement() instanceof Odrasli;
	}

	public boolean stariNa(int x, int y) {
		return map[x][y].getElement() instanceof Stari;
	}

	public int getBrojRedova() {
		return VELICINA;
	}

	public int getBrojKolona() {
		return VELICINA;
	}

}
