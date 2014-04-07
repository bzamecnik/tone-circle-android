package com.harmoneye.tonecircle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: since the space of possible values is limited, make a weak cache
// (like in the flyweight pattern)

/**
 * Represents a set of integer pitch classes within a 12-tone octave.
 * 
 * This is an immutable value class.
 */
public class PitchClassSet {
	public static final int OCTAVE_SIZE = 12;

	private static final int MIN_INDEX = 0;
	private static final int MAX_INDEX = (1 << (OCTAVE_SIZE)) - 1;

	/** bit set representing the pitch classes in the set */
	private final int index;

	// cached values

	/**
	 * Canonic set of this one - a set with minimal index containing the same
	 * pitch classes as this one up to transposition.
	 * 
	 * Lazy initialized.
	 */
	private PitchClassSet canonicSet;
	/**
	 * Pitch class that is root (0) in the canonic set, ie. the offset that
	 * transposes the canonic set to this one.
	 * 
	 * Lazy initialized.
	 */
	private Integer rootPitchClass;

	private PitchClassSet(int index) {
		if (index < MIN_INDEX || index > MAX_INDEX) {
			throw new IllegalArgumentException("index out ouf bounds: " + index);
		}
		this.index = index;
	}

	public static PitchClassSet fromIndex(int index) {
		return new PitchClassSet(index);
	}

	public static PitchClassSet empty() {
		return fromIndex(0);
	}

	public static PitchClassSet fromSet(Set<Integer> set) {
		int bitSetIndex = 0;
		for (Integer pitchClass : set) {
			bitSetIndex += 1 << pitchClass;
		}
		return PitchClassSet.fromIndex(bitSetIndex);
	}

	public static PitchClassSet fromArray(int... tones) {
		Set<Integer> set = new HashSet<Integer>();
		for (int tone : tones) {
			set.add(tone);
		}
		return fromSet(set);
	}

	public Set<Integer> asSet() {
		return new HashSet<Integer>(asList());
	}

	public List<Integer> asList() {
		List<Integer> list = new ArrayList<Integer>();
		for (int pitchClass = 0; pitchClass < OCTAVE_SIZE; pitchClass++) {
			if ((index & (1 << pitchClass)) > 0) {
				list.add(pitchClass);
			}
		}
		return list;
	}

	public int getIndex() {
		return index;
	}

	public boolean get(int pitchClass) {
		assertPitchClass(pitchClass);
		return (index & (1 << pitchClass)) != 0;
	}

	public PitchClassSet set(int pitchClass) {
		assertPitchClass(pitchClass);
		return PitchClassSet.fromIndex(index | (1 << pitchClass));
	}

	public PitchClassSet set(int pitchClass, boolean value) {
		return (value) ? set(pitchClass) : clear(pitchClass);
	}

	public PitchClassSet clear(int pitchClass) {
		assertPitchClass(pitchClass);
		return PitchClassSet.fromIndex(index & ~(1 << pitchClass));
	}

	public PitchClassSet flip(int pitchClass) {
		assertPitchClass(pitchClass);
		return PitchClassSet.fromIndex(index ^ (1 << pitchClass));
	}

	public int cardinality() {
		return Integer.bitCount(index);
	}

	public PitchClassSet transpose(int offset) {
		offset = Modulo.modulo(offset, OCTAVE_SIZE);
		int upper = index << offset;
		int lower = index >> (OCTAVE_SIZE - offset);
		int mask = (1 << OCTAVE_SIZE) - 1;
		return PitchClassSet.fromIndex((upper | lower) & mask);
	}

	public PitchClassSet getCanonic() {
		if (canonicSet == null) {
			findCanonicSet();
		}
		return canonicSet;
	}

	public int getRoot() {
		if (rootPitchClass == null) {
			findCanonicSet();
		}
		return rootPitchClass;
	}

	private void findCanonicSet() {
		int minIndex = Integer.MAX_VALUE;
		int transposition = 0;
		for (int i = 0; i < OCTAVE_SIZE; i++) {
			PitchClassSet transposed = transpose(i);
			if (transposed.getIndex() < minIndex) {
				minIndex = transposed.getIndex();
				transposition = i;
			}
		}
		int root = Modulo.modulo(OCTAVE_SIZE - transposition, OCTAVE_SIZE);
		boolean isCanonic = root == 0;
		this.rootPitchClass = root;
		this.canonicSet = isCanonic ? this : PitchClassSet.fromIndex(minIndex);
	}

	private void assertPitchClass(int pitchClass) {
		if (pitchClass < 0 || pitchClass >= OCTAVE_SIZE) {
			throw new ArrayIndexOutOfBoundsException(pitchClass);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PitchClassSet other = (PitchClassSet) obj;
		if (index != other.index)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("index: ").append(getIndex());
		sb.append(", values: ").append(asList());
		sb.append(", root: ").append(getRoot());
		sb.append(", canonic: ").append(getCanonic().asList());
		return sb.toString();
	}
}
