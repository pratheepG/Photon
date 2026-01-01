package com.photon.console.sdk;

import com.photon.console.entity.ActionInfo;
import com.photon.console.entity.EndpointDetails;
import com.photon.console.entity.FeatureInfo;
import com.photon.console.entity.Model;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Clean SDK generator that accepts JPA entities (EndpointDetails) directly.
 */
@Slf4j
@Service
public class SdkGenerator {

    private final ObjectMapper json = new ObjectMapper();
    private final MustacheFactory mf = new DefaultMustacheFactory();

    // dev-time templates root (override via constructor if you like)
    private final Path templateRoot = Paths.get("src/main/resources/templates");
    private final Path overridesRoot = Paths.get("src/main/resources/sdk-overrides");

    public SdkGenerator() throws IOException {
        Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir"), "custom-sdk-gen"));
    }

    /**
     * Generate SDKs for a list of modules (EndpointDetails). Returns map: moduleId -> (lang -> zipPath)
     */
    public Map<String, Map<String, Path>> generateModuleSdksFromEntities(List<EndpointDetails> modules, List<String> languages, Path outBase) throws Exception {
        if (modules == null) throw new IllegalArgumentException("modules is required");
        if (languages == null || languages.isEmpty()) throw new IllegalArgumentException("languages is required");

        Path workBase = outBase.toAbsolutePath();
        Files.createDirectories(workBase);

        Map<String, Map<String, Path>> result = new LinkedHashMap<>();
        for (EndpointDetails module : modules) {
            if (module == null) continue;
            String moduleId = module.getId() == null ? ("module_" + UUID.randomUUID()) : module.getId();
            log.info("Processing module: {}", moduleId);

            ModuleAst ast = buildAstFromEntity(module);

            Map<String, Path> perLang = new LinkedHashMap<>();
            for (String lang : languages) {
                ensureTemplatesExist(lang);

                Path outDir = workBase.resolve(moduleId + "-" + lang);
                if (Files.exists(outDir)) deleteRecursively(outDir);
                Files.createDirectories(outDir);

                renderLanguageFromAst(ast, lang, outDir);

                // optional overrides (dev)
                copyIfExists(overridesRoot.resolve(lang).resolve("common"), outDir);
                copyIfExists(overridesRoot.resolve(lang).resolve(moduleId.toLowerCase()), outDir);

                Path zip = workBase.resolve(moduleId + "-" + lang + ".zip");
                if (Files.exists(zip)) Files.delete(zip);
                zipDirectory(outDir, zip);

                perLang.put(lang, zip);
                log.info("  generated {} -> {}", lang, zip);
            }
            result.put(moduleId, perLang);
        }
        return result;
    }

