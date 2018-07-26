package de.splitnass.data;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import de.splitnass.rules.Solo;
import de.splitnass.rules.Rules;

public class Runde implements Serializable {

    public static final int KEINE_120 = 1;
    //public static final int HUNDERTZWANZIG = 1;
    public static final int KEINE_9 = 2;
    public static final int KEINE_6 = 3;
    public static final int KEINE_3 = 4;
    public static final int SCHWARZ = 5;
	
	private static Logger log = Logger.getLogger("Runde");

    private transient Spieltag spieltag;

    public Spieltag getSpieltag() {
        return spieltag;
    }

    void setSpieltag(Spieltag s) { spieltag = s; }

    private long _id; //DB primary key
    private int id;
	private Date start;
	private Date ende;
	//Spieler
    private List<Spieler> spieler = new ArrayList<Spieler>(); //exactly 4
    private Spieler geber;
    private Spieler aufspieler; //bei Solo ausser Null immer der Soloist
    private List<Spieler> gewinner = new ArrayList<Spieler>();
	//Ansagen
    private int reVonVorneHerein;
    private int reAngesagt;
    private int kontraVonVorneHerein;
    private int kontraAngesagt;
    //Boecke
    private int boecke;
    private int boeckeBeiBeginn;
    //Gespielt
    private Solo solo = Solo.KEIN_SOLO;
    private int re;
    private int kontra;
    private boolean reGewinnt;
    private boolean gegenDieAlten;
    private boolean gegenDieSau;
    private int extrapunkte;
    private boolean armut;
    private boolean herzGehtRum;

    private int ergebnis = -1;
    private String ergebnisString;

