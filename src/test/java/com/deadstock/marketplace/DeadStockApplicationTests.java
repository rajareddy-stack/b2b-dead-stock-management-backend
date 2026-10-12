package com.deadstock.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
