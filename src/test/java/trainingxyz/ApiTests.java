package trainingxyz;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import org.junit.jupiter.api.Test;

import io.restassured.response.Response;
import models.Product;

import static io.restassured.RestAssured.given;

public class ApiTests {

	static final String baseURL = "http://localhost/api_testing";
	@Test
	public void getCategories() {
		String endpoint = baseURL +"/category/read.php";
		var response = given().when().get(endpoint).then();
		response.log().body();
	}
	
	@Test
	public void getProduct() { // accessing response body
		String endpoint = baseURL +"/product/read_one.php";
		given().
			queryParam("id", 2).
		when().
			get(endpoint).
		then().
		assertThat().
			statusCode(200).
			body("id", equalTo("2")).
			body("name", equalTo("Cross-Back Training Tank")).
			body("description", equalTo("The most awesome phone of 2013!")).
			body("price", equalTo("299.00")).
			body("category_id", equalTo(2)).
			body("category_name", equalTo("Active Wear - Women"));
		/*
		 var response =
				given().
					queryParam("id", 2).
				when().
					get(endpoint).
				then();
		 response.log().body();*/
	}
	
	@Test
	public void getProducts() { //verify complex response bodies
		String endpoint = baseURL +"/product/read.php";
		given().
		when().
			get(endpoint).
		then().
			log().
			body().
			assertThat().
				statusCode(200).
				header("Content-Type", equalTo("application/json; charset=UTF-8")).
				body("records.size()", greaterThan(0)).
				body("records.id", everyItem(notNullValue())).
				body("records.name", everyItem(notNullValue())).
				body("records.description", everyItem(notNullValue())).
				body("records.price", everyItem(notNullValue())).
				body("records.category_id", everyItem(notNullValue())).
				body("records.category_name", everyItem(notNullValue())).
				body("records.id[0]", equalTo(29));
	}
	
	@Test
	public void createProduct() { // post
		String endpoint = baseURL +"/product/create.php";
		String body = """
				{
					"name": "Water Bottle",
					"description": "Blue water bottle, holds 64 ounces",
					"price": 12,
					"category_id": 3
				}
				""";
		
		 var response = given().body(body).when().post(endpoint).then();
		 response.log().body();
	}
	
	@Test
	public void updateProduct() { //put
		String endpoint = baseURL +"/product/update.php";
		String body = """
				{
					"id": 19,
					"name": "Water Bottle",
					"description": "Blue water bottle, holds 64 ounces",
					"price": 15,
					"category_id": 3
				}
				""";
		var response = given().body(body).when().put(endpoint).then();
		response.log().body();
	}
	
	@Test
	public void deleteProduct() { //delete
		String endpoint = baseURL +"/product/delete.php";
		String body = """
				{
					"id": 19
				}
				""";
		var response = given().body(body).when().delete(endpoint).then();
		response.log().body();
	}
	
	@Test
	public void createSerializedProduct() { // Need jackson-databind to get this work
		String endpoint = baseURL +"/product/create.php";
		Product product = new Product(
				"Water Bottle",
				"Blue water bottle, holds 64 ounces",
				15,
				3
		);
		var response = given().body(product).when().delete(endpoint).then();
		response.log().body();
	}
	
	@Test
	public void getDeserializedProduct() { // Deserialize response body 
		String endpoint = baseURL +"/product/read_one.php";
		Product expectedProduct = new Product(
				2,
				"Cross-Back Training Tank",
				"The most awesome phone of 2013!",
				299.00,
				2,
				"Active Wear - Women"
		);
		
		Product actualProduct = given().
			queryParam("id", 2).
		when().
			get(endpoint).
				as(Product.class);
		
		assertThat(actualProduct, samePropertyValuesAs(expectedProduct));
	}
	
	@Test
	public void SweatbandLifeCycle() { // challenge
		
		// create a product
		String endpoint = baseURL +"/product/create.php";
		String body = """
				{
				"name": "Sweatband",
				"description": "Red Sweatband size: XL",
				"price": 5,
				"category_id": 3
				}
				""";
		var response = given().body(body).when().post(endpoint).then();
		response.log().body();
		
		//update the price
		endpoint = baseURL +"/product/update.php";
		body = """
				{
				"id": 26,
				"name": "Sweatband",
				"description": "Red Sweatband size: XL",
				"price": 6,
				"category_id": 3
				}
				""";
		response = given().body(body).when().put(endpoint).then();
		response.log().body();
		
		//retrieve info
		endpoint = baseURL +"/product/read_one.php";
		response = given().queryParam("id", 26).when().get(endpoint).then();
		response.log().body();
		
		// delete the product
		endpoint = baseURL +"/product/delete.php";
		body = """
				{"id":	26}
				""";
		response = given().body(body).when().delete(endpoint).then();
		response.log().body();
	}
	
	@Test
	public void VerifyAPIResponse() {
		String endpoint = baseURL +"/product/read_one.php";
		
		// not using deserialization
/*		
		given().
			queryParam("id", 18).
		when().
			get(endpoint).
		then().
			log().
			body().
			assertThat().
				statusCode(200).
				header("Content-Type", equalTo("application/json")).
				body("id", equalTo("18")).
				body("name", equalTo("Multi-Vitamin (90 capsules)")).
				body("description", equalTo("A daily dose of our Multi-Vitamins fulfills a day’s nutritional needs for over 12 vitamins and minerals.")).
				body("price", equalTo("10.00")).
				body("category_id", equalTo(4)).
				body("category_name", equalTo("Supplements"));
*/		
		// to using deserialization as well as check http header
		Product expectedProduct = new Product(
				18,
				"Multi-Vitamin (90 capsules)",
				"A daily dose of our Multi-Vitamins fulfills a day’s nutritional needs for over 12 vitamins and minerals.",
				10.00,
				4,
				"Supplements"
		);
		Response response = given().queryParam("id", 18).when().get(endpoint);
		response.then().assertThat().statusCode(200).header("Content-Type", equalTo("application/json"));
		
		Product actualProduct = response.as(Product.class);
		assertThat(expectedProduct, samePropertyValuesAs(actualProduct));
		
	}
}
