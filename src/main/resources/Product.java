package com.dbtojson.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Product {
	 @JsonProperty("id")
	    private Long id;
	    @JsonProperty("name")
	    private String name;
	    @JsonProperty("price")
	    private double price;
	    @JsonProperty("category")
	    private String category;

	    // Getters and Setters
	    public Long getId() {
	        return id;
	    }

	    public void setId(Long id) {
	        this.id = id;
	    }

	    public String getName() {
	        return name;
	    }

	    public void setName(String name) {
	        this.name = name;
	    }

	    public double getPrice() {
	        return price;
	    }

	    public void setPrice(double price) {
	        this.price = price;
	    }

	    public String getCategory() {
	        return category;
	    }

	    public void setCategory(String category) {
	        this.category = category;
	    }

}
