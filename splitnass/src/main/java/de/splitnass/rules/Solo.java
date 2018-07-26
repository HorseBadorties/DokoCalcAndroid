package de.splitnass.rules;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;

public class Solo implements Serializable {
	
	private int id;
    private transient String name;
	private transient boolean regulaeresAufspiel;
	private transient boolean sauMoeglich;
	private transient boolean gegenDieAltenMoeglich;
	
	private Solo(int id, String name, boolean regulaeresAufspiel, boolean sauMoeglich, boolean gegenDieAltenMoeglich) {
        this.id = id;
        this.name = name;
		this.regulaeresAufspiel = regulaeresAufspiel;
		this.sauMoeglich = sauMoeglich;
		this.gegenDieAltenMoeglich = gegenDieAltenMoeglich;
	}
	
	public static Solo KEIN_SOLO = new Solo(0, "<kein Solo>", true, true, true);
	public static Solo FLEISCHLOS = new Solo(1, "Fleischlos", false, false, false);
	public static Solo DAMENSOLO = new Solo(2, "Damensolo", false, false, false);
	public static Solo BAUERNSOLO = new Solo(3, "Bauernsolo", false, false, false);
	public static Solo FARBENSOLO = new Solo(4, "Farbensolo", false, false, false);
	public static Solo TRUMPFSOLO = new Solo(5, "Trumpfsolo", false, true, false);
	public static Solo NULL = new Solo(6, "Null", true, false, false);
	public static Solo STILLES_SOLO = new Solo(7, "Stilles Solo", true, true, true);

	public boolean isRegulaeresAufspiel() {
		return regulaeresAufspiel;
	}	
	
	public boolean isSauMoeglich() {
		return sauMoeglich;
	}
	
	public boolean isGegenDieAltenMoeglich() {
		return gegenDieAltenMoeglich;
	}

    public int getId() {
        return id;
    }

    public static Solo getById(int id) {
        for (Solo s : getAll()) {
            if (s.id == id) return s;
        }
        return null;
    }
	
	public static List<Solo> getAll() {
		List<Solo> result = new ArrayList<Solo>();
		result.add(KEIN_SOLO);
		result.add(FLEISCHLOS);
		result.add(DAMENSOLO);
		result.add(BAUERNSOLO);
//		result.add(FARBENSOLO);
		result.add(TRUMPFSOLO);
		result.add(NULL);
		result.add(STILLES_SOLO);
		return result;
	}

	@Override
	public String toString() {
		return name; 
	}

	public static class JsonSerializer implements JsonDeserializer<Solo> {
		@Override
		public Solo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			return Solo.getById(jObject.get("id").getAsInt());
		}
	}
}
