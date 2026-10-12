package com.deadstock.marketplace.config;

import com.deadstock.marketplace.model.Category;
import com.deadstock.marketplace.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("production")
public class ProductionCatalogInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public ProductionCatalogInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() != 0) {
            return;
        }
        categoryRepository.saveAll(List.of(
                new Category("Construction & Raw Metals", "Excess steel beams, rebar, aluminum sheets, timber, and hardware surplus.", "bi-hammer"),
                new Category("Packaging & Plastics", "Overstocked corrugated boxes, stretch wrap rolls, pallets, and HDPE containers.", "bi-box-seam"),
                new Category("Restaurant & Food Service", "Unused commercial food equipment, bulk stainless steel furniture, and dry goods.", "bi-cup-hot"),
                new Category("Industrial Machinery & Parts", "Dormant electric motors, CNC tooling, pumps, hydraulic valves, and electrical switchgear.", "bi-gear-wide-connected"),
                new Category("Textiles & Raw Fabrics", "Surplus woven cotton, denim rolls, synthetic yarns, and apparel overruns.", "bi-scissors"),
                new Category("Chemicals & Coatings", "Industrial paints, epoxy resins, solvents, lubricants, and surplus adhesives.", "bi-droplet-half")
        ));
    }
}
