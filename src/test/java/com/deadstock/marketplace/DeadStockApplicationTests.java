package com.deadstock.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class DeadStockApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void homePageRendersListingImagesWithLocalFallbacks() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/images/listing-placeholder.svg")))
                .andExpect(content().string(containsString("class=\"listing-meta")));
    }

    @Test
    void publicApiProvidesMarketplaceOverview() throws Exception {
        mockMvc.perform(get("/api/public/overview"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"featuredListings\"")))
                .andExpect(content().string(containsString("\"categories\"")));
    }

    @Test
    void publicApiSearchesListingsWithoutFilters() throws Exception {
        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void registrationDoesNotAllowAdminRole() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "untrusted-admin@example.com",
                                  "password": "safe-password",
                                  "fullName": "Untrusted Admin",
                                  "role": "ADMIN",
                                  "companyName": "Example",
                                  "taxId": "EXAMPLE-123",
                                  "address": "1 Main Street",
                                  "city": "Mumbai",
                                  "state": "Maharashtra",
                                  "phone": "1234567890"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Only buyer and seller accounts can be registered")));
    }

    @Test
    @WithUserDetails("seller@metrosteel.com")
    void sellerDashboardRendersTheInventoryTable() throws Exception {
        mockMvc.perform(get("/seller/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/dashboard"))
                .andExpect(content().string(containsString("class=\"table-responsive\"")))
                .andExpect(content().string(containsString("data-value=")));
    }

    @Test
    @WithUserDetails("buyer@apexbuild.com")
    void buyerDashboardRendersOfferTotals() throws Exception {
        mockMvc.perform(get("/buyer/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/dashboard"))
                .andExpect(content().string(containsString("My Purchase Offers")))
                .andExpect(content().string(containsString("Confirmed Deals")))
                .andExpect(content().string(containsString("₹")));
    }
}
