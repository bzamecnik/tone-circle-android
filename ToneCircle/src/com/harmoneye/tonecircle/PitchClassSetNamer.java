package com.harmoneye.tonecircle;

import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PitchClassSetNamer {

	private Map<Integer, Map<String, Object>> names;

	private static final String[] TONE_NAMES = { "C", "Db", "D", "Eb", "E",
		"F", "Gb", "G", "Ab", "A", "Bb", "B" };

	private PitchClassSetNamer(Map<Integer, Map<String, Object>> names) {
		this.names = names;
	}

	@SuppressWarnings("unchecked")
	public static PitchClassSetNamer fromJson(InputStream inputStream) {
		try {
			JsonFactory f = new JsonFactory();
			f.enable(JsonParser.Feature.ALLOW_COMMENTS);
			ObjectMapper objectMapper = new ObjectMapper(f);
			Object n = objectMapper.readValue(inputStream,
				new TypeReference<Map<Integer, Map<String, Object>>>() {
				});
			return new PitchClassSetNamer((Map<Integer, Map<String, Object>>) n);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
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
		int root = Modulo.modulo(templateRoot + tones.getRoot(),
			PitchClassSet.OCTAVE_SIZE);
		String rootName = TONE_NAMES[root];
		String chordName = (String) details.get("chord");
		if (chordName == null) {
			chordName = "";
		}
		chordName = chordName.replace("X", rootName);
		return chordName;
	}
}