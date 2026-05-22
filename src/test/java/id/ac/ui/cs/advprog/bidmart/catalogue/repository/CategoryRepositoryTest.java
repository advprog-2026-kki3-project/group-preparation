package id.ac.ui.cs.advprog.bidmart.catalogue.repository;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository repository;

    @Test
    void testSaveAndFind() {
        Category category = new Category();
        category.setName("Electronics");

        repository.save(category);

        List<Category> result = repository.findAll();
        assertFalse(result.isEmpty());
    }
}
