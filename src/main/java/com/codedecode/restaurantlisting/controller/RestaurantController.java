package com.codedecode.restaurantlisting.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codedecode.restaurantlisting.dto.RestaurantDto;
import com.codedecode.restaurantlisting.service.RestaurantService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")

@RequestMapping("/restaurant")
public class RestaurantController {

	@Autowired
	RestaurantService restaurantService;

	@GetMapping("/fetchAllRestaurants")
	public ResponseEntity<List<RestaurantDto>> getAllRestaurants() {
		List<RestaurantDto> restaurants = restaurantService.getAllRestaurants();
		return new ResponseEntity<>(restaurants, HttpStatus.OK);

	}

	@PostMapping("/addRestaurant")
	public ResponseEntity<RestaurantDto> addRestaurant(@RequestBody RestaurantDto restaurantDto) {
		RestaurantDto restaurantSaved = restaurantService.addRestaurant(restaurantDto);
		return new ResponseEntity<>(restaurantSaved, HttpStatus.CREATED);
	}

	@GetMapping("/fetchRestaurant/{restaurantId}")
	public ResponseEntity<RestaurantDto> fetchRestaurant(@PathVariable Integer restaurantId) {
		return restaurantService.fetchRestaurant(restaurantId);
	}

}
