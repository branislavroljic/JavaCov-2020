package application.forme;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import elementi.kuce.Kuca;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.util.Callback;
import simulacija.Mapa;
import simulacija.Simulacija;
import util.Alarm;

public class GlavnaForma implements Initializable {
	
	private Stage thisStage;
	private Scene thisScene;
	public static final Path zavrsniFajl = Paths.get(System.getProperty("user.dir") + File.separator + "SIM-JavaKov-20-");
	private Simulacija simulacija;
	private long startTime;
	private long ukupnoVrijeme = 0;

	
	@FXML
	private GridPane mapaGP;
	
	private Button[][] gridButtons;

	@FXML
	private ListView<String> listaKretanja;
	
	@FXML
	private ListView<Alarm> alarmiListView;

	ObservableList<Alarm> alarmi;
	
	@FXML
	private ListView<Kuca> blokiraneKuce;
	
	ObservableList<Kuca> kuce; 
	
	@FXML
	private Label brojZarazenihUAmbulantiLabela;
	
	@FXML
	private Label brojOporavljenihLabela;
	
	@FXML
	private Label obavjestenjePolje;
	
	@FXML
	private Button pauzaButton;
	
	@FXML
	private Button nastavakButton;
	
	@FXML
	private Button pregledajAmbulanteButton;
	
	@FXML
	private Button pregledajStatistickePodatkeButton;
	
	@FXML
	private Button ambulantnoVoziloButton;


