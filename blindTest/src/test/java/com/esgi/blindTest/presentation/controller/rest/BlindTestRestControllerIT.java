package com.esgi.blindTest.presentation.controller.rest;

import com.esgi.blindTest.domain.exception.ReponseDejaReserveeException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.usecase.AjouterBlindTestUseCase;
import com.esgi.blindTest.domain.usecase.ConsulterBlindTestUseCase;
import com.esgi.blindTest.domain.usecase.MettreEnPauseBlindTestUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'integration leger : le contexte Spring est demarre, les use cases sont mockes.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BlindTestRestControllerIT {

    @MockitoBean
    ConsulterBlindTestUseCase consulterBlindTestUseCase;

    @MockitoBean
    AjouterBlindTestUseCase ajouterBlindTestUseCase;

    @MockitoBean
    MettreEnPauseBlindTestUseCase mettreEnPauseBlindTestUseCase;

    @Autowired
    MockMvc mockMvc;

    @Test
    void refuse_un_appel_non_authentifie() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/blindtests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void liste_les_blind_tests() throws Exception {
        when(consulterBlindTestUseCase.apply()).thenReturn(List.of(new BlindTest("Soiree ESGI")));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/blindtests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Soiree ESGI"))
                .andExpect(jsonPath("$[0].nombreDeParticipants").value(0));
    }

    @Test
    @WithMockUser
    void cree_un_blind_test() throws Exception {
        when(ajouterBlindTestUseCase.apply(anyString())).thenReturn(new BlindTest("Soiree ESGI"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/blindtests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Soiree ESGI\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Soiree ESGI"));
    }

    @Test
    @WithMockUser
    void refuse_le_second_clic_avec_un_message_clair() throws Exception {
        doThrow(new ReponseDejaReserveeException())
                .when(mettreEnPauseBlindTestUseCase).apply(any(), any());

        // Le blind test est designe par son nom : passe en variable d'URI pour que
        // l'espace soit encode une seule fois.
        mockMvc.perform(MockMvcRequestBuilders.post("/api/blindtests/{nom}/pause", "Soiree ESGI"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @WithMockUser
    void refuse_un_nom_de_blind_test_vide() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/blindtests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
