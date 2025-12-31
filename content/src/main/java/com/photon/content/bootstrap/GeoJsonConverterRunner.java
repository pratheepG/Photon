//package com.photon.content.bootstrap;
//
//import com.fasterxml.jackson.core.*;
//import com.fasterxml.jackson.databind.*;
//import com.fasterxml.jackson.databind.node.*;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.io.*;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.nio.file.*;
//import java.util.*;
//
///**
// * Spring Boot CommandLineRunner that converts a GeoJSON file into the requested structure.
// *
// * Configure in application.properties:
// * app.geo.input=/full/path/All_India_pincode_Boundary-19312.geojson
// * app.geo.output=/full/path/converted_pincodes.json
// * app.geo.reducePrecision=false
// * app.geo.precision=3
// *
// * Run from IDE. If dataset is large, increase JVM heap (e.g. -Xmx2g).
// */
//@Component
//public class GeoJsonConverterRunner implements CommandLineRunner {
//
//    @Value("${app.geo.input:All_India_pincode_Boundary-19312.geojson}")
//    private String inputPath;
//
//    @Value("${app.geo.output:converted_pincodes.json}")
//    private String outputPath;
//
//    @Value("${app.geo.reducePrecision:false}")
//    private boolean reducePrecision;
//
//    @Value("${app.geo.precision:3}")
//    private int precision;
//
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    private static final String[] removeSuffixes = { " S.O", " H.O", " B.O", " SO", " HO", " BO", " MDG" };
//
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("GeoJsonConverterRunner starting...");
//        System.out.println("Input: " + inputPath);
//        System.out.println("Output: " + outputPath);
//        System.out.println("Reduce precision: " + reducePrecision + " (decimals: " + precision + ")");
//
//        Path in = Paths.get(inputPath);
//        if (!Files.exists(in)) {
//            System.err.println("Input file not found: " + in.toAbsolutePath());
//            return;
//        }
//
//        // Map< Circle, Map< Division, List<ObjectNode(city)> > >
//        LinkedHashMap<String, LinkedHashMap<String, ArrayNode>> recordsMap = new LinkedHashMap<>();
//
//        // We'll use streaming to scan features array and process features one-by-one
//        try (InputStream is = Files.newInputStream(in);
//             JsonParser jp = mapper.getFactory().createParser(is)) {
//
//            // move to start
//            if (jp.nextToken() == null) {
//                throw new IOException("Empty input file");
//            }
//
//            // find "features" field
//            while (jp.currentToken() != null) {
//                JsonToken t = jp.currentToken();
//                if (t == JsonToken.FIELD_NAME && "features".equals(jp.getCurrentName())) {
//                    jp.nextToken(); // move into features array
//                    if (jp.currentToken() != JsonToken.START_ARRAY) {
//                        throw new IOException("Expected features to be an array");
//                    }
//
//                    // iterate features
//                    while (jp.nextToken() != JsonToken.END_ARRAY) {
//                        // read single feature as tree (small, per-feature)
//                        JsonNode featureNode = mapper.readTree(jp);
//                        processFeature(featureNode, recordsMap);
//                    }
//                    break; // done processing features
//                } else {
//                    jp.nextToken(); // advance
//                }
//            }
//        }
//
//        // Build output root node
//        ObjectNode outRoot = mapper.createObjectNode();
//        ArrayNode recordsArray = mapper.createArrayNode();
//
//        for (Map.Entry<String, LinkedHashMap<String, ArrayNode>> circleEntry : recordsMap.entrySet()) {
//            String circleName = circleEntry.getKey();
//            LinkedHashMap<String, ArrayNode> divisions = circleEntry.getValue();
//
//            ObjectNode recordNode = mapper.createObjectNode();
//            recordNode.put("name", circleName.trim());
//            recordNode.put("code", ""); // keep empty
//
//            ArrayNode districtsArray = mapper.createArrayNode();
//            for (Map.Entry<String, ArrayNode> divEntry : divisions.entrySet()) {
//                ObjectNode districtNode = mapper.createObjectNode();
//                districtNode.put("name", divEntry.getKey().trim());
//                districtNode.set("cities", divEntry.getValue());
//                districtsArray.add(districtNode);
//            }
//
//            recordNode.set("districts", districtsArray);
//            recordsArray.add(recordNode);
//        }
//
//        outRoot.set("records", recordsArray);
//
//        // Optionally reduce geometry coordinate precision
//        if (reducePrecision) {
//            truncateCoordinates(outRoot, precision);
//        }
//
//        // Write pretty output (stream to file)
//        Path out = Paths.get(outputPath);
//        try (OutputStream os = Files.newOutputStream(out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//             Writer w = new OutputStreamWriter(os, "UTF-8")) {
//            mapper.writerWithDefaultPrettyPrinter().writeValue(w, outRoot);
//        }
//
//        // Simple stats
//        int featureCount = recordsMap.values().stream()
//                .flatMap(m -> m.values().stream())
//                .mapToInt(ArrayNode::size).sum();
//        System.out.println("Conversion complete. Cities processed: " + featureCount);
//        System.out.println("Saved to: " + out.toAbsolutePath());
//    }
//
//    private void processFeature(JsonNode featureNode,
//                                LinkedHashMap<String, LinkedHashMap<String, ArrayNode>> recordsMap) {
//        if (featureNode == null || featureNode.isMissingNode()) return;
//        JsonNode props = featureNode.path("properties");
//       // JsonNode geom = featureNode.path("geometry");
//
//        String circle = firstNonEmptyText(props, "Circle", "circle", "CIRCLE", "CircleName");
//        if (circle == null || circle.isEmpty()) circle = "Unknown Circle";
//
//        String division = firstNonEmptyText(props, "Division", "division", "DIVISION", "District");
//        if (division == null || division.isEmpty()) division = "Unknown Division";
//
//        String office = firstNonEmptyText(props, "Office_Name", "OfficeName", "office_name", "Name", "name");
//        if (office == null || office.isEmpty()) office = "Unknown Office";
//
//        for (String suffix : removeSuffixes) {
//            if (office.toUpperCase().endsWith(suffix)) {
//                office = office.substring(0, office.length() - suffix.length()).trim();
//                break;
//            }
//        }
//
//        String pincode = firstNonEmptyText(props, "Pincode", "PINCODE", "PIN", "pincode");
//        if (pincode == null) pincode = "";
//
//        // init structures
//        recordsMap.computeIfAbsent(circle, k -> new LinkedHashMap<>());
//        LinkedHashMap<String, ArrayNode> divMap = recordsMap.get(circle);
//        divMap.computeIfAbsent(division, k -> mapper.createArrayNode());
//
//        // create city node
//        ObjectNode cityNode = mapper.createObjectNode();
//        cityNode.put("name", office.trim());
//        cityNode.put("pinCode", pincode.trim());
//
//        // copy geometry as-is (shallow copy)
////        if (geom != null && !geom.isMissingNode() && !geom.isNull()) {
////            cityNode.set("geometry", geom);
////        } else {
////            ObjectNode empty = mapper.createObjectNode();
////            empty.put("type", "GeometryCollection");
////            empty.set("geometries", mapper.createArrayNode());
////            cityNode.set("geometry", empty);
////        }
//
//        divMap.get(division).add(cityNode);
//    }
//
//    private String firstNonEmptyText(JsonNode node, String... keys) {
//        if (node == null || node.isMissingNode()) return null;
//        for (String k : keys) {
//            JsonNode v = node.get(k);
//            if (v != null && !v.isNull()) {
//                String s = v.asText("");
//                if (!s.isEmpty()) return s;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Recursively traverses the output JSON and truncates numeric coordinates in arrays to the given decimals.
//     * This only affects numeric leaf values inside arrays under "coordinates".
//     */
//    private void truncateCoordinates(JsonNode root, int decimals) {
//        if (root == null) return;
//        BigDecimal scale = BigDecimal.ONE;
//        // nothing needed from scale other than rounding usage
//        truncateNodeRecursive(root, decimals);
//    }
//
//    private void truncateNodeRecursive(JsonNode node, int decimals) {
//        if (node == null) return;
//        if (node.isObject()) {
//            ObjectNode obj = (ObjectNode) node;
//            Iterator<Map.Entry<String, JsonNode>> it = obj.fields();
//            List<String> fieldNames = new ArrayList<>();
//            it.forEachRemaining(e -> fieldNames.add(e.getKey()));
//            for (String fname : fieldNames) {
//                JsonNode child = obj.get(fname);
//                if ("coordinates".equals(fname) && (child.isArray())) {
//                    // modify coordinates array inplace
//                    JsonNode replaced = roundCoordinatesArray(child, decimals);
//                    obj.set(fname, replaced);
//                } else {
//                    truncateNodeRecursive(child, decimals);
//                }
//            }
//        } else if (node.isArray()) {
//            for (JsonNode el : node) {
//                truncateNodeRecursive(el, decimals);
//            }
//        }
//    }
//
//    /**
//     * Rounds all numeric values found inside the coordinates array/sub-arrays to 'decimals' places.
//     * Returns a new array node with rounded numbers (keeps structure).
//     */
//    private JsonNode roundCoordinatesArray(JsonNode arrNode, int decimals) {
//        if (!arrNode.isArray()) return arrNode;
//        ArrayNode out = mapper.createArrayNode();
//        for (JsonNode el : arrNode) {
//            if (el.isArray()) {
//                out.add(roundCoordinatesArray(el, decimals));
//            } else if (el.isNumber()) {
//                // round
//                double d = el.asDouble();
//                double rounded = roundDouble(d, decimals);
//                out.add(rounded);
//            } else {
//                // keep as-is (strings/objects)
//                out.add(el);
//            }
//        }
//        return out;
//    }
//
//    private double roundDouble(double value, int decimals) {
//        BigDecimal bd = BigDecimal.valueOf(value);
//        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
//        return bd.doubleValue();
//    }
//}
