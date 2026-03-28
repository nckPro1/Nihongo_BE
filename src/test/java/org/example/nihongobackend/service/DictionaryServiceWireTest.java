package org.example.nihongobackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nihongobackend.config.DeepSeekProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Mock HTTP — không gọi thật DeepSeek.
 */
@ExtendWith(MockitoExtension.class)
class DictionaryServiceWireTest {

    @Mock
    private RestTemplate restTemplate;

    private DictionaryService dictionaryService;

    @BeforeEach
    void setUp() {
        DeepSeekProperties props = new DeepSeekProperties();
        props.setApiKey("sk-test");
        props.setBaseUrl("https://api.deepseek.com");
        props.setModel("deepseek-chat");
        dictionaryService = new DictionaryService(restTemplate, new ObjectMapper(), props);
    }

    @Test
    void searchVietToNhat_parsesDeepSeekJson() {
        String envelope = "{\"choices\":[{\"message\":{\"content\":\"{\\\"japanese\\\":\\\"仕事 (しごと)\\\"}\"}}]}";
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(envelope));

        Map<String, String> m = dictionaryService.searchVietToNhat("công việc");

        assertEquals("công việc", m.get("keyword"));
        assertEquals("仕事 (しごと)", m.get("ja"));
        assertEquals("仕事 (しごと)", m.get("kanji"));
    }

    @Test
    void searchNhatToViet_parsesDeepSeekJson() {
        String envelope = "{\"choices\":[{\"message\":{\"content\":\"{\\\"vietnamese\\\":\\\"công việc\\\",\\\"reading\\\":\\\"しごと\\\"}\"}}]}";
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(envelope));

        Map<String, String> m = dictionaryService.searchNhatToViet("仕事");

        assertEquals("仕事", m.get("keyword"));
        assertEquals("công việc", m.get("meaning"));
        assertTrue(m.get("meaningFormatted").contains("công việc"));
        assertTrue(m.get("meaningFormatted").contains("—"));
    }
}
