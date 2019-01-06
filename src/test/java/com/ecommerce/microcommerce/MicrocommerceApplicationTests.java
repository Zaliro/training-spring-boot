package com.ecommerce.microcommerce;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.controller.ProductController;
import com.google.gson.Gson;
import net.minidev.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MicrocommerceApplicationTests {

    @Autowired
    private ProductController controller;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductDao productDao;

    private Gson gsonBuilder;

    @Test
    public void contexLoads() throws Exception {
        assertThat(controller).isNotNull();

        this.gsonBuilder = new Gson();
        assertThat(this.gsonBuilder).isNotNull();
    }

    // region Crud Operations

    //region ProductList
    @Test
    public void getProductsList_Should_ReturnProductListAsJson() throws Exception {

        Product product1 = new Product(1, "Product1", 100, 10);
        Product product2 = new Product(2, "Product2", 200, 50);

        List<Product> productsList = new ArrayList<>();
        productsList.add(product1);
        productsList.add(product2);

        given(productDao.findAll()).willReturn(productsList);

        mockMvc.perform(get("/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(product1.getId())))
                .andExpect(jsonPath("$[0].nom", is(product1.getNom())))
                .andExpect(jsonPath("$[0].prix", is(product1.getPrix())))
                .andExpect(jsonPath("$[0].prixAchat", is(product1.getPrixAchat())));
    }
    //endregion

    //region GetProductById
    @Test
    public void getProductById_Should_ReturnProductDetailsAsJson() throws Exception {

        int productId = 1;
        Product product1 = new Product(productId, "Product1", 100, 10);

        given(productDao.findById(productId)).willReturn(product1);

        mockMvc.perform(get(String.format("/products/%s", productId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nom", is(product1.getNom())))
                .andExpect(jsonPath("$.prix", is(product1.getPrix())))
                .andExpect(jsonPath("$.prixAchat", is(product1.getPrixAchat())));
    }

    @Test
    public void getProductById_Should_ReturnProductNotFound() throws Exception {

        int productId = 1;
        int unknownId = 2;

        Product product1 = new Product(productId, "Product1", 100, 10);

        given(productDao.findById(productId)).willReturn(product1);

        mockMvc.perform(get(String.format("/products/%s", unknownId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    //endregion

    //region AddProduct
    @Test
    public void addProduct_Should_ReturnCreated() throws Exception {

        Product newProduct = new Product(1, "NewProduct", 666, 666);
        String newProductLocation = String.format("http://localhost/products/%s", newProduct.getId());

        given(productDao.save(Mockito.any(Product.class)))
                .willReturn(newProduct);

        Gson gsonBuilder = new Gson();
        String newProductAsJson = gsonBuilder.toJson(newProduct);

        mockMvc.perform(
                put("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newProductAsJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", newProductLocation));
    }

    @Test
    public void addProductWithInvalidArgs_Should_ThrowError() throws Exception {

        Product newProduct = new Product();
        newProduct.setNom("NewProduct");

        given(productDao.save(Mockito.any(Product.class)))
                .willReturn(newProduct);

        Gson gsonBuilder = new Gson();
        String newProductAsJson = gsonBuilder.toJson(newProduct);

        mockMvc.perform(
                put("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newProductAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addProductWithPriceZero_Should_ThrowError() throws Exception {

        Product newProduct = new Product(1, "NewProduct", 0, 666);

        given(productDao.save(Mockito.any(Product.class)))
                .willReturn(newProduct);

        Gson gsonBuilder = new Gson();
        String newProductAsJson = gsonBuilder.toJson(newProduct);

        mockMvc.perform(
                put("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newProductAsJson))
                .andExpect(status().isBadRequest());
    }
    //endregion

    //region UpdateProduct
    @Test
    public void updateProduct_Should_ReturnOk() throws Exception {

        Product newProduct = new Product(1, "NewProduct", 666, 666);

        given(productDao.save(Mockito.any(Product.class)))
                .willReturn(newProduct);

        Gson gsonBuilder = new Gson();
        String newProductAsJson = gsonBuilder.toJson(newProduct);

        mockMvc.perform(
                post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newProductAsJson))
                .andExpect(status().isOk());
    }

    @Test
    public void updateProductWithPriceZero_Should_ThrowError() throws Exception {

        Product newProduct = new Product(1, "NewProduct", 0, 666);

        given(productDao.save(Mockito.any(Product.class)))
                .willReturn(newProduct);

        Gson gsonBuilder = new Gson();
        String newProductAsJson = gsonBuilder.toJson(newProduct);

        mockMvc.perform(
                post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newProductAsJson))
                .andExpect(status().isUnprocessableEntity());
    }
    //endregion

    //endregion

    //region Specific Operations

    //region ProductsMargin
    @Test
    public void getProductsMargin_Should_ReturnProductMarginAsJson() throws Exception {

        Product product1 = new Product(1, "Product1", 100, 10);
        Product product2 = new Product(2, "Product2", 200, 50);

        List<Product> productsList = new ArrayList<>();
        productsList.add(product1);
        productsList.add(product2);

        given(productDao.findAll()).willReturn(productsList);

        JSONArray product1ExpectedMargin = new JSONArray();
        product1ExpectedMargin.add(90);

        JSONArray product2ExpectedMargin = new JSONArray();
        product2ExpectedMargin.add(150);

        mockMvc.perform(get("/products/margin")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].*", is(product1ExpectedMargin)))
                .andExpect(jsonPath("$[1].*", is(product2ExpectedMargin)));
    }
    //endregion

    //endregion
}