    /* ---------- AST build ---------- */
    private ModuleAst buildAstFromEntity(EndpointDetails e) {
        ModuleAst ast = new ModuleAst();
        ast.moduleId = e.getId();
        ast.moduleName = e.getName();
        ast.clientId = e.getClientId();
        ast.clientSecret = e.getClientSecret();

        // 1) models
        if (e.getModels() != null) {
            for (Model m : e.getModels()) {
                log.debug("Model {} fields raw: {}", m.getModelId(), m.getFields());
                String mid = m.getModelId() != null ? m.getModelId() : m.getId();
                String simple = simpleName(mid);
                simple = dropDtoSuffix(simple);
                String className = sanitizeForIdentifier(capitalize(simple));
                ModelDef md = new ModelDef(mid, className);
                // fields: each field stored as Map<String,String> in your entity converter
                if (m.getFields() != null) {
                    List<Map<String, String>> fieldsSorted = new ArrayList<>(m.getFields());
                    fieldsSorted.sort(Comparator.comparing(f -> f.getOrDefault("name", "")));
                    for (Map<String, String> fm : fieldsSorted) {
                        String fname = fm.get("name");
                        String ftype = fm.getOrDefault("type", null);
                        String ref = fm.getOrDefault("referenceType", null);
                        md.fields.add(new FieldDef(fname, ftype, ref));
                    }
                }
                ast.modelsById.put(mid, md);
            }
        }

        // 2) features -> actions grouped by feature.moduleName (we will make one repository per moduleName)
        if (e.getFeatures() != null) {
            for (FeatureInfo f : e.getFeatures()) {
                String featureKey = f.getModuleName() != null ? f.getModuleName() : (f.getFeatureId() != null ? f.getFeatureId() : "Feature");
                featureKey = featureKey.replaceAll("\\s+", "");
                for (ActionInfo a : f.getActions()) {
                    ActionDef ad = new ActionDef();
                    ad.actionId = a.getActionId();
                    ad.operationName = a.getOperationName() != null ? a.getOperationName() : normalizeOperationName(a.getActionId());
                    ad.httpMethod = a.getRequestMethod() == null ? "GET" : a.getRequestMethod().name();
                    ad.path = joinPaths(f.getPath(), a.getPath());
                    ad.description = a.getDescription();
                    ad.requestModelId = a.getRequestModel().getModelId();
                    ad.requestCollection = a.getRequestModel().isCollection();
                    ad.responseModelId = a.getResponseModel().getModelId();
                    ad.responseCollection = a.getResponseModel().isCollection();

                    // if referenced models missing, create placeholder model defs
                    if (ad.requestModelId != null && !ast.modelsById.containsKey(ad.requestModelId)) {
                        String pn = dropDtoSuffix(simpleName(ad.requestModelId));
                        ast.modelsById.put(ad.requestModelId, new ModelDef(ad.requestModelId, sanitizeForIdentifier(capitalize(pn))));
                    }
                    if (ad.responseModelId != null && !ast.modelsById.containsKey(ad.responseModelId)) {
                        String pn = dropDtoSuffix(simpleName(ad.responseModelId));
                        ast.modelsById.put(ad.responseModelId, new ModelDef(ad.responseModelId, sanitizeForIdentifier(capitalize(pn))));
                    }

                    ast.groupedActions.computeIfAbsent(featureKey, k -> new ArrayList<>()).add(ad);
                }
            }
        }

        // map modelId->className and prepare ordered models collection
        for (Map.Entry<String, ModelDef> en : ast.modelsById.entrySet()) {
            ast.modelIdToClass.put(en.getKey(), en.getValue().name);
            ast.models.put(en.getValue().name, en.getValue());
        }

        return ast;
    }

