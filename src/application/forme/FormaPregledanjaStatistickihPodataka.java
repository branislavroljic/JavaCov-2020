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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import simulacija.Mapa;
import simulacija.Simulacija;
public class FormaPregledanjaStatistickihPodataka implements Initializable{

	private Simulacija simulacija;
	private Scene thisScene;
	private Stage stage;
	
	@FXML
	private PieChart statistikaPoPolu;
	
	@FXML
	private PieChart statistikaPoStarosti;
	
	@FXML
	private Label zarazeniLabel;
	
	@FXML
	private Label zarazeniUAmbulantiLabel;
	
	@FXML
	private Label oporavljeniLabel;
	
	@FXML
	private Label infoLabel;
	
	public FormaPregledanjaStatistickihPodataka(Simulacija simulacija) {

		this.simulacija = simulacija;
		
		//Load the FXML file
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/fxmlFajlovi/PregledajStatistickePodatke.fxml"));
			
			//Set this class as controller
			loader.setController(this);
			
			stage = new Stage();
			stage.setResizable(false);
			//Save the scene
			thisScene = new Scene(loader.load());
			stage.setScene(thisScene);
			stage.setOnShowing( event -> Simulacija.setZaustavljena(true) );
			stage.setOnCloseRequest(event ->   Simulacija.setZaustavljena(false)); 
			stage.setOnHiding(event ->   Simulacija.setZaustavljena(false));
			stage.show();
			
		} catch (IOException ex) {
			Logger.getLogger(FormaPregledanjaStatistickihPodataka.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	/*
	 * prikazuju se podaci o ukuponom broj u zarazenih, broju zarazenih u ambulantama i broju oporavljenih
	 * te statistika po polu i starosti u odnosu na broj trenutno zarazenih
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initialize (URL location, ResourceBundle resources) {
		Mapa mapa = simulacija.getMapa();
		zarazeniLabel.setText("Ukupno zarazenih: " + Simulacija.brojZarazenih);
		
		int ukupanBrojZarazenihUAmbulantama = mapa.ambulante.parallelStream()
													.map(a -> a.getBrojZarazenih()).reduce(0, (a,b) -> a+b);
		
		zarazeniUAmbulantiLabel.setText("Zarazenih u ambulantama: " + ukupanBrojZarazenihUAmbulantama);
		
		oporavljeniLabel.setText("Oporavljenih: " + Simulacija.brojOporavljenih);
		
		ObservableList<Data> list1 = FXCollections.observableArrayList(
				new PieChart.Data("muskarci", simulacija.getBrojOsobaPoFilteru(Simulacija.trenutnoZarazeni,
						Simulacija.muskiFilter)),
				new PieChart.Data("zene", simulacija.getBrojOsobaPoFilteru(Simulacija.trenutnoZarazeni,
						Simulacija.zeneFilter)));
		statistikaPoPolu.setData(list1);
		
		ObservableList<Data> list2 = FXCollections.observableArrayList(
				new PieChart.Data("odrasli", simulacija.getBrojOsobaPoFilteru(Simulacija.trenutnoZarazeni,
						Simulacija.odrasliFilter)),
					new PieChart.Data("djeca", simulacija.getBrojOsobaPoFilteru(Simulacija.trenutnoZarazeni,
							Simulacija.djecaFilter)),
					new PieChart.Data("stari", simulacija.getBrojOsobaPoFilteru(Simulacija.trenutnoZarazeni,
							Simulacija.stariFilter)));		
			statistikaPoStarosti.setData(list2);
	}
	
	/*
	 * preuzimanje fajla je realizovano kao kreiranje fajla u folderu Downloads
	 */
	
	@SuppressWarnings("unchecked")
	@FXML
	public void preuzmiPressed() {
		Path path = Paths.get(System.getProperty("user.home"));
		File downloadFile = new File(path + File.separator + "Downloads");
		PrintWriter pw;
		SimpleDateFormat formatter = new SimpleDateFormat("HH_mm_ss_yyyy_MM_dd");
		try {
			pw = new PrintWriter(new FileWriter(downloadFile + File.separator + "statistika" 
													+ formatter.format(new Date()) + ".csv"));
			pw.println(
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
			
			Logger.getLogger(FormaPregledanjaStatistickihPodataka.class.getName()).log(Level.SEVERE, null, e);
			infoLabel.setText("Preuzimanje nije uspjelo!");
			infoLabel.setTextFill(Color.web("#ff0000", 0.8));
			return;
		}
		infoLabel.setText("Preuzimanje uspjelo!");
		infoLabel.setTextFill(Color.web("#00ff33", 0.8));
	}
	
	
	
}
