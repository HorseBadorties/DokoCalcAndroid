package de.splitnass.data;

public interface SpieltagListener {
	public void rundeUpdated(Runde r);
	public void spieltagUpdated();
    public void aktuelleRundeChanged();
	public void spielerSteigtEin();
}
