package de.splitnass.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;

import de.splitnass.rules.Solo;

public class Spieltag implements RundeListener {

    private static Logger log =  Logger.getLogger("Spieltag");

    private long id;
    private Date start;
    private Date ende;
    private List<Runde> runden;
    private Runde aktuelleRunde;
    private List<Spieler> spieler;

    private transient Set<SpieltagListener> listener = new HashSet<SpieltagListener>();


    @Override
    public void bock(EventObject e) {
		addBock();		
	}

    @Override
	public void boecke(EventObject e) {
		addBoecke(getAktiveSpieler().size());
	}

    @Override
	public void undoBoecke(EventObject e) {
		for (int i = 0; i < getAktiveSpieler().size(); i++) {
			removeBock();
		}
	}

    @Override
	public void undoBock(EventObject e) {
		removeBock();
	}

    @Override
	public void rundeDataChanged(EventObject e) {
		fireRundeUpdatedEvent(aktuelleRunde);
	}

    @Override
	public void ergebnisBerechnet(EventObject e) {}

	public void addListener(SpieltagListener l) {
		listener.add(l);
	}
	
	public void removeListener(SpieltagListener l) {
		listener.remove(l);
	}

	public void removeAllListener() {
		listener.clear();
	}

	private void fireRundeUpdatedEvent(Runde r) {		
		for(SpieltagListener l : listener) {
			l.rundeUpdated(r);
		}
	}
	
	private void fireSpieltagUpdatedEvent() {		
		for(SpieltagListener l : listener) {
			l.spieltagUpdated();
		}
	}
    private void fireAktuelleRundeChangedEvent() {
        for(SpieltagListener l : listener) {
            l.aktuelleRundeChanged();
        }
    }

	
	public Date getStart() {
		return start; 
	}
	
	private void addBock() {
		Runde r = getNaechsteRunde(getAktuelleRunde());
		while (r != null && r.getBoecke() >= 2) {
			r = getNaechsteRunde(r);
		}
		if (r != null) {
			r.addBock();
			fireRundeUpdatedEvent(r);
		}
	}
	
	private void addBoecke(int count) {
		Runde r = getNaechsteRunde(getAktuelleRunde());
		while (r != null && r.getBoecke() >= 2) {
			r = getNaechsteRunde(r);
		}
		if (r != null) {
			for (int i = count; i > 0; i--) {
				r.addBock();
				fireRundeUpdatedEvent(r);
				r = getNaechsteRunde(r);
				if (r == null) { //keine weitere Runde vorhanden
					addBoecke(i-1);//noch mal oben dranh�ngen
					break;
				}
			}			
		}
	}
	
	private void removeBock() {
		Runde r = getNaechsteRunde(getAktuelleRunde());
		Runde bockRunde = null;
		if (r.getBoecke() == 2) {
			while (r != null && r.getBoecke() == 2) {
				r = getNaechsteRunde(r);
			}
			bockRunde = getVorherigeRunde(r); //letzte mit zwei B�cken			
		} else if (r.getBoecke() == 1) {
			while (r != null && r.getBoecke() == 1) {
				r = getNaechsteRunde(r);
			}
			bockRunde = getVorherigeRunde(r); //letzte mit einem Bock
		}
		if (bockRunde != null) {
			bockRunde.removeBock();
			fireRundeUpdatedEvent(bockRunde);
		}
	}
	
	public boolean isBeendet() {
		return ende != null;
	}
	
	public boolean isGestartet() {
		return start != null;
	}
			
	public void setGesamtRunden(int value) {		
		if (value <= 0 || value == getRundenAnzahl()) return;
		if (runden == null) {//neuer Spieltag
			runden = new ArrayList<Runde>(value);
			addRunden(value);
		} else {//Rundenanzahl des laufenden Spieltags aendern
			if (getRundenAnzahl() <= value) {
				addRunden(value - getRundenAnzahl());
			} else {
				for (int i = 0; i < getRundenAnzahl() - value; i++) {
                    Runde r = runden.remove(runden.size()-1);
					r.removeListener(this);
				}
			}
		}
        //Falls letzte Runde bereits gespielt, neue Runde beginnen
        if (isGestartet() && aktuelleRunde != null && aktuelleRunde.isBeendet()) {
            rundeAbrechnenUndNeu();
        }
		fireSpieltagUpdatedEvent();
	}
	