    /* ---------- rendering ---------- */
    private void renderLanguageFromAst(ModuleAst ast, String lang, Path outDir) throws IOException {
        // package root
        String basePkg = "com.photon." + ast.moduleId.toLowerCase();

        // 1) models
        Path modelsDir = outDir.resolve("src/main/java/" + pkgToPath(basePkg + ".models"));
        Files.createDirectories(modelsDir);
        for (ModelDef m : ast.models.values()) {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("moduleId", ast.moduleId);
            ctx.put("moduleIdLower", ast.moduleId.toLowerCase());
            ctx.put("className", m.name);
            List<Map<String,Object>> props = new ArrayList<>();
            for (FieldDef f : m.fields) {
                String propType = mapToJavaType(f, ast);
                Map<String,Object> p = new HashMap<>();
                p.put("name", sanitizeFieldName(f.name));
                p.put("capName", capitalize(sanitizeFieldName(f.name)));
                p.put("type", propType);
                props.add(p);
            }
            ctx.put("properties", props);
            Path outFile = modelsDir.resolve(m.name + ".java");
            renderTemplate("java", "model.mustache", ctx, outFile);
        }

        // 2) repositories (one per feature-group)
        Path repoDir = outDir.resolve("src/main/java/" + pkgToPath(basePkg + ".repository"));
        Files.createDirectories(repoDir);
        for (Map.Entry<String, List<ActionDef>> en : ast.groupedActions.entrySet()) {
            String rawFeatureName = en.getKey();
            String repoClass = deriveRepositoryName(rawFeatureName);
            Map<String,Object> ctx = new TreeMap<>();
            ctx.put("moduleId", ast.moduleId);
            ctx.put("moduleIdLower", ast.moduleId.toLowerCase());
            ctx.put("repoClassName", repoClass);
            List<Map<String,Object>> methods = new ArrayList<>();
            for (ActionDef a : en.getValue()) {
                Map<String,Object> mctx = new TreeMap<>();
                mctx.put("description", a.description == null ? a.actionId : a.description);
                mctx.put("methodName", sanitizeMethodName(a.operationName));
                // request
                if (a.requestModelId != null) {
                    String reqClazz = ast.modelIdToClass.get(a.requestModelId);
                    if (reqClazz == null) reqClazz = sanitizeForIdentifier(capitalize(dropDtoSuffix(simpleName(a.requestModelId))));
                    mctx.put("hasRequest", true);
                    mctx.put("requestType", reqClazz + (a.requestCollection ? "[]" : ""));
                } else {
                    mctx.put("hasRequest", false);
                    mctx.put("requestType", ""); // unused
                }
                // response
                if (a.responseModelId != null) {
                    String respClazz = ast.modelIdToClass.get(a.responseModelId);
                    if (respClazz == null) respClazz = sanitizeForIdentifier(capitalize(dropDtoSuffix(simpleName(a.responseModelId))));
                    if (a.responseCollection) {
                        mctx.put("returnType", "List<" + respClazz + ">");
                        mctx.put("imports", List.of("java.util.List", basePkg + ".models." + respClazz));
                    } else {
                        mctx.put("returnType", respClazz);
                        mctx.put("imports", List.of(basePkg + ".models." + respClazz));
                    }
                } else {
                    mctx.put("returnType", "void");
                    mctx.put("imports", List.of());
                }
                methods.add(mctx);
            }
            ctx.put("methods", methods);
            // compute aggregated imports
            Set<String> imports = new TreeSet<>();
            for (Map<String,Object> m : methods) {
                Object im = m.get("imports");
                if (im instanceof List) imports.addAll((List<String>)im);
            }
            ctx.put("imports", imports);
            Path outFile = repoDir.resolve(repoClass + ".java");
            renderTemplate("java", "repository.mustache", ctx, outFile);
        }

        // 3) sdk client
        Path clientDir = outDir.resolve("src/main/java/" + pkgToPath(basePkg + ".sdk"));
        Files.createDirectories(clientDir);
        Map<String,Object> clientCtx = new HashMap<>();
        clientCtx.put("moduleId", ast.moduleId);
        clientCtx.put("moduleIdLower", ast.moduleId.toLowerCase());
        renderTemplate("java", "sdk-client.mustache", clientCtx, clientDir.resolve("SdkClient.java"));
    }

    /* ---------- utilities & templates ---------- */

    private void renderTemplate(String lang, String templateName, Object ctx, Path outFile) throws IOException {
        try (Reader r = templateReader(lang, templateName);
             Writer w = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            Mustache m = mf.compile(r, templateName);
            m.execute(w, ctx).flush();
        } catch (IOException e) {
            log.error("Failed to render template {}/{} -> {}: {}", lang, templateName, outFile, e.getMessage());
            throw e;
        }
    }

