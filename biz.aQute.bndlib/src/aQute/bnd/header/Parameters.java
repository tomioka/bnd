package aQute.bnd.header;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;

import aQute.lib.collections.SortedList;
import aQute.service.reporter.Reporter;

public class Parameters implements Map<String,Attrs> {
	private final Map<String, Attrs>	map;
	private final boolean				allowDuplicateAttributes;

	public Parameters(boolean allowDuplicateAttributes) {
		this.allowDuplicateAttributes = allowDuplicateAttributes;
		map = new LinkedHashMap<>();
	}

	public Parameters() {
		this(false);
	}

	public Parameters(String header) {
		this(header, null);
	}

	public Parameters(String header, Reporter reporter) {
		this(false);
		OSGiHeader.parseHeader(header, reporter, this);
	}

	public void clear() {
		map.clear();
	}

	public void add(String key, Attrs attrs) {
		while (containsKey(key))
			key += "~";
		put(key, attrs);
	}

	public boolean containsKey(String name) {
		return map.containsKey(name);
	}

	@SuppressWarnings("cast")
	@Deprecated
	public boolean containsKey(Object name) {
		assert name instanceof String;
		return map.containsKey(name);
	}

	public boolean containsValue(Attrs value) {
		return map.containsValue(value);
	}

	@SuppressWarnings("cast")
	@Deprecated
	public boolean containsValue(Object value) {
		assert value instanceof Attrs;
		return map.containsValue(value);
	}

	public Set<java.util.Map.Entry<String,Attrs>> entrySet() {
		return map.entrySet();
	}

	@SuppressWarnings("cast")
	@Deprecated
	public Attrs get(Object key) {
		assert key instanceof String;
		return map.get(key);
	}

	public Attrs get(String key) {
		return map.get(key);
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<String> keySet() {
		return map.keySet();
	}

	public Attrs put(String key, Attrs value) {
		assert key != null;
		assert value != null;
		return map.put(key, value);
	}

	public void putAll(Map< ? extends String, ? extends Attrs> map) {
		this.map.putAll(map);
	}

	public void putAllIfAbsent(Map<String, ? extends Attrs> map) {
		for (Map.Entry<String, ? extends Attrs> entry : map.entrySet()) {
			if (!containsKey(entry.getKey()))
				put(entry.getKey(), entry.getValue());
		}
	}

	@SuppressWarnings("cast")
	@Deprecated
	public Attrs remove(Object var0) {
		assert var0 instanceof String;
		return map.remove(var0);
	}

	public Attrs remove(String var0) {
		return map.remove(var0);
	}

	public int size() {
		return map.size();
	}

	public Collection<Attrs> values() {
		return map.values();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		append(sb);
		return sb.toString();
	}

	public void append(StringBuilder sb) {
		String del = "";
		for (Map.Entry<String,Attrs> s : entrySet()) {
			sb.append(del);
			sb.append(removeDuplicateMarker(s.getKey()));
			if (!s.getValue().isEmpty()) {
				sb.append(';');
				s.getValue().append(sb);
			}

			del = ",";
		}
	}

	private Object removeDuplicateMarker(String key) {
		while (key.endsWith("~"))
			key = key.substring(0, key.length() - 1);
		return key;
	}

	@Override
	@Deprecated
	public boolean equals(Object other) {
		return super.equals(other);
	}

	@Override
	@Deprecated
	public int hashCode() {
		return super.hashCode();
	}

	public boolean isEqual(Parameters other) {
		if (this == other)
			return true;

		if (other == null || size() != other.size())
			return false;

		if (isEmpty())
			return true;

		SortedList<String> l = new SortedList<String>(keySet());
		SortedList<String> lo = new SortedList<String>(other.keySet());
		if (!l.isEqual(lo))
			return false;

		for (String key : keySet()) {
			Attrs value = get(key);
			Attrs valueo = other.get(key);
			if (!(value == valueo || (value != null && value.isEqual(valueo))))
				return false;
		}
		return true;
	}

	public Map<String, ? extends Map<String,String>> asMapMap() {
		return this;
	}

	/**
	 * Merge all attributes of the given parameters with this
	 */
	public void mergeWith(Parameters other, boolean override) {
		for (Map.Entry<String,Attrs> e : other.entrySet()) {
			Attrs existing = get(e.getKey());
			if (existing == null) {
				put(e.getKey(), new Attrs(e.getValue()));
			} else
				existing.mergeWith(e.getValue(), override);
		}
	}

	public boolean allowDuplicateAttributes() {
		return allowDuplicateAttributes;
	}

	public static Collector<String, Parameters, Parameters> toParameters() {
		return Collector.of(Parameters::new, Parameters::accumulator, Parameters::combiner);
	}
	private static void accumulator(Parameters p, String s) {
		OSGiHeader.parseHeader(s, null, p);
	}
	private static Parameters combiner(Parameters t, Parameters u) {
		t.mergeWith(u, true);
		return t;
	}
}
