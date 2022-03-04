package elementi.kuce;

import java.util.List;
import java.util.stream.Collectors;

import elementi.NepokretniElement;
import elementi.osobe.Dijete;
import elementi.osobe.Osoba;
import simulacija.Mapa;

public class Kuca extends NepokretniElement{

	private static final long serialVersionUID = 1L;
	private static int id = 0;
	private int ID = id++;
	private boolean zarazenaKuca = false;
	
	public Kuca(int x, int y, Mapa mapa) {
		super(x,y, mapa);
	}
	
	/*
	 * kuca je zarazena ako je bar jedan od njenih ukucana zarazen, ili je bio zarazen
	 */
	public boolean isZarazenaKuca() {
		return zarazenaKuca;
	}

	public void setZarazenaKuca(boolean zarazenaKuca) {
		this.zarazenaKuca = zarazenaKuca;
	}

	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	
	public List<Osoba> getOsobeUKuci(){
		return mapa.kucaOsobe.get(ID).stream().filter(o -> o.isUKuci()).collect(Collectors.toList());
	}
	
	public long getBrojUkucana() {
		return getOsobeUKuci().size();
	}
	
	/*
	 * provjera da li su djeca ostala sa jednim odraslim ili starim u kuci, jer u tom odrasli i stari moraju cekati
	 * da sva djeca izadju iz kuce
	 */
	public boolean ostaloDijeteIJedanOdrasli() {
		List<Osoba> osobe = getOsobeUKuci();
		return osobe.stream().filter(o -> !(o instanceof Dijete)).count() == 1L &&
				osobe.stream().filter(o -> o instanceof Dijete).count() >= 1L;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Kuca) {
			Kuca k = (Kuca)obj;
			return (k.getID() == this.ID);
		}
		return false;
	}
		
	@Override
	public String toString() {
		return "Kuca" + ID + super.toString();
	}
	
}