	private void addRunden(int count) {
		for (int i = 0; i < count; i++) {
			Runde newRunde = new Runde(this, runden.size()+1);
			newRunde.addListener(this);
			runden.add(newRunde);
		}
	}
	
	public void start(List<Spieler> spieler, Spieler geber) {
		start = new Date();
		this.spieler = spieler;
		for (Spieler s : spieler) {
			s.setIsAktiv(true);
		}
		aktuelleRunde = runden.get(0);
		aktuelleRunde.setGeber(geber);
		aktuelleRunde.setSpieler(getSpieler(geber));
		aktuelleRunde.setAufspieler(getNaechstenSpieler(geber));
		aktuelleRunde.start();
        fireAktuelleRundeChangedEvent();
	}
	
	public void ende() {
		ende = new Date();
	}

	public void rundeAbrechnenUndNeu() {
		aktuelleRunde.ende();			
		Spieler geber = null;
		if (aktuelleRunde.isSolo() && 
				!aktuelleRunde.getSolo().isRegulaeresAufspiel()) 
		{
			geber = aktuelleRunde.getGeber();				
		} else {
			geber = getNaechstenSpieler(aktuelleRunde.getGeber());				
		}	
		log.info(aktuelleRunde.getErgebnisString());
		if (aktuelleRunde.getId() < runden.size()) {
			fireRundeUpdatedEvent(aktuelleRunde);
			aktuelleRunde = runden.get(aktuelleRunde.getId());
			aktuelleRunde.setGeber(geber);
			aktuelleRunde.setSpieler(getSpieler(geber));
			aktuelleRunde.setAufspieler(getNaechstenSpieler(geber));
			aktuelleRunde.start();
            fireAktuelleRundeChangedEvent();
		}
		fireRundeUpdatedEvent(aktuelleRunde);
	}
	
	public void undoLetzteRunde() {
		aktuelleRunde.reset();		
		Runde vorherigeRunde = getVorherigeRunde(aktuelleRunde);
		if (vorherigeRunde == null) return;
		aktuelleRunde = vorherigeRunde;
		aktuelleRunde.reset();
        fireAktuelleRundeChangedEvent();
		fireRundeUpdatedEvent(aktuelleRunde);
		fireRundeUpdatedEvent(getNaechsteRunde(aktuelleRunde));
		fireSpieltagUpdatedEvent();
	}


	private Spieler getNaechstenSpieler(Spieler s) {
		Spieler result = null;
		int i = spieler.indexOf(s);
		if (i == spieler.size()-1) {
			result = spieler.get(0);
		} else {
			result = spieler.get(i+1);
		}
		if (result.isAktiv()) {
			return result;
		} else {
			return getNaechstenSpieler(result); 
		}
	}
	
	public Runde getVorherigeRunde(Runde r) {
		if (r.getId() <= 1) {
			return null;
		} else {
			return runden.get(r.getId()-2);
		}
	}
	
	public Runde getNaechsteRunde(Runde r) {
		if (r.getId() == getRundenAnzahl()) {
			return null;
		} else {
			return runden.get(r.getId());
		}
	}
	
	
	private List<Spieler> getSpieler(Spieler geber) {
		List<Spieler> result = new ArrayList<Spieler>(4);
		Spieler spieler = geber;
		for (int i = 0; i < 4; i++) {
			spieler = getNaechstenSpieler(spieler);
			result.add(spieler);
		}
		return result;
	}

	public Runde getAktuelleRunde() {
		return aktuelleRunde;
	}

	public int getRundenAnzahl() {
		return runden != null ? runden.size() : 0;
	}

