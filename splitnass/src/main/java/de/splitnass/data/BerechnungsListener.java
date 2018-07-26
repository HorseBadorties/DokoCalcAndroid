package de.splitnass.data;

import java.util.EventListener;

public interface BerechnungsListener extends EventListener {

    public void handle(BerechnungsEvent event);
}
