package id.ac.ui.cs.advprog.bidmart.catalogue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;
import id.ac.ui.cs.advprog.bidmart.catalogue.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CategoryRestControllerTest {

    private MockMvc mockMvc;

    private CategoryRepository categoryRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        categoryRepository = mock(CategoryRepository.class);

        CategoryRestController controller =
                new CategoryRestController(categoryRepository);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetAllCategories() throws Exception {

        Category category1 = new Category();

        category1.setName("Electronics");

        Category category2 = new Category();

        category2.setName("Handphone");

        when(categoryRepository.findAll())
                .thenReturn(List.of(category1, category2));

        mockMvc.perform(get("/api/categories"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].name")
                        .value("Electronics"))

                .andExpect(jsonPath("$[1].name")
                        .value("Handphone"));

        verify(categoryRepository).findAll();
    }

    @Test
    void testCreateCategory() throws Exception {

        Category category = new Category();

        category.setName("Electronics");

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(category);

        mockMvc.perform(post("/api/categories")

                        .contentType(MediaType.APPLICATION_JSON)

                        .content(objectMapper.writeValueAsString(category)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.name")
                        .value("Electronics"));

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testGetCategoryByIdFound() throws Exception {

        Category category = new Category();

        category.setName("Electronics");

        when(categoryRepository.findById("1"))
                .thenReturn(Optional.of(category));

        mockMvc.perform(get("/api/categories/1"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.name")
                        .value("Electronics"));

        verify(categoryRepository).findById("1");
    }

    @Test
    void testGetCategoryByIdNotFound() throws Exception {

        when(categoryRepository.findById("1"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/1"))

                .andExpect(status().isNotFound());

        verify(categoryRepository).findById("1");
    }
}