	public List<Runde> getRunden() {
        return runden;
	}

	public List<Spieler> getSpieler() {
		return spieler;
	}
	
	public List<Spieler> getAktiveSpieler() {
		List<Spieler> result = new ArrayList<Spieler>();
		for (Spieler s : spieler) {
			if (s.isAktiv()) {
				result.add(s);
			}
		}
		return result;
	}
	
	public void spielerSteigtAus(Spieler s) {
		if (s != null) {
			s.setIsAktiv(false);
			fireSpieltagUpdatedEvent();
		}
	}
	
	public void spielerSteigtEin(Spieler s) {
		if (s != null) {
            //Neuer Spieler sitzt hinter dem aktuellen Geber und wird zum Geber der aktuellen Runde!
            if (spieler.contains(s) && !s.isAktiv()) {
                //Spieler hatte pausiert
                s.setIsAktiv(true);
                spieler.remove(s);
                spieler.add(spieler.indexOf(aktuelleRunde.getGeber())+1, s);
                aktuelleRunde.setGeber(s);
            } else if (!spieler.contains(s))  {
                int punktestand = getStartwertNeuerSpieler();
                s.setIsAktiv(true);
                //Spieler steigt neu ein
                spieler.add(spieler.indexOf(aktuelleRunde.getGeber())+1, s);
                aktuelleRunde.setGeber(s);
                /*
                 * wir legen eine Dummyrunde an, die als Punktezahl den Startwert des neuen Spielers bekommt
                 * und den neuen Spieler als einzigen Gewinner.
                 */
                createDummyRunde(s, punktestand,
                        String.format("%s steigt mit %d Punkten ein", s, punktestand));
			}
            for (SpieltagListener l : listener) {
                l.spielerSteigtEin();
            }
            fireSpieltagUpdatedEvent();
		}
	}

	
	private int getStartwertNeuerSpieler() {
		int result = 0;
		Runde vorherigeRunde = getVorherigeRunde(aktuelleRunde);
		if (vorherigeRunde != null) {
            result = Integer.MAX_VALUE;
            for (Integer punkte : getPunktestand(vorherigeRunde).values()) {
                if (punkte < result) {
                    result = punkte;
                }
            }
		}
		return result;
	}

    public void setPunktestand(Spieler s, int neuerPunktestand) {
        int aktuellerPunktestand = getPunktestand(getAktuelleRunde(), s);
        /*
         * wir legen eine Dummyrunde an, die als Punktezahl die Differenz zum
          * bisherigen Punktestand des Spielers bekommt und
          * den Spieler als einzigen Gewinner.
         */
        createDummyRunde(s, neuerPunktestand-aktuellerPunktestand,
                String.format("Punktestand von %s auf %d Punkte geändert", s.getName(), neuerPunktestand));

    }

    private void createDummyRunde(Spieler s, int neuerPunktestand, String ergebnisString) {
        int neueId = aktuelleRunde.getId() == 1 ? 1 : aktuelleRunde.getId();
        Runde dummyRunde = new Runde(this, neueId);
        dummyRunde.setDummy(neuerPunktestand, s, ergebnisString);
        for (Runde r : runden.subList(dummyRunde.getId()-1, runden.size())) {
            r.setId(r.getId()+1);
        }
        runden.add(dummyRunde.getId()-1, dummyRunde);
    }


    /**
     * Liefert den Punktestand eines Spielers zum Zeitpunk einer Runde.
     */
    public int getPunktestand(Runde runde, Spieler spieler) {
        int result = 0;
        for (Runde r : runden.subList(0, runde.getId())) {
            if (r.getGewinner().contains(spieler)) {
                result += r.getErgebnis();
            }
        }
        return result;
    }

    /**
     * Liefert den Punktestand aller aktuell aktiven Spieler zum Zeitpunk einer Runde.
     */
    public Map<Spieler, Integer> getPunktestand(Runde runde) {
        Map<Spieler, Integer> result = new HashMap<Spieler, Integer>(getAktiveSpieler().size());
        for (Spieler s : getAktiveSpieler()) {
            result.put(s, getPunktestand(runde, s));
        }
        return result;
    }

