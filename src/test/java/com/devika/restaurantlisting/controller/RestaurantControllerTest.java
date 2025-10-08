package com.devika.restaurantlisting.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.devika.restaurantlisting.controller.RestaurantController;
import com.devika.restaurantlisting.dto.RestaurantDto;
import com.devika.restaurantlisting.service.RestaurantService;

public class RestaurantControllerTest {

	@InjectMocks
	RestaurantController restaurantController;

	@Mock
	RestaurantService restaurantService;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this); // in order for Mock and InjectMocks annotations to take effect, you need to
											// call MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testFetchAllRestaurants() {
		// Mock the service behavior
		List<RestaurantDto> mockRestaurants = Arrays.asList(
				new RestaurantDto(1, "Restaurant 1", "Address 1", "city 1", "Desc 1"),
				new RestaurantDto(2, "Restaurant 2", "Address 2", "city 2", "Desc 2"));
		when(restaurantService.getAllRestaurants()).thenReturn(mockRestaurants);

		// Call the controller method
		ResponseEntity<List<RestaurantDto>> response = restaurantController.getAllRestaurants();

		// Verify the response
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(mockRestaurants, response.getBody());

		// Verify that the service method was called
		verify(restaurantService, times(1)).getAllRestaurants();
	}

	@Test
	public void testSaveRestaurant() {
		// Create a mock restaurant to be saved
		RestaurantDto mockRestaurant = new RestaurantDto(1, "Restaurant 1", "Address 1", "city 1", "Desc 1");

		// Mock the service behavior
		when(restaurantService.addRestaurant(mockRestaurant)).thenReturn(mockRestaurant);

		// Call the controller method
		ResponseEntity<RestaurantDto> response = restaurantController.addRestaurant(mockRestaurant);

		// Verify the response
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(mockRestaurant, response.getBody());

		// Verify that the service method was called
		verify(restaurantService, times(1)).addRestaurant(mockRestaurant);
	}

	@Test
	public void testFindRestaurantById() {
		// Create a mock restaurant ID
		Integer mockRestaurantId = 1;

		// Create a mock restaurant to be returned by the service
		RestaurantDto mockRestaurant = new RestaurantDto(1, "Restaurant 1", "Address 1", "city 1", "Desc 1");

		// Mock the service behavior
		when(restaurantService.fetchRestaurant(mockRestaurantId))
				.thenReturn(new ResponseEntity<>(mockRestaurant, HttpStatus.OK));

		// Call the controller method
		ResponseEntity<RestaurantDto> response = restaurantController.fetchRestaurant(mockRestaurantId);

		// Verify the response
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(mockRestaurant, response.getBody());

		// Verify that the service method was called
		verify(restaurantService, times(1)).fetchRestaurant(mockRestaurantId);
	}
}
