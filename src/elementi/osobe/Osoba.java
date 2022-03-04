package elementi.osobe;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import elementi.Element;
import elementi.PokretniElement;
import elementi.Polje;
import elementi.kuce.Kuca;
import elementi.zdravstveniObjekti.KontrolniPunkt;
import simulacija.Mapa;
import simulacija.Simulacija;
import util.Alarm;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class Osoba extends PokretniElement{
	
	private static final long serialVersionUID = 1L;

	//period promjene temperature
	public static final int PROMJENA_TEMP = 30;
	
	private final Pol pol;
	private String ime;
	private String prezime;
	private static int id = 0;
	private int ID = ++id;
	private int kucaID;
	private int godinaRodjenja;
	private Pravac pravac;
	private double temperatura;
	private boolean uKuci = true;
	private Stanje stanje = Stanje.SLOBODAN;
	
	protected static Random rand = new Random();

	//upotreba u finkciji mapa.objektiUOsegu()
	public static  Predicate<Element> kontrolniPunktFilter = (o) -> o instanceof KontrolniPunkt;
	public static  Predicate<Element> odrasliStariFilter = (o) -> o instanceof Odrasli || o instanceof Stari;
	
	public Osoba( int x, int y, String ime, String prezime, int godinaRodjenja, Pol pol, int kucaID, Mapa mapa){
		super(x, y, 300 + id*10, mapa);
		this.ime = ime;
		this.prezime = prezime;
		this.godinaRodjenja = godinaRodjenja;
		this.pol = pol;
		this.temperatura = new Random().nextDouble() + 36.1;
		this.kucaID = kucaID;
	}
	
	public enum Stanje{
		SLOBODAN, //osoba se krece po matrici, nije zarazena niti je iz kuce ciji je ukucan zarazen
		POSLAN_KUCI, //osoba je iz kuce ciji je ukucan zarazen
		ZARAZEN, //osoba je zarazena
		U_IZOLACIJI, //osoba jer usla u kucu koja je blokirana, jer je neki ukucan bio ili je i dalje zarazen
		OPORAVLJEN, //osoba je otpustena iz bolnice
		OPORAVLJEN_U_IZOLACIJI; //osoba je dosla kuci nakon oporavka
	}

	public enum Pol{
		MUSKI, ZENSKI;
		private static final Random random = new Random();
		
		public static Pol randomPol() {
			return random.nextInt() % 2 == 0 ? MUSKI : ZENSKI;
		}
	}

	public void setIme(String ime) {
		this.ime = ime;
	}

	public String getIme() {
		return ime;
	}

	public void setPrezime(String prezime) {
		this.prezime = prezime;
	}

	public String getPrezime() {
		return prezime;
	}

	public int getID() {
		return ID;
	}

	public int getKucaID() {
		return kucaID;
	}

	public Pol getPol() {
		return pol;
	}
	
	public synchronized double getTemperatura() {
		return temperatura;
	}

	public synchronized void setTemperatura(double temperatura) {
		this.temperatura = temperatura;
	}
	
	public Pravac getPravac() {
		return pravac;
	}
	
	public void setPravac(Pravac pravac) {
		this.pravac = pravac;
	}
	
	public synchronized Stanje getStanje() {
		return stanje;
	}

	public synchronized void setStanje(Stanje stanje) {
		this.stanje = stanje;
	}
	
	@Override
	public String toString() {
		return this.ID + ";"  +  this.pol + ";" + this.ime + ";" + this.prezime + ";" + this.godinaRodjenja + ";" + this.kucaID
				+ ";" + this.getPravac()
				+ ";(" + this.getX() + ", " + this.getY() + ")" + ";" + stanje + ";" + this.getClass().getSimpleName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Osoba) {
			Osoba o = (Osoba)obj;
			return (o.getID() == this.ID);
		}
		return false;
	}
	
	
	public boolean isUKuci() {
		return uKuci;
	}
	
	public void setUKuci(boolean uKuci) {
		this.uKuci = uKuci;
	}
	
	public void generisiKorak() {		
		//provjera da li su djeca ostala sa jednim odraslim ili starim u kuci
		//ako jeste, odrasli ili stari ne moze ici nikud
		if(mapa.kuce.get(kucaID).ostaloDijeteIJedanOdrasli() && !(this instanceof Dijete)) {
			pravac = Pravac.U_MJESTU;
			return;
		}
		
		//izbor mogucih pravaca kretanja, filtriraju se oni koraci koji ne prelaze granice kretanja osobe
	    List<Pravac> moguciPravci = EnumSet.allOf(Pravac.class).parallelStream()
	    						.filter(p -> x + p.getX() >= X_DONJA_GRANICA &&  x + p.getX() < X_GORNJA_GRANICA)
	    						.filter(p -> y + p.getY() >=  Y_DONJA_GRANICA  &&  y + p.getY() < Y_GORNJA_GRANICA)
    							.collect(Collectors.toList());
   
	    //Da ne bi osoba uvijek isla u istom pravcu
		   Collections.shuffle(moguciPravci);

			for (Pravac pravac : moguciPravci) {
				int moguciX = x + pravac.getX();
				int moguciY = y + pravac.getY();
				
				//izabrano polje je kuca cija je osoba ukucanin
				if ((mapa.kucaNa(moguciX, moguciY)
						&& (((Kuca) mapa.polje(moguciX, moguciY).getElement()).getID() == this.kucaID))) {
					
					//u kuci nema niko, dijete ne moze uci, jer ce biti samo u kuci
					if(mapa.kuce.get(this.kucaID).getBrojUkucana() == 0L && this instanceof Dijete) {
						continue;
					}
					
					// "udje" u kucu;
					setUKuci(true);

					if (mapa.osobaNa(this.x, this.y)) {
						mapa.polje(x, y).setElement(null);
					}
					x = moguciX;
					y = moguciY;
					this.pravac = pravac;
					return;
				}
				//izabrano polje nije prazno, osoba ne moze "stati" na polje koje nije njegova kuca ili kontrolnuPunkt
			   else if(!mapa.praznoPoljeNa(moguciX, moguciY) && !(mapa.kontrolniPunktNa(moguciX, moguciY))) { //ne moze u tudju kucu ili neki drugi objekat
				   continue;
			   }
				/*
				 * provjera da li je korak na izabrano "prazno" polje moguc
				 * ako je osoba dijete, korak je moguc uvijek, u suprotnom, korak je moguc ako je broj objekata u opsegu(odraslih i starih)
				 * jedanak 0 i osoba je u kuci ili na kontrolnom punktu
				 * odnosno 1 ako nije na nekom objektu(jer tada u opseg objekata ulazi i sama osoba(objekat this))
				 */
			   else{
					if (this instanceof Dijete
							|| !mapa.osobaNa(this.x, this.y)
									&& mapa.objektiUOpsegu(moguciX, moguciY, 2, odrasliStariFilter) == 0
							|| mapa.osobaNa(this.x, this.y)
									&& mapa.objektiUOpsegu(moguciX, moguciY, 2, odrasliStariFilter) <= 1) {

						if (isUKuci() && !pravac.equals(Pravac.U_MJESTU)) {
							setUKuci(false);
						} else if (mapa.osobaNa(this.x, this.y))
							mapa.polje(x, y).setElement(null);

						x = moguciX;
						y = moguciY;
						if (mapa.praznoPoljeNa(x, y)) {
							mapa.polje(x, y).setElement(this);
						}
						this.pravac = pravac;
						return;
					}
				}
			}
	}
	