    public long getId() {
        return id;
    }

    public Date getEnde() {
        return ende;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setEnde(Date ende) {
        this.ende = ende;
    }

    public void setRunden(List<Runde> runden) {
        if (this.runden != null) {
            for (Runde r : this.runden) {
                r.removeListener(this);
            }
        }
        this.runden = runden;
        if (this.runden != null) {
            for (Runde r : this.runden) {
                r.addListener(this);
            }
        }
    }

    public void setAktuelleRunde(Runde aktuelleRunde) {
        this.aktuelleRunde = aktuelleRunde;
    }

    public void setSpieler(List<Spieler> spieler) {
        this.spieler = spieler;
    }

	public static String toJson(Spieltag s) {
		Gson gson = new Gson();
		return gson.toJson(s);
	}

	public static Spieltag fromJson(String jsonString) {
		GsonBuilder gsonBldr = new GsonBuilder();
		gsonBldr.registerTypeAdapter(Solo.class, new Solo.JsonSerializer());
		gsonBldr.registerTypeAdapter(Spieler.class, new Spieler.JsonSerializer());
		Spieltag result = gsonBldr.create().fromJson(jsonString, Spieltag.class);
		cleanupAfterDeserialization(result);
		return result;
	}

	public static String listToJson(List<Spieltag> spieltage) {
		Gson gson = new Gson();
		return gson.toJson(spieltage);
	}

	public static List<Spieltag> listFromJson(String jsonString) {
		GsonBuilder gsonBldr = new GsonBuilder();
		gsonBldr.registerTypeAdapter(Solo.class, new Solo.JsonSerializer());
		gsonBldr.registerTypeAdapter(Spieler.class, new Spieler.JsonSerializer());
		Type listType = new TypeToken<List<Spieltag>>(){}.getType();
		List<Spieltag> result = gsonBldr.create().fromJson(jsonString, listType);
		for (Spieltag s : result) {
			cleanupAfterDeserialization(s);
		}
		return result;
	}

	private static void cleanupAfterDeserialization(Spieltag s) {
		//Runden haben ihre Spieltag-Instanz nicht gesetzt
		for (Runde r : s.runden) {
			r.setSpieltag(s);
		}
		//Spieltag aktuelleRunde ist eigene Instanz und nicht in Rundenliste enthalten
		int aktuelleRunde = s.aktuelleRunde.getId();
		for (Runde r : s.runden) {
			if (r.getId() == aktuelleRunde) {
				s.aktuelleRunde = r;
			}
		}
		//Nicht gespielte Runden entfernen
		List<Runde> gespielteRunden = new ArrayList<Runde>();
		for (Runde r : s.runden) {
			if (r.isGestartet()) gespielteRunden.add(r);
		}
		s.runden = gespielteRunden;

		//...?!
	}


	public static void main(String[] args) {
		Spieltag s = new Spieltag();
		s.id = 4711;
		s.setGesamtRunden(100);
		s.start(Spieler.getAll().subList(0,5), Spieler.getAll().get(0));
		Runde aktuelleRunde = s.getAktuelleRunde();
		aktuelleRunde.setReAngesagt(1);
		aktuelleRunde.setRe(2);
		aktuelleRunde.setReGewinnt(true);
		aktuelleRunde.setGewinner(aktuelleRunde.getSpieler().subList(0,2));
		String jsonString = toJson(s);
		System.out.println("Length: " + jsonString.length());
		System.out.println(jsonString);

		Spieltag s1 = fromJson(jsonString);
		System.out.println(s1);

		List<Spieltag> spieltage = new ArrayList<Spieltag>();
		spieltage.add(s);
		spieltage.add(s1);
		String jsonSpieltage = listToJson(spieltage);
		List<Spieltag> spieltage2  = listFromJson(jsonSpieltage);
		System.out.println("fetig");
	}
}

