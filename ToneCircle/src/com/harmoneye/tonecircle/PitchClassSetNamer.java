package com.harmoneye.tonecircle;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PitchClassSetNamer {

	private Map<Integer, Map<String, Object>> names = new HashMap<Integer, Map<String, Object>>();

	private static final String[] TONE_NAMES = { "C", "Db", "D", "Eb", "E",
		"F", "Gb", "G", "Ab", "A", "Bb", "B" };
	
	private PitchClassSetNamer() {

	}

	public static PitchClassSetNamer fromJson(InputStream inputStream) {
		PitchClassSetNamer namer = new PitchClassSetNamer();
		try {
			JsonFactory f = new JsonFactory();
			f.enable(JsonParser.Feature.ALLOW_COMMENTS);
			ObjectMapper objectMapper = new ObjectMapper(f);
			namer.names = objectMapper.readValue(inputStream,
				new TypeReference<Map<Integer, Map<String, Object>>>() {
				});
		} catch (Exception e) {
			Log.e("Namer", e.toString());
		}
		return namer;
	}

	public String getName(PitchClassSet tones) {
		Map<String, Object> details = names.get(tones.getCanonic().getIndex());
		if (details == null) {
			return "";
		}
		Integer templateRoot = (Integer) details.get("root");
		if (templateRoot == null) {
			templateRoot = 0;
		}
		int root = Modulo.modulo(templateRoot + tones.getRoot(), PitchClassSet.OCTAVE_SIZE);
		String rootName = TONE_NAMES[root];
		String chordName = (String) details.get("chord");
		if (chordName == null) {
			chordName = "";
		}
		chordName = chordName.replace("X", rootName);
		return chordName;
	}
}