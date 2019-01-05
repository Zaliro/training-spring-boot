package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.FreeProductException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

// Products controller
@Api(description = "Controller for CRUD operations on products entities")

@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;

    /**
     * Récupérer la liste des produits (GET)
     */
    @GetMapping(value = "/products")
    public MappingJacksonValue getProductsList() {

        Iterable<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");
        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);
        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }

    /**
     * Récupérer un porduit spécifique grâce à son identifiant (GET)
     */
    @ApiOperation(value = "Retrieves a product through its id")
    @GetMapping(value = "/products/{id}")
    public Product getProduct(@PathVariable int id) {

        Product produit = productDao.findById(id);

        if (produit == null)
            throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");

        return produit;
    }

    /**
     * Ajouter un produit (PUT)
     */
    @PutMapping(value = "/products")
    public ResponseEntity<Void> addProduct(@Valid @RequestBody Product product) {

        Product productAdded = productDao.save(product);

        if (productAdded == null)
            return ResponseEntity.noContent().build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    /**
     * Supprimer un produit (DELETE)
     */
    @DeleteMapping(value = "/products/{id}")
    public void deleteProduct(@PathVariable int id) {
        productDao.delete(id);
    }

    /**
     * Mettre à jour un produit (POST)
     */
    @PostMapping(value = "/products")
    public void updateProduct(@RequestBody Product product) {

        if (product.getPrix() <= 0)
            throw new FreeProductException("Price must be greater than zero.");

        productDao.save(product);
    }

    @GetMapping(value = "/products/margin")
    public MappingJacksonValue getProductsMarginsList() {

        Iterable<Product> products = productDao.findAll();
        ArrayList<AbstractMap.SimpleEntry<Product, Integer>> productsMargin = new ArrayList<>();

        for(Product p : products) {
            int margin = p.getPrix() - p.getPrixAchat();
            productsMargin.add(new AbstractMap.SimpleEntry<Product, Integer>(p, margin));
        }

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");
        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue jacksonResult = new MappingJacksonValue(productsMargin);
        jacksonResult.setFilters(listDeNosFiltres);

        return jacksonResult;
    }

    @GetMapping(value = "/products/sorted")
    public List<Product> getProductsListOrderedByName() {
        return productDao.findAllByOrderByNom();
    }

    // Pour les tests
    @GetMapping(value = "test/produits/{prix}")
    public List<Product> testeDeRequetes(@PathVariable int prix) {
        return productDao.chercherUnProduitCher(400);
    }
}
