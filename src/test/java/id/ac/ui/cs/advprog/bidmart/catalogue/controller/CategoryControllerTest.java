package id.ac.ui.cs.advprog.bidmart.catalogue.controller;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;
import id.ac.ui.cs.advprog.bidmart.catalogue.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryControllerTest {

    private MockMvc mockMvc;
    private CategoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CategoryRepository.class);
        CategoryController controller = new CategoryController(repository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetAllCategories() throws Exception {
        when(repository.findAll()).thenReturn(List.of(new Category()));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCategoryById() throws Exception {
        when(repository.findById("1"))
                .thenReturn(java.util.Optional.of(new Category()));

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateCategory() throws Exception {
        Category category = new Category();
        category.setName("Electronics");

        when(repository.save(Mockito.any())).thenReturn(category);

        String json = """
                {
                    "name": "Electronics"
                }
                """;

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteCategory() throws Exception {
        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchCategory() throws Exception {
        when(repository.findByNameContainingIgnoreCase("elec"))
                .thenReturn(List.of(new Category()));

        mockMvc.perform(get("/categories/search")
                        .param("name", "elec"))
                .andExpect(status().isOk());
    }
}