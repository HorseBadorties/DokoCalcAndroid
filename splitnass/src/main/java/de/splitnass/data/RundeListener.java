package de.splitnass.data;

import java.util.EventObject;

public interface RundeListener {
	public void ergebnisBerechnet(EventObject e);
	public void rundeDataChanged(EventObject e);
	public void boecke(EventObject e); //z.B. re-Kontra
	public void undoBoecke(EventObject e); 
	public void bock(EventObject e); //einfacher Bock bei drei bereits vorhandenen -> weiterleiten
	public void undoBock(EventObject e);
}
