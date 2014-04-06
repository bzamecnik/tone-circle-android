package com.harmoneye.tonecircle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PitchClassSet {
	private static final int OCTAVE_SIZE = 12;

	private int index;

	// cached values
	private PitchClassSet canonic;
	private Integer root;

	public PitchClassSet() {
		this(0);
	}

	public PitchClassSet(int bitSetIndex) {
		this.index = bitSetIndex;
	}

	public static PitchClassSet fromSet(Set<Integer> set) {
		int bitSetIndex = 0;
		for (Integer e : set) {
			bitSetIndex += 1 << e;
		}
		return new PitchClassSet(bitSetIndex);
	}

	public int getIndex() {
		return index;
	}

	public boolean get(int i) {
		return (index & (1 << i)) != 0;
	}

	public PitchClassSet set(int i) {
		return new PitchClassSet(index | (1 << i));
	}

	public PitchClassSet set(int i, boolean value) {
		return (value) ? set(i) : clear(i);
	}

	public PitchClassSet clear(int i) {
		return new PitchClassSet(index & ~(1 << i));
	}

	public PitchClassSet flip(int i) {
		return new PitchClassSet(index ^ (1 << i));
	}

	public int cardinality() {
		return Integer.bitCount(index);
	}

	public Set<Integer> asSet() {
		Set<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < OCTAVE_SIZE; i++) {
			if ((index & (1 << i)) > 0) {
				set.add(i);
			}
		}
		return set;
	}

	public List<Integer> asList() {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < OCTAVE_SIZE; i++) {
			if ((index & (1 << i)) > 0) {
				list.add(i);
			}
		}
		return list;
	}

	public PitchClassSet transpose(int offset) {
		offset = Modulo.modulo(offset, OCTAVE_SIZE);
		int upper = index << offset;
		int lower = index >> (OCTAVE_SIZE - offset);
		int mask = (1 << OCTAVE_SIZE) - 1;
		return new PitchClassSet((upper | lower) & mask);
	}

	private PitchClassSet getCanonic() {
		if (canonic == null) {

			int minIndex = Integer.MAX_VALUE;
			for (int i = 0; i < OCTAVE_SIZE; i++) {
				PitchClassSet pcs = transpose(i);
				minIndex = Math.min(minIndex, pcs.getIndex());
			}

			canonic = new PitchClassSet(minIndex);
		}
		return canonic;
	}

	public int getRoot() {
		if (root == null) {
			root = 0;
			PitchClassSet canonic = getCanonic();
			for (int i = 0; i < OCTAVE_SIZE; i++) {
				if (canonic.transpose(i).getIndex() == getIndex()) {
					root = i;
					break;
				}
			}

		}
		return root;
	}
}
