package id.ac.ui.cs.advprog.bidmart.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmart.auth.service.PermissionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.SessionService;
import id.ac.ui.cs.advprog.bidmart.auth.service.TokenService;
import id.ac.ui.cs.advprog.bidmart.notification.dto.NotificationPreferenceRequest;
import id.ac.ui.cs.advprog.bidmart.notification.model.NotificationPreference;
import id.ac.ui.cs.advprog.bidmart.notification.service.NotificationPreferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationPreferenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationPreferenceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationPreferenceService notificationPreferenceService;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SessionService sessionService;

    @Test
    void getPreferences_returnsPreference() throws Exception {
        NotificationPreference preference = new NotificationPreference("alice");
        preference.update(true, false, Instant.parse("2026-05-22T00:00:00Z"));
        when(notificationPreferenceService.findOrCreate("alice")).thenReturn(preference);

        mockMvc.perform(get("/api/notification-preferences/alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.emailEnabled").value(true))
                .andExpect(jsonPath("$.pushEnabled").value(false));
    }

    @Test
    void updatePreferences_returnsUpdatedPreference() throws Exception {
        NotificationPreference preference = new NotificationPreference("alice");
        preference.update(false, true, Instant.parse("2026-05-22T00:00:00Z"));
        when(notificationPreferenceService.update("alice", false, true)).thenReturn(preference);

        NotificationPreferenceRequest request = new NotificationPreferenceRequest(false, true);

        mockMvc.perform(put("/api/notification-preferences/alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.emailEnabled").value(false))
                .andExpect(jsonPath("$.pushEnabled").value(true));
    }
}
