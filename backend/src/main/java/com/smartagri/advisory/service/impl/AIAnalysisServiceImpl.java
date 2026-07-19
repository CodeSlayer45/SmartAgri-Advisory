package com.smartagri.advisory.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.smartagri.advisory.dto.WeatherSnapshot;
import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.service.AIAnalysisService;
import com.smartagri.advisory.service.WeatherService;

@Service
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AIAnalysisServiceImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final FarmFieldRepository fieldRepository;
    private final WeatherService weatherService;

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.enabled:false}")
    private boolean enabled;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String model;

    public AIAnalysisServiceImpl(FarmFieldRepository fieldRepository, WeatherService weatherService) {
        this.fieldRepository = fieldRepository;
        this.weatherService = weatherService;
    }

    private boolean isEnabled() { return enabled && apiKey != null && !apiKey.isBlank() && !apiKey.contains("your-openai"); }
    private FarmField getField(Long id) { return fieldRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Field not found: " + id)); }
    private long getCropAge(FarmField f) { return f.getSowingDate() == null ? 0 : ChronoUnit.DAYS.between(f.getSowingDate(), LocalDate.now()); }

    @Override
    public Map<String, Object> analyzeCropHealth(Long fieldId, String imageBase64, String imageFormat) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (imageBase64 == null || imageBase64.isBlank()) {
            result.put("status", "error"); result.put("message", "Please provide a crop photo"); return result;
        }
        try {
            FarmField field = getField(fieldId);
            long cropAge = getCropAge(field);
            String analysis;
            if (isEnabled()) {
                String prompt = String.format("""
                    You are an expert agricultural plant pathologist analyzing a crop photo.
                    Field: %s, Crop: %s, Age: %d days, Location: %s
                    Examine and list: 1. DISEASE 2. CONFIDENCE 3. SEVERITY(1-10) 4. SYMPTOMS 5. CAUSE 6. TREATMENT(3 steps) 7. PREVENTION 8. URGENCY
                    Be practical for an Indian farmer.""", field.getFieldName(), field.getCropName(), cropAge, field.getLocation());
                analysis = callOpenAIVision(prompt, imageBase64, imageFormat);
            } else {
                analysis = """
                    1. DISEASE: Early Blight (Alternaria solani) — common fungal infection
                    2. CONFIDENCE: HIGH
                    3. SEVERITY: 6/10 — moderate infection, treat immediately
                    4. SYMPTOMS: Dark brown spots with concentric rings on lower leaves, yellowing around lesions
                    5. CAUSE: High humidity (80%+) + warm temperatures (22-28°C) create perfect conditions
                    6. TREATMENT:
                       - Apply Mancozeb 75% WP (2.5g/L water) immediately on all affected leaves
                       - Remove and destroy severely infected leaves to prevent spread
                       - Spray Chlorothalonil (2ml/L) after 7 days as follow-up
                    7. PREVENTION: Maintain proper plant spacing for airflow, avoid overhead irrigation, practice crop rotation
                    8. URGENCY: TODAY — infection spreads rapidly in current weather conditions""";
            }
            result.put("status", "success"); result.put("type", "crop_health");
            result.put("analysis", analysis); result.put("fieldId", fieldId);
            result.put("cropName", field.getCropName()); result.put("timestamp", LocalDate.now().toString());
            result.put("aiEnhanced", isEnabled());
        } catch (Exception e) {
            log.error("Crop health analysis failed: {}", e.getMessage());
            result.put("status", "error"); result.put("message", "Analysis failed: " + e.getMessage());
        }
        return result;
    }

    @Override
    public String askFarmerQuestion(Long fieldId, String question) {
        if (question == null || question.isBlank()) return "Please ask a question about your farm.";
        try {
            FarmField field = getField(fieldId);
            long cropAge = getCropAge(field);
            WeatherSnapshot w = weatherService.getByFieldId(fieldId);
            if (isEnabled()) {
                String prompt = String.format("""
                    You are a friendly agricultural expert for an Indian farmer.
                    Field: %s, Crop: %s (%d days), Location: %s
                    Weather: %.1f°C, %d%% humidity, %.1fmm rain
                    Question: %s
                    Answer simply and practically, under 200 words.""",
                    field.getFieldName(), field.getCropName(), cropAge, field.getLocation(),
                    w.temperatureC(), w.humidityPercent(), w.rainfallMm(), question);
                return callOpenAI(prompt);
            }
            String q = question.toLowerCase();
            if (q.contains("water") || q.contains("irrigation")) {
                return "Based on today's weather (%.1f°C, %d%% humidity), your %s needs light irrigation. Water deeply once every 3-4 days during vegetative stage. Avoid overhead watering in high humidity to prevent fungal diseases.".formatted(w.temperatureC(), w.humidityPercent(), field.getCropName());
            } else if (q.contains("fertilizer") || q.contains("nutrient")) {
                return "For %s at %d days old, apply a balanced NPK (19:19:19) at 2kg/acre. After 15 days, switch to high-potassium fertilizer (00:00:50) at 1.5kg/acre for better flowering and fruit development.".formatted(field.getCropName(), cropAge);
            } else if (q.contains("pest") || q.contains("disease") || q.contains("spray")) {
                return "At this growth stage, watch for aphids and whiteflies. Apply Neem oil (5ml/L) as preventive spray every 10 days. If you see active infestation, use Imidacloprid 17.8% SL (0.5ml/L) — but stop 15 days before harvest.";
            } else {
                return "Good question! For your %s crop (%d days old), the most important things right now are: 1) Monitor soil moisture daily 2) Check for pest signs on leaf undersides 3) Ensure adequate nutrition. Would you like specific advice about watering, fertilizing, or pest control?".formatted(field.getCropName(), cropAge);
            }
        } catch (Exception e) {
            return "I couldn't process your question. Error: " + e.getMessage();
        }
    }

    @Override
    public Map<String, Object> analyzeMarketPrice(String cropName, String location) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            String analysis;
            if (isEnabled()) {
                String prompt = "Crop: %s, Location: %s. List: 1.CURRENT_PRICE_RANGE 2.PRICE_TREND 3.BEST_TIME 4.NEARBY_MANDIS 5.DEMAND 6.TIPS".formatted(cropName, location != null ? location : "India");
                analysis = callOpenAI(prompt);
            } else {
                analysis = """
                    1. CURRENT_PRICE_RANGE: ₹1,800 - ₹2,200 per quintal
                    2. PRICE_TREND: Stable with upward movement expected in next 2 weeks
                    3. BEST_TIME: Sell in 10-14 days for potentially 8-12% higher price
                    4. NEARBY_MANDIS: Pune (best rate), Mumbai (higher demand), Nashik (competitive pricing)
                    5. DEMAND: HIGH — current market demand is strong due to off-season supply shortage
                    6. TIPS: Grade your produce before selling — graded produce gets 15-20% premium""";
            }
            result.put("status", "success"); result.put("crop", cropName); result.put("location", location);
            result.put("analysis", analysis); result.put("generatedAt", LocalDate.now().toString());
            result.put("aiEnhanced", isEnabled());
        } catch (Exception e) {
            result.put("status", "error"); result.put("message", e.getMessage());
        }
        return result;
    }

    @Override
    public String analyzeWeatherImpact(FarmField field, WeatherSnapshot weather, long cropAgeDays) {
        try {
            if (isEnabled()) {
                String prompt = String.format("""
                    Crop: %s (%d days), Location: %s. Weather: %.1f°C, %d%% humidity, %.1fmm rain, %.1f m/s wind.
                    Analyze: 1.GOOD/BAD for crop? 2.Risks? 3.Action today? 4.If continues 3 days? 5.Recommendation.""",
                    field.getCropName(), cropAgeDays, field.getLocation(),
                    weather.temperatureC(), weather.humidityPercent(), weather.rainfallMm(), weather.windSpeedMs());
                return callOpenAI(prompt);
            }
            String risk = weather.humidityPercent() >= 75 ? "HIGH disease risk" : "LOW disease risk";
            return """
                1. WEATHER STATUS: %s — Current weather is %s for %s
                2. RISKS: %s — %s conditions can trigger fungal infections in %s
                3. ACTION TODAY: %s
                4. 3-DAY OUTLOOK: Prolonged warm and humid weather will increase disease pressure significantly
                5. RECOMMENDATION: Apply preventive fungicide spray if not done in last 7 days. Ensure proper drainage."""
                .formatted(
                    weather.humidityPercent() >= 75 ? "FAVORABLE for disease" : "NORMAL",
                    weather.temperatureC() >= 25 ? "warm and humid" : "moderate",
                    field.getCropName(),
                    risk,
                    weather.humidityPercent() >= 70 ? "Humid" : "Dry",
                    field.getCropName(),
                    weather.rainfallMm() > 5 ? "Avoid irrigation — enough moisture from rain" : "Normal irrigation schedule can continue"
                );
        } catch (Exception e) {
            return "Weather analysis unavailable: " + e.getMessage();
        }
    }

    @Override
    public Map<String, Object> analyzeGrowthStage(FarmField field, long cropAgeDays) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            WeatherSnapshot w = weatherService.getByFieldId(field.getId());
            String stage;
            if (cropAgeDays <= 15) stage = "🌱 GERMINATION / EARLY SEEDLING";
            else if (cropAgeDays <= 30) stage = "🌿 VEGETATIVE GROWTH (Early)";
            else if (cropAgeDays <= 60) stage = "🌿 VEGETATIVE GROWTH (Active)";
            else if (cropAgeDays <= 80) stage = "🌸 FLOWERING";
            else if (cropAgeDays <= 100) stage = "🍎 FRUIT / GRAIN FORMATION";
            else if (cropAgeDays <= 130) stage = "🌾 MATURATION";
            else stage = "✅ READY FOR HARVEST";

            result.put("crop", field.getCropName()); result.put("fieldName", field.getFieldName());
            result.put("daysSinceSowing", cropAgeDays); result.put("currentStage", stage);

            String report;
            if (isEnabled()) {
                String prompt = "Crop: %s, Stage: %s (%d days), Location: %s, Weather: %.1f°C, %d%% humidity. List 1.STAGE_DESCRIPTION 2.KEY_ACTIVITIES(3) 3.IRRIGATION 4.FERTILIZER 5.PEST_WARNING 6.NEXT_STEPS 7.WARNING".formatted(
                    field.getCropName(), stage, cropAgeDays, field.getLocation(), w.temperatureC(), w.humidityPercent());
                report = callOpenAI(prompt);
            } else {
                String tips = switch (stage.split(" ")[0]) {
                    case "🌱" -> "1. STAGE_DESCRIPTION: Seeds are germinating. Keep soil consistently moist but not waterlogged.\n2. KEY_ACTIVITIES: Protect from birds, maintain soil moisture, watch for damping-off disease\n3. IRRIGATION: Light sprinkling twice daily\n4. FERTILIZER: No fertilizer needed yet\n5. PEST_WARNING: Cutworms and ants\n6. NEXT_STEPS: Prepare for transplanting/thinning in 2 weeks\n7. WARNING: Don't overwater — causes seed rot";
                    case "🌿" -> "1. STAGE_DESCRIPTION: Plant is developing leaves and stem — rapid growth phase\n2. KEY_ACTIVITIES: Apply nitrogen fertilizer, install support stakes, weed management\n3. IRRIGATION: Water every 2-3 days, 1 inch per week\n4. FERTILIZER: Urea (1kg/acre) after 25 days\n5. PEST_WARNING: Aphids and leaf miners\n6. NEXT_STEPS: Prepare for flowering support\n7. WARNING: Don't skip weeding — weeds compete for nutrients";
                    default -> "1. STAGE_DESCRIPTION: %s is %d days old and in %s stage\n2. KEY_ACTIVITIES: Monitor for pests, maintain irrigation, apply balanced fertilizer\n3. IRRIGATION: Water based on soil moisture — check 2 inches deep\n4. FERTILIZER: Apply potash-based fertilizer for better yields\n5. PEST_WARNING: Regular scouting recommended\n6. NEXT_STEPS: Prepare for harvest planning\n7. WARNING: Don't ignore early signs of stress".formatted(field.getCropName(), cropAgeDays, stage);
                };
                report = tips;
            }
            result.put("aiReport", report); result.put("stage", stage); result.put("weather", w);
            result.put("aiEnhanced", isEnabled());
        } catch (Exception e) {
            result.put("status", "error"); result.put("message", e.getMessage());
        }
        return result;
    }

    private String callOpenAI(String userPrompt) {
        return callOpenAI(List.of(
                Map.of("role", "system", "content", "You are an expert agricultural advisor for Indian smallholder farmers."),
                Map.of("role", "user", "content", userPrompt)), 0.3, 600);
    }

    private String callOpenAIVision(String textPrompt, String imageBase64, String imageFormat) {
        String dataUrl = "data:image/" + imageFormat + ";base64," + imageBase64;
        return callOpenAI(List.of(
                Map.of("role", "system", "content", "You are an expert plant pathologist for Indian farmers."),
                Map.of("role", "user", "content", List.of(Map.of("type", "text", "text", textPrompt), Map.of("type", "image_url", "image_url", Map.of("url", dataUrl, "detail", "high"))))), 0.2, 800);
    }

    private String callOpenAI(List<?> messages, double temperature, int maxTokens) {
        Map<String, Object> body = Map.of("model", model, "temperature", temperature,
                "max_tokens", maxTokens, "messages", messages);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        ResponseEntity<Map> response = restTemplate.postForEntity("https://api.openai.com/v1/chat/completions", new HttpEntity<>(body, headers), Map.class);
        return extractContent(response.getBody());
    }

    private String extractContent(Map<?, ?> body) {
        if (body == null) return "No response";
        try {
            List<?> choices = (List<?>) body.get("choices");
            if (choices == null || choices.isEmpty()) return "No analysis";
            Map<?, ?> msg = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            return msg == null || msg.get("content") == null ? "Empty" : String.valueOf(msg.get("content")).trim();
        } catch (Exception e) { return "Parse error"; }
    }
}
