package com.dbtojson.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Person {
	
	 @JsonProperty("id")
	    private Long userId;
	    @JsonProperty("full_name")
	    private String fullName;
	    @JsonProperty("date_of_birth")
	    private String dateOfBirth;
	    @JsonProperty("street")
	    private String street;
	    @JsonProperty("city")
	    private String city;
	    @JsonProperty("postal_code")
	    private String postalCode;
		public Long getUserId() {
			return userId;
		}
		public void setUserId(Long userId) {
			this.userId = userId;
		}
		public String getFullName() {
			return fullName;
		}
		public void setFullName(String fullName) {
			this.fullName = fullName;
		}
		public String getDateOfBirth() {
			return dateOfBirth;
		}
		public void setDateOfBirth(String dateOfBirth) {
			this.dateOfBirth = dateOfBirth;
		}
		public String getStreet() {
			return street;
		}
		public void setStreet(String street) {
			this.street = street;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getPostalCode() {
			return postalCode;
		}
		public void setPostalCode(String postalCode) {
			this.postalCode = postalCode;
		}

	   

}