    private transient List<RundeListener> listener = new ArrayList<RundeListener>();
    private transient List<BerechnungsListener> berechnungsListener = new ArrayList<BerechnungsListener>();

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	     in.defaultReadObject();
	     listener = new ArrayList<RundeListener>();
	}

	public void addListener(RundeListener l) {
		listener.add(l);
	}

	public void removeListener(RundeListener l) {
		listener.remove(l);
	}

    public void addBerechnungsListener(BerechnungsListener l) {
        berechnungsListener.add(l);
    }

    public void removeBerechnungsListener(BerechnungsListener l) {
        berechnungsListener.remove(l);
    }



	public Runde(Spieltag spieltag, int id) {
        this.spieltag = spieltag;
		this.id = id;
	}

	public void start() {
		start = new Date();
		boeckeBeiBeginn = boecke;
	}

	public void ende() {
		ende = new Date();
	}

	public boolean isBeendet() {
		return ende != null;
	}

	public boolean isGestartet() {
		return start != null;
	}

    public boolean isLaufend() {
        return isGestartet() && !isBeendet();
    }

	public void setExtrapunkte(int value) {
		extrapunkte = value;
		fireRundeEvent(RundeEventTyp.DATA_CHANGED);
	}

	public boolean reAngesagt() {
		return reAngesagt > 0;
	}



	public boolean kontraAngesagt() {
		return kontraAngesagt > 0;
	}

    /**
     * Setzt eine Re-Ansage:
     * <ul>
     * <li>1: Re</li>
     * <li>2: Re keine 9</li>
     * <li>3: Re keine 6</li>
     * <li>4: Re keine 3</li>
     * <li>5: Re schwarz</li>
     * </ul>
     */
	public void setReAngesagt(int value) {
		if (value > 0 && !reAngesagt()) {
			addBock();
			if (kontraAngesagt()) {
				fireRundeEvent(RundeEventTyp.BOECKE);
			}
		}
        reAngesagt = value;
	}

    /**
     * Setzt eine Re-Von-Vorneherein-Ansage:
     * <ul>
     * <li>1: Re von vorneherein</li>
     * <li>2: Re keine 9 von vorneherein</li>
     * <li>3: Re keine 6 von vorneherein</li>
     * <li>4: Re keine 3 von vorneherein</li>
     * <li>5: Re schwarz von vorneherein</li>
     * </ul>
     */
    public void setReVonVorneHerein(int value) {
        reVonVorneHerein = value;
        if (reAngesagt < reVonVorneHerein) {
            setReAngesagt(reVonVorneHerein);
        }
    }

    /**
     * Setzt eine Kontra-Ansage:
     * <ul>
     * <li>1: Kontra</li>
     * <li>2: Kontra keine 9</li>
     * <li>3: Kontra keine 6</li>
     * <li>4: Kontra keine 3</li>
     * <li>5: Kontra schwarz</li>
     * </ul>
     */
    public void setKontraAngesagt(int value) {
        if (value > 0 && !kontraAngesagt()) {
            addBock();
            if (reAngesagt()) {
                fireRundeEvent(RundeEventTyp.BOECKE);
            }
        }
        kontraAngesagt = value;
    }

    /**
     * Setzt eine Kontra-Von-Vorneherein-Ansage:
     * <ul>
     * <li>1: Kontra von vorneherein</li>
     * <li>2: Kontra keine 9 von vorneherein</li>
     * <li>3: Kontra keine 6 von vorneherein</li>
     * <li>4: Kontra keine 3 von vorneherein</li>
     * <li>5: Kontra schwarz von vorneherein</li>
     * </ul>
     */
    public void setKontraVonVorneHerein(int value) {
        kontraVonVorneHerein = value;
        if (kontraAngesagt < kontraVonVorneHerein) {
            setReAngesagt(kontraVonVorneHerein);
        }
    }

    /**
     * Setzt das Ergebnis als Re-Sieg:
     * <ul>
     * <li>1: Re gewinnt 120</li>
     * <li>2: Re gewinnt keine 9</li>
     * <li>3: Re gewinnt keine 6</li>
     * <li>4: Re gewinnt keine 3</li>
     * <li>5: Re gewinnt schwarz</li>
     * </ul>
     */
	public void setRe(int count) {
		re = count;
	}

    /**
     * Setzt das Ergebnis als Kontra-Sieg:
     * <ul>
     * <li>1: Kontra gewinnt 120</li>
     * <li>2: Kontra gewinnt keine 9</li>
     * <li>3: Kontra gewinnt keine 6</li>
     * <li>4: Kontra gewinnt keine 3</li>
     * <li>5: Kontra gewinnt schwarz</li>
     * </ul>
     */
    public void setKontra(int count) {
        kontra = count;
    }


	public int getId() {
		return id;
	}

    void setId(int value) {
        id = value;
    }


    private void fireBerechnungsEvent(BerechnungsEvent.Typ typ, int value) {
        for (BerechnungsListener l : berechnungsListener) {
            l.handle(new BerechnungsEvent(this, typ, value));
        }
    }

    public int berechneErgebnis() {
        return berechneErgebnis(false);
    }

    private int berechneErgebnis(boolean offizielleZaehlweise) {
        ergebnis = 0;
        //kein Ergebnis - gespaltener Arsch!?
        if (re <= 0 && kontra <=0) {
            ergebnis = 0;
            fireBerechnungsEvent(BerechnungsEvent.Typ.KEIN_ERGEBNIS, 0);
            return ergebnis;
        }
        int gespieltePunkte = re > 0 ? re : kontra;
        if (solo == Solo.NULL) {
            //Bei Null zählt das angesagte Ergebnis. Wenn nichts angesagt, dann das gespielte Ergebnis (sollte 120 sein)
            gespieltePunkte = reAngesagt > 0 ? reAngesagt : re;
        }
        //Re un Kontra haben falsche Ansagen gemacht: gespaltener Arsch
        if (gespieltePunkte < reAngesagt && gespieltePunkte < kontraAngesagt && solo != Solo.NULL) {
            ergebnis = 0;
            fireBerechnungsEvent(BerechnungsEvent.Typ.RE_UND_KONTRA_FALSCHE_ANSAGEN, 0);
            return ergebnis;
        }
        //nichts angesagt und keine 6 oder besser: gespaltener Arsch
        if (re >= 3 && !reAngesagt() && solo != Solo.NULL && !offizielleZaehlweise) {
            ergebnis = 0;
            fireBerechnungsEvent(BerechnungsEvent.Typ.RE_OMA, 0);
            return ergebnis;
        }
        if (kontra >= 3 && !kontraAngesagt() && solo != Solo.NULL && !offizielleZaehlweise) {
            ergebnis = 0;
            fireBerechnungsEvent(BerechnungsEvent.Typ.KONTRA_OMA, 0);
            return ergebnis;
        }
        //Hat unter Berücksichtigung der Ansagen Re oder Kontra gewonnen?
        if (re > 0) {
            reGewinnt = re >= reAngesagt || solo == Solo.NULL;
        } else {
            reGewinnt = kontra < kontraAngesagt;
        }
        //Gegen die Alten?
        if (!reGewinnt && !armut && solo.isGegenDieAltenMoeglich()) {
            gegenDieAlten = true;
        }
        //berechnen
        int maxAnsage = reAngesagt > kontraAngesagt ? reAngesagt : kontraAngesagt;
        if (offizielleZaehlweise) {
            ergebnis = re + kontra;
            if (reAngesagt > 0) {
                ergebnis += reAngesagt + 1;
            }
            if (kontraAngesagt > 0) {
                ergebnis += kontraAngesagt + 1;
            }
        } else {
            if (maxAnsage > gespieltePunkte) {
                int relevanteAnsage = reGewinnt ? kontraAngesagt : reAngesagt;
                for (int i = maxAnsage; i > gespieltePunkte; i--) {
                    if (relevanteAnsage >= i && solo != Solo.NULL) {
                       fireBerechnungsEvent(toBerechnungsEventTyp(i), 2);
                        ergebnis += 2;
                    }
                }
            }
            for (int i = gespieltePunkte; i > 0; i--) {
                int tmpErgebnis = ergebnis;
                ergebnis++;
                if (i > 1 && reAngesagt >= i ) {
                    ergebnis++;
                    if (!reGewinnt && kontra > 0 && solo != Solo.NULL) {
                        ergebnis++;
                    }
                }
                if (i > 1 && kontraAngesagt >= i) {
                    ergebnis++;
                    if (reGewinnt  && re > 0 && solo != Solo.NULL) {
                        ergebnis++;
                    }
                }
                fireBerechnungsEvent(toBerechnungsEventTyp(i), ergebnis - tmpErgebnis);
            }
        }
        if (gegenDieAlten) {
            ergebnis++;
            fireBerechnungsEvent(BerechnungsEvent.Typ.GEGEN_DIE_ALTEN, 1);
        }
        if (gegenDieSau && !offizielleZaehlweise) {
            ergebnis++;
            fireBerechnungsEvent(BerechnungsEvent.Typ.GEGEN_DIE_SAU, 1);
        }
        if (solo != null && solo != Solo.KEIN_SOLO) {
            ergebnis++;
            fireBerechnungsEvent(BerechnungsEvent.Typ.SOLO, 1);
        }
        if (isSoloVerloren()) {
            ergebnis++;
            fireBerechnungsEvent(BerechnungsEvent.Typ.SOLO_VERLOREN, 1);
        }
        if (reVonVorneHerein > 0) {
            ergebnis += reVonVorneHerein;
            fireBerechnungsEvent(BerechnungsEvent.Typ.RE_VON_VORNEHEREIN, 1);
        }
        if (kontraVonVorneHerein > 0) {
            ergebnis += kontraVonVorneHerein;
            fireBerechnungsEvent(BerechnungsEvent.Typ.KONTRA_VON_VORNEHEREIN, 1);
        }
        if (extrapunkte != 0) {
            ergebnis += extrapunkte;
            fireBerechnungsEvent(BerechnungsEvent.Typ.EXTRAPUNKTE, extrapunkte);
        }
        //durch negative Extrapunkte kann die Gegenseite gewonnen haben...! (gegenDieAlten etc.)
        if (ergebnis < 0) {
            ergebnis = Math.abs(ergebnis);
            reGewinnt = !reGewinnt;
        }
        fireBerechnungsEvent(BerechnungsEvent.Typ.PUNKTE_VOR_BOCK, ergebnis);
        for (int i = 0; i < boecke; i++) {
            ergebnis = ergebnis * 2;
        }
        if (boecke > 0) {
            fireBerechnungsEvent(BerechnungsEvent.Typ.BOECKE, boecke);
        }

        fireBerechnungsEvent(BerechnungsEvent.Typ.ERGEBNIS, ergebnis);
        fireRundeEvent(RundeEventTyp.ERGBENIS_BERECHNET);
        return ergebnis;
    }

    public int getErgebnis() {
        return ergebnis;
	}

    public void setErgebnis(int value) {
        if (ergebnis != value) {
            ergebnis = value;
            fireRundeEvent(RundeEventTyp.DATA_CHANGED);
        }

    }

    private static String translate(int value) {
        switch (value) {
            case KEINE_120: return "120";
            case KEINE_9: return "keine 9";
            case KEINE_6: return "keine 6";
            case KEINE_3: return "keine 3";
            case SCHWARZ: return "schwarz";
            default : return "";
        }
    }

    private static BerechnungsEvent.Typ toBerechnungsEventTyp(int value) {
        switch (value) {
            case KEINE_120: return BerechnungsEvent.Typ.HUNDERTZWANZIG;
            case KEINE_9: return BerechnungsEvent.Typ.KEINE_9;
            case KEINE_6: return BerechnungsEvent.Typ.KEINE_6;
            case KEINE_3: return BerechnungsEvent.Typ.KEINE_3;
            case SCHWARZ: return BerechnungsEvent.Typ.SCHWARZ;
            default : return null;
        }
    }

	public Spieler getAufspieler() {
		return aufspieler;
	}

	public void setAufspieler(Spieler aufspieler) {
		this.aufspieler = aufspieler;
	}

	public void setBoecke(int count) {
		if (count >= 0 && count <= 3) {
			boecke = count;
			fireRundeEvent(RundeEventTyp.DATA_CHANGED);
		}
	}

	public int getBoecke() {
		return boecke;
	}

	public void addBock() {
		if (boecke == Rules.MAX_BOECKE_PRO_RUNDE) {
			fireRundeEvent(RundeEventTyp.BOCK);
		} else {
			boecke++;
			fireRundeEvent(RundeEventTyp.DATA_CHANGED);
		}
	}

	public void removeBock() {
		if (boecke > 0) {
			boecke--;
			fireRundeEvent(RundeEventTyp.DATA_CHANGED);
		}
	}

	public Spieler getGeber() {
		return geber;
	}

	public void setGeber(Spieler geber) {
		this.geber = geber;
	}

	public boolean isGespaltenerArsch() {
		return ergebnis == 0;
	}

	public void setGespaltenerArsch() {
        ergebnis = 0;
        gewinner.clear();
		fireRundeEvent(RundeEventTyp.BOECKE);
		fireRundeEvent(RundeEventTyp.DATA_CHANGED);
	}

    public boolean isArmut() {
        return armut;
    }

    public void setArmut(boolean armut) {
        this.armut = armut;
    }

	public List<Spieler> getGewinner() {
		return gewinner;
	}

	public void setGewinner(List<Spieler> gewinner) {
		this.gewinner = gewinner;
	}
	
	public void setHerzGehtRum() {
		if (!herzGehtRum) {
			herzGehtRum = true;
			fireRundeEvent(RundeEventTyp.BOECKE);
		}
	}

    public boolean isHerzGehtRum() {
        return herzGehtRum;
    }

	public List<Spieler> getSpieler() {
		return spieler;
	}

	public void setSpieler(List<Spieler> spieler) {
		this.spieler = spieler;
		this.gewinner = new ArrayList<Spieler>();
	}

	public boolean isSolo() {
		return solo != Solo.KEIN_SOLO;
	}
	
	public Solo getSolo() {
		return solo;
	}
	
	public void setSolo(Solo s) {		
		if (solo != s) {
            solo = s != null ? s : Solo.KEIN_SOLO;
            fireRundeEvent(RundeEventTyp.DATA_CHANGED);
        }
	}

    public boolean isSoloVerloren() {
        return ergebnis > 0 && solo != Solo.KEIN_SOLO && !reGewinnt;
    }

	public String getErgebnisString() {
        return ergebnisString != null ? ergebnisString : "";
	}

    public String getPunktestand() {
        StringBuilder result = new StringBuilder();
        for (Spieler spieler : spieltag.getAktiveSpieler()) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(spieler.getName()).append("=")
                    .append(spieltag.getPunktestand(this, spieler));
        }
        return result.toString();
    }

    void setDummy(int _ergebnis, Spieler _spieler, String _ergebnisString) {
        reset();
        ergebnisString = _ergebnisString;
        this.ergebnis = _ergebnis;
        this.spieler = new ArrayList<Spieler>(1);
        this.spieler.add(_spieler);
        gewinner = this.spieler;
        start = ende = new Date();
        fireRundeEvent(RundeEventTyp.DATA_CHANGED);
    }

    //Dummyrunde bei Einstieg eines Spielers?
    public boolean isDummy() {
        return isBeendet() && getGeber() == null;
    }

	public void setGegenDieAlten(boolean value) {
		gegenDieAlten = value;
	}
		
	public boolean isGegenDieAlten() {
		return gegenDieAlten;
	}
	
	public void setGegenDieSau(boolean value) {
		gegenDieSau = value;
	}
	
	public void setReVonVorneHerein() {
		if (reVonVorneHerein != 1) {
			reVonVorneHerein = 1;
			fireRundeEvent(RundeEventTyp.DATA_CHANGED);
		}
	}

	public void setKontraVonVorneHerein() {
		if (kontraVonVorneHerein != 1) {
			kontraVonVorneHerein = 1;
			fireRundeEvent(RundeEventTyp.DATA_CHANGED);
		}
	}

	public void reset() {
		if (reAngesagt() && kontraAngesagt()) {
			fireRundeEvent(RundeEventTyp.UNDO_BOECKE);
			if (boeckeBeiBeginn == 2) {
				fireRundeEvent(RundeEventTyp.UNDO_BOCK);
			}
		}
		if (herzGehtRum) {
			fireRundeEvent(RundeEventTyp.UNDO_BOECKE);
		}
		reVonVorneHerein = 0;
		kontraVonVorneHerein = 0;
		gegenDieAlten = false;
		gegenDieSau = false;
		extrapunkte = 0;
		re = 0;
		reAngesagt = 0;
		kontra = 0;
		kontraAngesagt = 0;
		herzGehtRum = false;
		solo = Solo.KEIN_SOLO;
		boecke = boeckeBeiBeginn;
		//ende = null;
		ergebnis = -1;
		fireRundeEvent(RundeEventTyp.DATA_CHANGED);
	}

    public Date getStart() {
        return start;
    }

    public Date getEnde() {
        return ende;
    }

    public int getReVonVorneHerein() {
        return reVonVorneHerein;
    }

    public int getReAngesagt() {
        return reAngesagt;
    }

    public int getKontraVonVorneHerein() {
        return kontraVonVorneHerein;
    }

    public int getKontraAngesagt() {
        return kontraAngesagt;
    }

    public int getBoeckeBeiBeginn() {
        return boeckeBeiBeginn;
    }

    public int getRe() {
        return re;
    }

    public int getKontra() {
        return kontra;
    }

    public boolean isReGewinnt() {
        return reGewinnt;
    }

    public boolean isGegenDieSau() {
        return gegenDieSau;
    }

    public int getExtrapunkte() {
        return extrapunkte;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setEnde(Date ende) {
        this.ende = ende;
    }

    public void setBoeckeBeiBeginn(int boeckeBeiBeginn) {
        this.boeckeBeiBeginn = boeckeBeiBeginn;
    }

    public void setReGewinnt(boolean reGewinnt) {
        this.reGewinnt = reGewinnt;
    }

    public void setHerzGehtRum(boolean herzGehtRum) {
        this.herzGehtRum = herzGehtRum;
    }

    public void setErgebnisString(String ergebnisString) {
        this.ergebnisString = ergebnisString;
    }

    public long get_id() {
        return _id;
    }

    private enum RundeEventTyp {
		ERGBENIS_BERECHNET,
		DATA_CHANGED, 
		BOECKE,
		UNDO_BOECKE,
		BOCK,
		UNDO_BOCK
	}

	private void fireRundeEvent(RundeEventTyp typ) {
		EventObject e = new EventObject(this);
		for (RundeListener l : listener) {
			switch (typ) {
			case ERGBENIS_BERECHNET: l.ergebnisBerechnet(e); break;
			case DATA_CHANGED: l.rundeDataChanged(e); break;			
			case BOECKE: l.boecke(e); break;
			case BOCK: l.bock(e); break;
			case UNDO_BOECKE: l.undoBoecke(e); break;
			case UNDO_BOCK: l.undoBock(e); break;
			}			
		}
	}

	
	
}
