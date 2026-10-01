package offeria.material_service.ai.provider;

import lombok.extern.slf4j.Slf4j;
import offeria.material_service.ai.MaterialAiSuggestionProvider;
import offeria.material_service.dto.response.AiMaterialSuggestionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "offeria.ai.material.provider",
        havingValue = "ollama"
)
public class OllamaMaterialAiSuggestionProvider implements MaterialAiSuggestionProvider {

    private final ChatClient chatClient;

    public OllamaMaterialAiSuggestionProvider(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Optional<AiMaterialSuggestionResponse> suggest(String materialText) {
        try {
            MaterialSuggestion suggestion = chatClient.prompt()
                    .system("""
                            You are a material terminology assistant for Offeria,
                            an Iraqi construction and oil & gas procurement platform.

                            Rules:
                            - canonicalEnglishName must be English.
                            - preferredIraqiName must be the material name commonly used
                              by Iraqi suppliers, engineers and procurement teams.
                            - standardArabicName must be Modern Standard Arabic.
                            - Never invent technical information.
                            - If unit, category or specification cannot be determined
                              confidently, return null.
                            - Never approve the material.
                            - Do not add explanations.
                            """)
                    .user("""
                            Analyze this material:

                            %s
                            """.formatted(materialText))
                    .call()
                    .entity(MaterialSuggestion.class);

            if (suggestion == null
                    || suggestion.canonicalEnglishName() == null
                    || suggestion.canonicalEnglishName().isBlank()) {
                return Optional.empty();
            }

            return Optional.of(
                    AiMaterialSuggestionResponse.unapproved(
                            materialText,
                            suggestion.canonicalEnglishName(),
                            suggestion.preferredIraqiName(),
                            suggestion.standardArabicName(),
                            suggestion.unit(),
                            suggestion.category(),
                            suggestion.specification()
                    )
            );

        } catch (RuntimeException exception) {
            log.warn(
                    "Ollama material suggestion failed for input: {}",
                    materialText,
                    exception
            );
            return Optional.empty();
        }
    }

    private record MaterialSuggestion(
            String canonicalEnglishName,
            String preferredIraqiName,
            String standardArabicName,
            String unit,
            String category,
            String specification
    ) {
    }
}
