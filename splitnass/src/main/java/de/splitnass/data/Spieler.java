package de.splitnass.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;

public class Spieler implements Serializable {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Spieler spieler = (Spieler) o;

        if (name != null ? !name.equals(spieler.name) : spieler.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private transient String name;
	private transient boolean isAktiv;

	@Override
	public String toString() {
		return name;
	}

	
	public Spieler(int id, String name) {
        this.id = id;
		this.name = name;
		this.isAktiv = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setIsAktiv(boolean value) {
		isAktiv = value;
	}
	
	public boolean isAktiv() {
		return isAktiv;
	}
		
	private static List<Spieler> all = null;
	public static List<Spieler> getAll() {
		if (all == null) {
			all = new ArrayList<Spieler>();
			all.add(new Spieler(1, "Claus"));
			all.add(new Spieler(2, "Guido"));
			all.add(new Spieler(3, "Levent"));
			all.add(new Spieler(4, "Ralf"));
            all.add(new Spieler(5, "Torsten"));
            all.add(new Spieler(7, "Ernesto"));
            all.add(new Spieler(8, "Achim"));
            all.add(new Spieler(9, "Thomas"));

		}
		return all;		
	}

    public static Spieler byName(String name) {
        for (Spieler s : getAll()) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public static Spieler byId(int id) {
        for (Spieler s : getAll()) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    public static class JsonSerializer implements JsonDeserializer<Spieler> {
        @Override
        public Spieler deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return Spieler.byId(jObject.get("id").getAsInt());
        }
    }
}