public void run() {
	
		switch(stanje) {
		//ako je slobodan, krece se proizvoljno
		case SLOBODAN :
			
			try {
				Thread.sleep(brzinaKretanja);
			} catch (InterruptedException e) {
				Logger.getLogger(Osoba.class.getName()).log(Level.SEVERE, null, e);
			}
			
			generisiKorak();			
		
			/*
			 * ako je osoba u opsegu kontrolnog punkta, ispituje se njena temperatura, te se generise alarm ako osoba
			 * ima temperaturu vecu od definisane granice 
			 * uslov !isUkuci() je potreban jer osoba moze biti u kuci koja je u opsegu kontrolnog punkta,
			 * ali tada se ne treba vrsiti provjera osoba
			 */
			if (mapa.objektiUOpsegu(x, y, KontrolniPunkt.OPSEG, kontrolniPunktFilter) > 0 && !isUKuci()) {
				if (KontrolniPunkt.imaTemperaturu(this)) {
					Simulacija.brojZarazenih++;
					this.setStanje(Stanje.ZARAZEN);
					mapa.kuce.get(this.kucaID).setZarazenaKuca(true);
					mapa.setAlarm(new Alarm(x, y, kucaID));
				}

			}
			Simulacija.glavnaForma.izmijenjenaMatrica(this.toString());
			break;
			//ako je osoba poslana kuci ili se oporavila(nakon boravka u ambulanti), ona najkracim putem ide kuci
		case POSLAN_KUCI :
		case OPORAVLJEN :
			
			Simulacija.glavnaForma.sviButtonOFF();
			
			List<Polje> putanja = mapa.nadjiNajkraciPut(this, mapa.kuce.get(kucaID), this instanceof Dijete,
														odrasliStariFilter, 2);

			// ako nije moguce pronaci putanju, znaci da osoba ne moze uci u kucu jer je
			// druga "stara" ili "odrasla" osoba u okolini kuce
			// i blokira ulazak osobe uz definisani razmak
			// osoba ce uci tek kada osoba koja je blokira udje u kucu odnosno kada zarazeni(odrasli li stari)
			//koji je blokira bude prevezen ambulantnim kolim
			if (putanja == null) {
				Simulacija.glavnaForma.stigloObavjestenje("Osoba: " + this + " NE MOZE UCI U KUCU!",
						false);
				return;
			}

			putanja.remove(putanja.size() - 1); // kuca

			Polje pocetno = putanja.remove(0);//pozicija osobe
			if (mapa.osobaNa(pocetno.getX(), pocetno.getY())) {
				pocetno.setElement(null);
			}
			
			kretanjePoPutanji(putanja);

			// osoba je dosla do kuce, uvedem ga u kucu
			this.setUKuci(true);

			Simulacija.glavnaForma.izmijenjenaMatrica(this.toString());

			/*
			 * stanje nakon ulaska u kucu je U_IZOLACIJI, jer u stanje POSLAN_KUCI i
			 * OPORAVLJEN se dolazi nakon naredbe za povratak kuci svim osobama, odnosno nakon
			 * oporavka zarazenog. U oba slucaja, prekida se daljnje kretanje osoba u toj kuci
			 */
			setStanje(stanje.equals(Stanje.OPORAVLJEN)?Stanje.OPORAVLJEN_U_IZOLACIJI:Stanje.U_IZOLACIJI);
			Simulacija.glavnaForma.sviButtonON();
			break;
		case ZARAZEN:
		case U_IZOLACIJI:
		case OPORAVLJEN_U_IZOLACIJI:
			break;
		default:
			break;
		}
	}
}
