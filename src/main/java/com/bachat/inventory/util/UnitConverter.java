package com.bachat.inventory.util;

import com.bachat.inventory.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Converts quantities between weight units used in the system.
 *
 * Canonical units stored in inventory are whatever the product's {@code unit} field says.
 * Supported units (case-insensitive): g, gram, grams, kg, kilogram, kilograms, maund, maunds, mand
 *
 * Conversion reference (Pakistan standard):
 *   1 maund = 40 kg
 *   1 kg    = 1000 g
 *   1 maund = 40,000 g
 */
public class UnitConverter {

    // All values expressed in grams
    private static final BigDecimal GRAMS_PER_KG    = new BigDecimal("1000");
    private static final BigDecimal GRAMS_PER_MAUND = new BigDecimal("40000"); // 40 kg

    private UnitConverter() {}

    /**
     * Converts {@code quantity} from {@code fromUnit} to {@code toUnit}.
     *
     * @param quantity  the quantity to convert (must be > 0)
     * @param fromUnit  source unit string (e.g. "maund", "kg", "g")
     * @param toUnit    target unit string (e.g. "kg")
     * @return converted quantity, scaled to 4 decimal places
     */
    public static BigDecimal convert(BigDecimal quantity, String fromUnit, String toUnit) {
        if (fromUnit == null || toUnit == null) {
            throw new BadRequestException("Unit cannot be null for conversion");
        }

        String from = normalise(fromUnit);
        String to   = normalise(toUnit);

        if (from.equals(to)) {
            return quantity;
        }

        // Convert fromUnit → grams → toUnit
        BigDecimal inGrams = toGrams(quantity, from);
        return fromGrams(inGrams, to);
    }

    /**
     * Convenience: converts only when the units differ.
     * If {@code incomingUnit} is null or blank it is treated as identical to {@code productUnit}
     * (backward-compatible: callers that don't send a unit get no conversion).
     */
    public static BigDecimal toProductUnit(BigDecimal quantity, String incomingUnit, String productUnit) {
        if (incomingUnit == null || incomingUnit.isBlank()) {
            return quantity; // no unit sent → assume already in product's unit
        }
        if (normalise(incomingUnit).equals(normalise(productUnit))) {
            return quantity;
        }
        return convert(quantity, incomingUnit, productUnit);
    }

    /**
     * Returns true if the given unit string is a recognised weight unit.
     */
    public static boolean isKnownUnit(String unit) {
        if (unit == null || unit.isBlank()) return false;
        try {
            normalise(unit);
            return true;
        } catch (BadRequestException e) {
            return false;
        }
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private static BigDecimal toGrams(BigDecimal qty, String normalisedUnit) {
        return switch (normalisedUnit) {
            case "g"     -> qty;
            case "kg"    -> qty.multiply(GRAMS_PER_KG);
            case "maund" -> qty.multiply(GRAMS_PER_MAUND);
            default      -> throw new BadRequestException("Unknown unit: " + normalisedUnit);
        };
    }

    private static BigDecimal fromGrams(BigDecimal grams, String normalisedUnit) {
        return switch (normalisedUnit) {
            case "g"     -> grams.setScale(4, RoundingMode.HALF_UP);
            case "kg"    -> grams.divide(GRAMS_PER_KG, 4, RoundingMode.HALF_UP);
            case "maund" -> grams.divide(GRAMS_PER_MAUND, 4, RoundingMode.HALF_UP);
            default      -> throw new BadRequestException("Unknown unit: " + normalisedUnit);
        };
    }

    /** Normalises to one of: "g", "kg", "maund" */
    private static String normalise(String unit) {
        return switch (unit.trim().toLowerCase()) {
            case "g", "gram", "grams"                   -> "g";
            case "kg", "kilogram", "kilograms"          -> "kg";
            case "maund", "maunds", "mand", "mann"      -> "maund";
            default -> throw new BadRequestException(
                    "Unrecognised unit '" + unit + "'. Supported: g, kg, maund");
        };
    }
}
