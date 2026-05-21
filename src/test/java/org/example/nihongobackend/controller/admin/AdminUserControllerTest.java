package org.example.nihongobackend.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nihongobackend.dto.request.admin.AdminUserActiveRequest;
import org.example.nihongobackend.dto.response.admin.AdminUserPageResponse;
import org.example.nihongobackend.dto.response.admin.AdminUserRowResponse;
import org.example.nihongobackend.exception.GlobalExceptionHandler;
import org.example.nihongobackend.service.admin.AdminUserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AdminUserManagementService adminUserManagementService;

    @BeforeEach
    void setUp() {
        adminUserManagementService = mock(AdminUserManagementService.class);
        AdminUserController controller = new AdminUserController(adminUserManagementService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listUsers_returnsPage() throws Exception {
        AdminUserRowResponse row = new AdminUserRowResponse();
        row.setId(UUID.randomUUID());
        row.setEmail("u@test.com");
        row.setName("U");
        row.setRole("USER");
        row.setActive(true);

        AdminUserPageResponse page = new AdminUserPageResponse();
        page.setContent(List.of(row));
        page.setTotalElements(1);
        page.setTotalPages(1);
        page.setNumber(0);
        page.setSize(20);

        when(adminUserManagementService.listUsers(0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/admin/users").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("u@test.com"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void setActive_callsService() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(adminUserManagementService).setUserActive(eq(id), eq(false));

        AdminUserActiveRequest body = new AdminUserActiveRequest();
        body.setActive(false);

        mockMvc.perform(patch("/api/admin/users/" + id + "/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(adminUserManagementService).setUserActive(id, false);
    }
}
