package com.devika.restaurantlisting.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.devika.restaurantlisting.dto.RestaurantDto;
import com.devika.restaurantlisting.entity.Restaurant;
// Removed MapStruct import
import com.devika.restaurantlisting.repository.RestaurantRepo;

@Service
public class RestaurantService {

	@Autowired
	RestaurantRepo restaurantRepostiroy;
	
    /**
     * Converts a Restaurant entity to a RestaurantDto using manual mapping.
     * @param restaurant The entity to convert.
     * @return The resulting DTO.
     */
    private RestaurantDto mapRestaurantToRestaurantDto(Restaurant restaurant) {
        RestaurantDto dto = new RestaurantDto();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setCity(restaurant.getCity());
        dto.setRestaurantDescription(restaurant.getRestaurantDescription());
        return dto;
    }

    /**
     * Converts a RestaurantDto to a Restaurant entity using manual mapping.
     * @param dto The DTO to convert.
     * @return The resulting entity.
     */
    private Restaurant mapRestaurantDtoToRestaurant(RestaurantDto dto) {
        // Note: When saving a new entity, the ID from the DTO might be ignored by JPA, 
        // but it's set here for consistency.
        Restaurant restaurant = new Restaurant();
        restaurant.setId(dto.getId()); 
        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setCity(dto.getCity());
        restaurant.setRestaurantDescription(dto.getRestaurantDescription());
        return restaurant;
    }


	public List<RestaurantDto> getAllRestaurants() {
		List<Restaurant> restaurantList = restaurantRepostiroy.findAll();
		
		// Use manual mapping (this::mapRestaurantToRestaurantDto)
		List<RestaurantDto> restaurantDtoList = restaurantList.stream()
				.map(this::mapRestaurantToRestaurantDto)
				.collect(Collectors.toList());
				
		return restaurantDtoList;
	}

	public RestaurantDto addRestaurant(RestaurantDto restaurantDto) {
		// Convert DTO to Entity using manual mapping
		Restaurant restaurantToSave = mapRestaurantDtoToRestaurant(restaurantDto);

		Restaurant restaurantSaved = restaurantRepostiroy.save(restaurantToSave);
		
		// Convert saved Entity back to DTO
		return mapRestaurantToRestaurantDto(restaurantSaved);
	}

	public ResponseEntity<RestaurantDto> fetchRestaurant(Integer id) {
		Optional<Restaurant> res = restaurantRepostiroy.findById(id);
		
		if (res.isPresent()) {
			// Convert Entity to DTO using manual mapping
			return new ResponseEntity<>(mapRestaurantToRestaurantDto(res.get()), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	}

}
