package application.forme;

import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import simulacija.Simulacija;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PocetnaForma implements Initializable {
	
	private final Stage thisStage;
	private final String TITLE = "DOBRODOSLI!";
	private Simulacija simulacija;
	
	@FXML
	private Button btnStart;
	
	@FXML
	private TextField brojStarih;
	
	@FXML
	private TextField brojOdraslih;
	
	@FXML
	private TextField brojDjece;
	
	@FXML
	private TextField brojKuca;

	@FXML
	private TextField brojKontrolnihPunktova;

	@FXML
	private TextField brojAmbulantnihVozila;

	// Broj odraslih koje odgovara unosu u text field

	private int odrasli;

	// Broj starih koje odgovara unosu u text field

	private int stari;

	// Broj djece koji odgovara unosu u text field

	private int djeca;

	// Broj kuca koji odgovara unosu u text field

	private int kuce;

	// Broj starih kontrolnih punktova odgovara unosu u text field

	private int kontrolniPunktovi;

	// Broj vozila koje odgovara unosu u text field

	private int vozila;
	
	public PocetnaForma() {
		
		// Create the new stage
		thisStage = new Stage();
		
		//Load the FXML file
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/fxmlFajlovi/Main.fxml"));
			
			//Set this class as controller
			loader.setController(this);
			
			//Load the scene
			thisStage.setScene(new Scene(loader.load()));
			thisStage.setResizable(false);
			
			//Setup the window/stage
			thisStage.setTitle(TITLE);
			
		} catch (IOException ex) {
			Logger.getLogger(PocetnaForma.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Show the stage that was loaded in the constructor
	 */
	public void showStage() {
		thisStage.showAndWait();
	}

	/**
     * The initialize() method allows you set setup your scene, adding actions, configuring nodes, etc.
     */
	@Override
    public void initialize(URL location, ResourceBundle resources) {
    }
	
	
	/*
	 * validacija unesenih podataka dozvoljeni broj osoba je 70, bez obzira na
	 * velicinu matrice
	 */
	private boolean validacijaPodataka() {
		try {
			if (brojStarih.getText().equals("") ||
				brojOdraslih.getText().equals("") ||
				brojDjece.getText().equals("") ||
				brojKuca.getText().equals("") || 
				brojAmbulantnihVozila.getText().equals("") || 
				brojKontrolnihPunktova.getText().equals("")) {
				return false;
			}
			stari = Integer.valueOf(brojStarih.getText());
			odrasli = Integer.valueOf(brojOdraslih.getText());
			djeca = Integer.valueOf(brojDjece.getText());
			kuce = Integer.valueOf(brojKuca.getText());
			kontrolniPunktovi = Integer.valueOf(brojKontrolnihPunktova.getText());
			vozila = Integer.valueOf(brojAmbulantnihVozila.getText());
		} catch (NumberFormatException ex) {
			Logger.getLogger(PocetnaForma.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
		
		if (kuce <= 0 || (odrasli+stari < kuce) || vozila < 4
				|| stari < 0 || odrasli < 0 || djeca < 0 || kontrolniPunktovi < 0 || stari + odrasli + djeca > 70
				|| kuce + kontrolniPunktovi > 70 || vozila > 20)
			return false;

		return true;
	}
		
	//ako podaci nisu validni, pojavljuje se odgovarajuci Alert
	@FXML
	private void startPressed() {
		if (validacijaPodataka()) {			
			this.simulacija = new Simulacija(stari, odrasli, djeca, kuce, kontrolniPunktovi, vozila);					
			otvoriGlavnuFormu(simulacija);
		}
		else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("GRESKA");
			alert.setHeaderText("Pogresno unijeti podaci");
			alert.setContentText("Nispravno unijeti podaci:"
                  + "\nbroj kuca <= broj odraslih + broj starih\n"
                  + "broj kuca > 0 \n"
                  + " 4 < broj ambulantnih vozila =< 20\n"
                  +	"ukupan broj osoba <= 70\n"
                  + "broj kuca + broj punktova <= 70\n"
                  + "SVE VRIJEDNOSTI MORAJU BITI POZITIVNE!");
			alert.showAndWait();
		}
	}

	// ucitavanje glavne forme, poseban FXML fajl
	private void otvoriGlavnuFormu(Simulacija simulacija) {
		GlavnaForma glavnaForma = new GlavnaForma(thisStage, simulacija);

		// prikazi novi prozor
		glavnaForma.showScene();
	}
}