    private Reader templateReader(String lang, String templateName) throws IOException {
        String resourcePath = "/templates/" + lang + "/" + templateName;
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is != null) return new InputStreamReader(is, StandardCharsets.UTF_8);
        Path p = templateRoot.resolve(lang).resolve(templateName);
        if (!Files.exists(p)) throw new FileNotFoundException("Template not found: " + p);
        return Files.newBufferedReader(p, StandardCharsets.UTF_8);
    }

    private void ensureTemplatesExist(String lang) throws IOException {
        // throw early if missing
        String cp = "/templates/" + lang + "/model.mustache";
        boolean onClasspath = getClass().getResourceAsStream(cp) != null;
        boolean onFs = Files.exists(templateRoot.resolve(lang).resolve("model.mustache"));
        if (!onClasspath && !onFs) throw new IllegalStateException("Templates for " + lang + " missing");
    }

    private void copyIfExists(Path src, Path destRoot) throws IOException {
        if (src == null || !Files.exists(src)) return;
        Files.walk(src).forEach(p -> {
            try {
                Path rel = src.relativize(p);
                Path dest = destRoot.resolve(rel.toString());
                if (Files.isDirectory(p)) Files.createDirectories(dest);
                else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(p, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }

    private void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
        if (!Files.exists(sourceDir)) throw new FileNotFoundException("Source does not exist: " + sourceDir);
        if (zipFile.getParent() != null) Files.createDirectories(zipFile.getParent());
        try (ZipOutputStream zs = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
            Files.walk(sourceDir)
                    .filter(p -> !Files.isDirectory(p))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString().replace("\\","/"));
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }

    private void deleteRecursively(Path p) throws IOException {
        if (!Files.exists(p)) return;
        Files.walk(p).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    private String mapToJavaType(FieldDef f, ModuleAst ast) {
        if (f == null) return "Object";
        if (f.referenceType != null && ast.modelIdToClass.containsKey(f.referenceType)) {
            return ast.modelIdToClass.get(f.referenceType);
        }
        if (f.type == null) return "Object";
        String t = f.type.toUpperCase();
        return switch (t) {
            case "STRING", "CHAR", "TEXT" -> "String";
            case "BOOLEAN" -> "Boolean";
            case "INTEGER", "INT" -> "Integer";
            case "LONG" -> "Long";
            case "DATE", "INSTANT", "DATETIME" -> "Instant";
            case "LIST", "SET", "COLLECTION" -> "List<Object>";
            case "MAP" -> "Map<String,Object>";
            case "DOUBLE", "FLOAT" -> "Double";
            default -> "Object";
        };
    }

    private String deriveRepositoryName(String raw) {
        String n = raw.replaceAll("Controller$", "");
        if (!n.endsWith("Repository")) n = n + "Repository";
        return sanitizeForIdentifier(capitalize(n));
    }

    private String sanitizeMethodName(String s) {
        if (s == null) return "call";
        String cleaned = s.replaceAll("[^A-Za-z0-9]", " ");
        return cleaned.replaceAll("\\s", "");
    }

    /* small helpers */
    private static String simpleName(String fqn) {
        if (fqn == null) return "Object";
        int last = fqn.lastIndexOf('.');
        return last >= 0 ? fqn.substring(last + 1) : fqn;
    }
    private static String sanitizeForIdentifier(String s) {
        if (s == null || s.isBlank()) return "X";
        String cleaned = s.replaceAll("[^A-Za-z0-9_]", "_");
        if (!Character.isLetter(cleaned.charAt(0)) && cleaned.charAt(0) != '_') cleaned = "_" + cleaned;
        return cleaned;
    }
    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
    private static String pkgToPath(String pkg) { return pkg.replace('.', '/'); }
    private static String dropDtoSuffix(String s) {
        if (s == null) return null;
        if (s.endsWith("Dto") || s.endsWith("DTO")) return s.substring(0, s.length() - 3);
        return s;
    }
    private static String sanitizeFieldName(String n) {
        if (n == null) return "field";
        String s = n.replaceAll("[^A-Za-z0-9]", "_");
        if (Character.isDigit(s.charAt(0))) s = "_" + s;
        return s;
    }
    private static String joinPaths(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        String combined = (a + "/" + b).replaceAll("//+", "/");
        if (!combined.startsWith("/")) combined = "/" + combined;
        return combined.replaceAll("/$", "");
    }
    private static String normalizeOperationName(String s) {
        if (s == null) return "call";
        return s.replaceAll("[^A-Za-z0-9]", "_").toLowerCase();
    }

    @PreDestroy
    public void onDestroy() { log.info("SdkGenerator shutting down"); }

    /* ---------- internal AST classes ---------- */
    static class ModuleAst {
        String moduleId;
        String moduleName;
        String clientId;
        String clientSecret;
        Map<String, ModelDef> models = new LinkedHashMap<>();
        Map<String, ModelDef> modelsById = new LinkedHashMap<>();
        Map<String, String> modelIdToClass = new LinkedHashMap<>();
        Map<String, List<ActionDef>> groupedActions = new LinkedHashMap<>();
    }
    static class ModelDef {
        String id;
        String name;
        List<FieldDef> fields = new ArrayList<>();
        ModelDef(String id, String name) { this.id = id; this.name = name; }
    }
    static class FieldDef { String name; String type; String referenceType; FieldDef(String n, String t, String r) { this.name = n; this.type = t; this.referenceType = r; } }
    static class ActionDef {
        String actionId;
        String operationName;
        String httpMethod;
        String path;
        String description;
        String requestModelId; boolean requestCollection;
        String responseModelId; boolean responseCollection;
    }
}