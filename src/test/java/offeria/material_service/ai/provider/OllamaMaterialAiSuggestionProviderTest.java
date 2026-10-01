package offeria.material_service.ai.provider;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OllamaMaterialAiSuggestionProviderTest {

    @Test
    void shouldCreateProviderWithChatClientBuilder() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);

        when(builder.build()).thenReturn(chatClient);

        OllamaMaterialAiSuggestionProvider provider =
                new OllamaMaterialAiSuggestionProvider(builder);

        assertNotNull(provider);
    }
}