	public GlavnaForma(Stage stage, Simulacija simulacija) {
		thisStage = stage;
		thisStage.setOnCloseRequest(event -> {
			System.exit(1);
		});
		thisStage.setResizable(false);
		this.simulacija = simulacija;
		
		kuce = FXCollections.<Kuca>observableArrayList(
				simulacija.getMapa()
				.kuce.values()
				.stream()
				.filter(k -> k.isZarazenaKuca()).collect(Collectors.toList()));
		alarmi = FXCollections.<Alarm>observableArrayList();
		
		//Load the FXML file
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/fxmlFajlovi/MainWindow.fxml"));
			
			//Set this class as controller
			loader.setController(this);
			
			//Save the scene
			thisScene = new Scene(loader.load());
			
		} catch (IOException ex) {
			Logger.getLogger(GlavnaForma.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	@Override
    public void initialize(URL location, ResourceBundle resources) {
		
		initializeLables();
		initializeMap();
		initializeAlarmiListView();
		initializeBlokiraneKuceListView();
		Simulacija.setGlavnaForma(this);
		PauzaOFF();
		NastavakOFF();
    }
	
	private void initializeLables() {
			izmjenaBrojaca(0);		
	}
	
	/*obavjestenja koja se prikazuju u glavnoj formi, ako je obavjestenje lose, tekst je ispisan crvenom bojom inace zelenom
	 *  koristi se za ispisivanje dolaska vozila u ambulantu, ako su sve ambulante pune ili ako osoba ne moze uci u kucu, a poslana je u istu
	 */
	public void stigloObavjestenje(String obavjestenje, boolean dobroObavjestenje) {
		Platform.runLater(() ->{
			if (dobroObavjestenje) {			
			    obavjestenjePolje.setText(obavjestenje);
			    obavjestenjePolje.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
			} else {
				obavjestenjePolje.setText(obavjestenje);
				obavjestenjePolje.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
			}
		});
	}

	/*
	 * izmjena vrijednosti labela koje prikazuju broj zarazenih u ambulantama i broj oporavljenih
	 */
	public void izmjenaBrojaca(int ukupanBrojZarazenihUAmbulantama) {
		Platform.runLater(() ->{
			brojOporavljenihLabela.setText("Broj oporavljenih: " + Simulacija.brojOporavljenih);
			brojZarazenihUAmbulantiLabela.setText("Broj zarazenih u ambulanti: " + ukupanBrojZarazenihUAmbulantama);
		});
	}
	
	/*
	 * metod koji pozivaju objekti koji se krecu, ili na neki nacin mijenjaju
	 * matricu, nakon svakog koraka, potrebno je pozvati ovu metodu, koja ce GUI
	 * dijelu aplikacije naznaciti da je potrebno azurirati izgled matrice
	 * 
	 */
	public void izmijenjenaMatrica(String opisKretanja) {
		Platform.runLater(new Runnable() {
          @Override 
          public void run() {
          	updateView(opisKretanja);
          }
      });
	}
	
	
	/*
	 * inicijalizacija Grid Pane, u svaku celiju se stavlja Button objekat
	 */
	private void initializeMap() {
		mapaGP.getRowConstraints().clear();
		mapaGP.getColumnConstraints().clear();
		int brojRedova = simulacija.getMapa().VELICINA;
		int brojKolona = simulacija.getMapa().VELICINA;

		for (int i = 0; i < brojKolona; i++) {
			ColumnConstraints colConst = new ColumnConstraints();
			colConst.setPercentWidth(100.0 / brojKolona);
			colConst.setFillWidth(true);
			colConst.setHgrow(Priority.ALWAYS); 
			
			mapaGP.getColumnConstraints().add(colConst);
		}
		for (int i = 0; i < brojRedova; i++) {
			RowConstraints rowConst = new RowConstraints();
			rowConst.setPercentHeight(100.0 / brojRedova);
			rowConst.setFillHeight(true);
			rowConst.setVgrow(Priority.ALWAYS);
			mapaGP.getRowConstraints().add(rowConst);
		}

		gridButtons = new Button[brojRedova][brojKolona];

		for (int i = 0; i < brojRedova; ++i) {
			for (int j = 0; j < brojKolona; ++j) {
				gridButtons[i][j] = new Button();
				gridButtons[i][j].setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
				gridButtons[i][j].setStyle("-fx-border-color: #000000;");
				mapaGP.add(gridButtons[i][j], j, i, 1, 1);
			}
		}
		
        updateView("");
	}
	
	//inicijalizacija liste alarma
	private void initializeAlarmiListView() {
		alarmiListView.setItems(alarmi);
		alarmiListView.setCellFactory(
        		new Callback<ListView<Alarm>, ListCell<Alarm>>() {
        			@Override
        			public ListCell<Alarm> call(ListView<Alarm> listView) {
        				return new ListCell<Alarm>(){
	        				@Override
	        				public void updateItem(Alarm item, boolean empty) {
	        					
	        					super.updateItem(item, empty);
	        					
	        					String cellText = null;
	        					
	        					if (item == null || empty) {
	        						// No action
	        					}
	        					else {
	        						cellText = item.toString();
	        					}
	        					
	        					this.setText(cellText);
	        					setGraphic(null);
	        				}
        				};
        			}
        		});
	}
	
	/*
	 * inicijalizacija liste blokiranih kuca, kuca je blokirana
	 *  ako je bar jedan od njenih ukucana zarazen,
	 *  ako je kuca blokirana, kretanje osoba u kuci se trajno prekida, odnosno do kraja aplikacije
	 */
	private void initializeBlokiraneKuceListView() {
		blokiraneKuce.setItems(kuce);
		blokiraneKuce.setCellFactory(
        		new Callback<ListView<Kuca>, ListCell<Kuca>>() {
        			@Override
        			public ListCell<Kuca> call(ListView<Kuca> listView) {
        				return new ListCell<Kuca>(){
	        				@Override
	        				public void updateItem(Kuca item, boolean empty) {
	        					
	        					super.updateItem(item, empty);
	        					
	        					String cellText = null;
	        					
	        					if (item == null || empty) {
	        						// No action
	        					}
	        					else {
	        						cellText = item.toString();
	        					}
	        					
	        					this.setText(cellText);
	        					setGraphic(null);
	        				}
        				};
        			}
        		});
	}
	/*
	 * update svih elemenata prikazanih na glavnoj formi
	 */
	private void updateView(String opisKretanja) {
		updateMap();
    	updateAlarmiListView();
		updateListaKretanjaListView(opisKretanja);
		updateBlokiraneKuceListView();   	
	}

	/*
	 * lista kretanja prikazuje opis kretanja objekata
	 */
	private void updateListaKretanjaListView(String opis) {
		if(opis == null || opis.isEmpty()) {
			return;
		}
		listaKretanja.getItems().add(opis);
		listaKretanja.scrollTo(listaKretanja.getItems().size() - 1);
	}
	
	/*
	 * zaustavljanje mjerenja vremena trajanja aplikacije
	 * timer predstavlja Thread koji vrsi periodicnu promjenu temperatura osobama
	 */
	private void zaustaviVrijeme() {		
		ukupnoVrijeme += System.currentTimeMillis() - startTime;
		simulacija.stopTimer();
	}
	/*
	 * pokretanje mjerenja vremena trajanja aplikacije
	 */
	private void pokreniVrijeme() {
		startTime = System.currentTimeMillis();
		simulacija.startTimer();
	}
	
	/*
	 * update izgleda mape
	 */
	private void updateMap() {
		int brojRedova = simulacija.getMapa().getBrojRedova();
		int numKolona = simulacija.getMapa().getBrojKolona();
		
		for (int i = 0; i < brojRedova; ++i) {
			for (int j = 0; j < numKolona; ++j) {
				updateButton(i, j);				
			}
		}
	}
	
	/*
	 * update izgleda mape, svaka boja predstavlja odgovarajuci tip objekta
	 * na Buttonima ce biti ispisan i tekst 
	 * S - stari
	 * O - odrasli
	 * D - dijete
	 * A - ambulanta
	 * AV - ambulantno vozilo
	 * K - kuca
	 * crna boja - kontronli punkt
	 * 
	 */
	private void updateButton(int x, int y) {
		
		Mapa mapa = simulacija.getMapa();
		
		gridButtons[x][y].setText("");
		
		if (mapa.praznoPoljeNa(x, y))
			gridButtons[x][y].setStyle("-fx-background-color: #ffffff"); 
		else if (mapa.kucaNa(x, y)) {			
			gridButtons[x][y].setStyle("-fx-background-color: #00ff00");
			gridButtons[x][y].setText("K");
			//gridButtons[x][y].setText("K" + ((Kuca)mapa.polje(x,y).getElement()).getID());
		}	 //zelena
		else if (mapa.ambulantaNa(x, y)) {			
			gridButtons[x][y].setStyle("-fx-background-color: #ff0000"); 
			gridButtons[x][y].setText("A");
		}//crvena
		else if (mapa.kontrolniPunktNa(x, y)) {
			gridButtons[x][y].setStyle("-fx-background-color: #3c3c3c");
		}	 //tamo siva
		else if (mapa.ambulantnoVoziloNa(x, y)) {
			gridButtons[x][y].setText("AV");
			gridButtons[x][y].setStyle("-fx-background-color: #ee82ee");
		}  //roza

		else if(mapa.odrasliNa(x, y)) {
			gridButtons[x][y].setStyle("-fx-background-color: #ffa500");
			gridButtons[x][y].setText("O");
			//gridButtons[x][y].setText("O" + ((Osoba)mapa.polje(x,y).getElement()).getID());			
		}
		else if(mapa.stariNa(x, y)) {
			gridButtons[x][y].setStyle("-fx-background-color: #ffa500");
			gridButtons[x][y].setText("S");
			//gridButtons[x][y].setText("S" + ((Osoba)mapa.polje(x,y).getElement()).getID());
		}
		else if(mapa.dijeteNa(x, y)) {
			gridButtons[x][y].setStyle("-fx-background-color: #ffa500");
			gridButtons[x][y].setText("D");
			//gridButtons[x][y].setText("D" + ((Osoba)mapa.polje(x,y).getElement()).getID());
		}
	}
	
	/*
	 * update liste alarma na steku
	 */
	private void updateAlarmiListView() {
		for (Alarm alarm : simulacija.getMapa().alarmi) {
			if (!alarmi.contains(alarm))
				alarmi.add(alarm);
		}
		alarmi.removeIf(c -> 
		!simulacija.getMapa().alarmi.contains(c));
	}
	
	/*
	 * update liste blokiranih kuca
	 */
	private void updateBlokiraneKuceListView() {
		for (Kuca kuca : simulacija.getMapa().kuce.values().stream().filter(k -> k.isZarazenaKuca()).collect(Collectors.toList())) {
			if (!kuce.contains(kuca))
				kuce.add(kuca);
		}
	}
	
	@FXML
	private void userPressedStart(ActionEvent event) {
		System.out.println("Pokrenuta aplikacija!");
		((Button)event.getSource()).setDisable(true);
		PauzaON();
		pokreniVrijeme();
		simulacija.startSimulacija();
	}
	
	
	@FXML
	private void userPressedAmbulantnoVozilo() {
		simulacija.setAmbulantaProlazi(true);
	}
	
	@FXML
	private void userPressedPauza() {
		simulacija.zaustaviSimulaciju();
		PauzaOFF();
		AmbVoziloOFF();
		NastavakON();
		zaustaviVrijeme();
	}
	
	@FXML
	private void userPressedNastavi() {	
		PauzaON();
		AmbVoziloON();
		NastavakOFF();
		pokreniVrijeme();
		simulacija.pokreniSimulaciju();
	}
	
	@FXML
	private void userPressedPregledajStanjeAmbulanti() {
		new FormaPregledanjaAmbulanti(simulacija);
	}
	
	@FXML
	private void userPressedPregledajStatistickePodatke() {
		new FormaPregledanjaStatistickihPodataka(simulacija);
	}
	
	@SuppressWarnings("unchecked")
	@FXML
	private void userPressedKRAJ() {
		Date datum = new Date();  
	    SimpleDateFormat formatter = new SimpleDateFormat("HH_mm_ss_yyyy_MM_dd");    
		try {
			simulacija.zaustaviSimulaciju();
			if(!pauzaButton.isDisable())
			zaustaviVrijeme();
			
			
			long millis = ukupnoVrijeme % 1000;
			long sec = (ukupnoVrijeme / 1000) % 60;
			long min = (ukupnoVrijeme / (1000 * 60)) % 60;
			long h = (ukupnoVrijeme / (1000 * 60 * 60)) % 24;
			String time = String.format("%02d:%02d:%02d.%d", h, min, sec, millis);
			
			PrintWriter pw  = new PrintWriter(new FileWriter(zavrsniFajl.toFile() + formatter.format(datum) + ".txt"));
			pw.println("Ukupno vrijeme: " + time + 
			"\nBroj djece: " + simulacija.getBrojDjece() +
			"\nBroj odraslih: " + simulacija.getBrojOdraslih() + 
			"\nBroj starih: " + simulacija.getBrojStarih() +
			"\nBroj kuca: " + simulacija.getBrojKuca() +
			"\nBroj ambulantnih vozila: " + simulacija.getBrojAmbulantnihVozila() +
			"\nBroj ambulanti: " + simulacija.getBrojAmbulanti() +
			"\nUkupno zarazenih: " + Simulacija.brojZarazenih +
			"\n\nTrenutno zarazenih: " + simulacija.getBrojOsobaPoFilteru(Simulacija.trenutnoZarazeni) + 
			"\nMuskarci : " + simulacija.getBrojOsobaPoFilteru(Simulacija.trenutnoZarazeni, Simulacija.muskiFilter) +
			"\nZene : " + simulacija.getBrojOsobaPoFilteru(Simulacija.trenutnoZarazeni, Simulacija.zeneFilter) +
			"\nStari : " + simulacija.getBrojOsobaPoFilteru(Simulacija.trenutnoZarazeni, Simulacija.stariFilter) +
			"\nOdrasli : " + simulacija.getBrojOsobaPoFilteru(Simulacija.trenutnoZarazeni, Simulacija.odrasliFilter) +
			"\nDjeca : " + simulacija.getBrojOsobaPoFilteru(Simulacija.trenutnoZarazeni, Simulacija.djecaFilter) +
			"\n\nBroj oporavljenih: " + Simulacija.brojOporavljenih +
			"\nMuskarci : " + simulacija.getBrojOsobaPoFilteru(Simulacija.oporavljeni, Simulacija.muskiFilter) +
			"\nZene : " + simulacija.getBrojOsobaPoFilteru(Simulacija.oporavljeni, Simulacija.zeneFilter) +
			"\nStari : " + simulacija.getBrojOsobaPoFilteru(Simulacija.oporavljeni, Simulacija.stariFilter) +
			"\nOdrasli : " + simulacija.getBrojOsobaPoFilteru(Simulacija.oporavljeni, Simulacija.odrasliFilter) +
			"\nDjeca : " + simulacija.getBrojOsobaPoFilteru(Simulacija.oporavljeni, Simulacija.djecaFilter));
			pw.close();
		} catch (IOException e) {
			Logger.getLogger(GlavnaForma.class.getName()).log(Level.SEVERE, null, e);
		}
		System.exit(1);
	}
	
	public void showScene() {
		//Load the scene
		thisStage.setScene(thisScene);
		
		//Setup the window/stage
		thisStage.setTitle("APLIKACIJA");
	}
	
	/*
	 * koristi se kada objekat ide najkracim putem do neke pozicije(osoba-kuci, vozilo-po osobu i nazad)
	 * onemogucava se kretanje drugih objekata i pritiskanje buttona dok traje kretanje
	 */
	public void sviButtonOFF() {
		PauzaOFF();
		AmbulantaOFF();
		StatistikaOFF();
	}
	
	public void sviButtonON() {
		PauzaON();
		AmbulantaON();
		StatistikaON();
	}

	/*
	 * ukuljucivanje i iskljucivanje odredjenih Button-a
	 */
	public void NastavakOFF() {
		nastavakButton.setDisable(true);
	}
	public void NastavakON(){
		nastavakButton.setDisable(false);
	}

	public void PauzaOFF() {
		pauzaButton.setDisable(true);
	}
	public void PauzaON(){
		pauzaButton.setDisable(false);
	}
	
	public void AmbulantaOFF() {
		pregledajAmbulanteButton.setDisable(true);
	}
	public void AmbulantaON(){
		pregledajAmbulanteButton.setDisable(false);
	}
	public void AmbVoziloOFF() {
		ambulantnoVoziloButton.setDisable(true);
	}
	public void AmbVoziloON(){
		ambulantnoVoziloButton.setDisable(false);
	}
	
	public void StatistikaOFF() {
		pregledajStatistickePodatkeButton.setDisable(true);
	}
	public void StatistikaON(){
		pregledajStatistickePodatkeButton.setDisable(false);
	}

}