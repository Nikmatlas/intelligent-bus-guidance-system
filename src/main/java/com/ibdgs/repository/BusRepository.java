package com.ibdgs.repository;

import com.ibdgs.model.Bus;
import com.ibdgs.service.BusValidator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores buses in a human-readable JSON file and enforces:
 *   B1 - busID format (via {@link BusValidator}) and uniqueness.
 *   b2 - capacity cannot increase on update (may decrease or stay equal).
 */
public class BusRepository {

    private final Path storeFile;
    private final Map<String, Bus> buses = new LinkedHashMap<>();

    /** Uses the default store file {@code data/buses.json}. */
    public BusRepository() {
        this(Paths.get("data", "buses.json"));
    }

    public BusRepository(Path storeFile) {
        this.storeFile = storeFile;
        load();
    }

    // ---------------- Operations ----------------

    /** Adds a new bus after validating B1 (format + uniqueness). */
    public void add(Bus bus) {
        BusValidator.validateFields(bus); // B1 format
        if (buses.containsKey(bus.getBusID())) {
            throw new IllegalArgumentException(
                    "B1 violation: duplicate busID '" + bus.getBusID() + "'");
        }
        buses.put(bus.getBusID(), copyOf(bus));
        save();
    }

    /** Updates an existing bus. Enforces B2 (capacity must not increase). */
    public void update(Bus updated) {
        if (updated == null || updated.getBusID() == null) {
            throw new IllegalArgumentException("Bus (with busID) is required for update");
        }
        Bus existing = buses.get(updated.getBusID());
        if (existing == null) {
            throw new IllegalArgumentException(
                    "Cannot update: no bus with ID '" + updated.getBusID() + "'");
        }
        if (!BusValidator.isValidCapacityUpdate(existing.getCapacity(), updated.getCapacity())) {
            throw new IllegalArgumentException(
                    "B2 violation: capacity must be positive and cannot increase (was "
                            + existing.getCapacity() + ", attempted " + updated.getCapacity() + ")");
        }
        if (!BusValidator.isValidFuelType(updated.getFuelType())) {
            throw new IllegalArgumentException("Invalid fuelType '" + updated.getFuelType() + "'");
        }
        if (updated.getFuelLevel() < 0) {
            throw new IllegalArgumentException("fuelLevel must not be negative");
        }
        buses.put(updated.getBusID(), copyOf(updated));
        save();
    }

    /** Retrieves a bus by ID, or an empty Optional if none exists. */
    public Optional<Bus> retrieve(String busID) {
        Bus b = buses.get(busID);
        return b == null ? Optional.empty() : Optional.of(copyOf(b));
    }

    /** Returns the number of stored buses. */
    public int count() {
        return buses.size();
    }

    public List<Bus> findAll() {
        List<Bus> all = new ArrayList<>();
        for (Bus b : buses.values()) {
            all.add(copyOf(b));
        }
        return all;
    }

    /**
     * Returns a defensive copy of a Bus using its constructor, so the repository
     * stores its own snapshot instead of the caller's mutable object. This avoids
     * depending on a copy() method existing on the shared Bus model.
     */
    private static Bus copyOf(Bus b) {
        return new Bus(b.getBusID(), b.getCapacity(), b.getFuelLevel(), b.getFuelType());
    }

    // ---------------- Persistence (inline, human-readable JSON) ----------------

    private void save() {
        StringBuilder sb = new StringBuilder("[\n");
        int i = 0;
        for (Bus b : buses.values()) {
            sb.append("  {\n");
            sb.append("    \"busID\": \"").append(escape(b.getBusID())).append("\",\n");
            sb.append("    \"capacity\": ").append(b.getCapacity()).append(",\n");
            sb.append("    \"fuelLevel\": ").append(b.getFuelLevel()).append(",\n");
            sb.append("    \"fuelType\": \"").append(escape(b.getFuelType())).append("\"\n");
            sb.append("  }").append(i < buses.size() - 1 ? ",\n" : "\n");
            i++;
        }
        sb.append("]\n");
        try {
            if (storeFile.getParent() != null) {
                Files.createDirectories(storeFile.getParent());
            }
            Files.write(storeFile, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write store file: " + storeFile, ex);
        }
    }

    private void load() {
        buses.clear();
        try {
            if (!Files.exists(storeFile)) {
                return;
            }
            String text = new String(Files.readAllBytes(storeFile), StandardCharsets.UTF_8);
            for (Map<String, String> rec : parseObjects(text)) {
                Bus b = new Bus(
                        rec.get("busID"),
                        parseInt(rec.get("capacity")),
                        parseDouble(rec.get("fuelLevel")),
                        rec.get("fuelType"));
                buses.put(b.getBusID(), b);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read store file: " + storeFile, ex);
        }
    }

    // --- tiny JSON helpers (array of flat objects; values may be quoted or bare) ---

    private static List<Map<String, String>> parseObjects(String s) {
        List<Map<String, String>> out = new ArrayList<>();
        int i = 0, n = s.length();
        while (i < n) {
            if (s.charAt(i) == '{') {
                Map<String, String> obj = new LinkedHashMap<>();
                i++;
                while (i < n && s.charAt(i) != '}') {
                    while (i < n && s.charAt(i) != '"') i++; // start of key
                    if (i >= n) break;
                    int[] k = readString(s, i);
                    String key = s.substring(i + 1, k[0]);
                    i = k[1];
                    while (i < n && s.charAt(i) != ':') i++;
                    i++; // skip ':'
                    while (i < n && Character.isWhitespace(s.charAt(i))) i++;
                    String value;
                    if (i < n && s.charAt(i) == '"') {
                        int[] v = readString(s, i);
                        value = unescape(s.substring(i + 1, v[0]));
                        i = v[1];
                    } else { // bare number/token until , or }
                        int start = i;
                        while (i < n && s.charAt(i) != ',' && s.charAt(i) != '}') i++;
                        value = s.substring(start, i).trim();
                    }
                    obj.put(key, value);
                    while (i < n && s.charAt(i) != ',' && s.charAt(i) != '}') i++;
                    if (i < n && s.charAt(i) == ',') i++;
                }
                out.add(obj);
            }
            i++;
        }
        return out;
    }

    /** Returns {endQuoteIndex, indexAfterClosingQuote} for a string starting at an opening quote. */
    private static int[] readString(String s, int openQuote) {
        int i = openQuote + 1;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\') { i += 2; continue; }
            if (c == '"') break;
            i++;
        }
        return new int[]{i, i + 1};
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0.0; }
    }
}