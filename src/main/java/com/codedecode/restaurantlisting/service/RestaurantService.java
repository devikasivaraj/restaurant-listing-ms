package com.codedecode.restaurantlisting.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.codedecode.restaurantlisting.dto.RestaurantDto;
import com.codedecode.restaurantlisting.entity.Restaurant;
import com.codedecode.restaurantlisting.mapper.RestaurantMapper;
import com.codedecode.restaurantlisting.repository.RestaurantRepo;

@Service
public class RestaurantService {

	@Autowired
	RestaurantRepo restaurantRepostiroy;

	public List<RestaurantDto> getAllRestaurants() {
		List<Restaurant> restaurantList = restaurantRepostiroy.findAll();
		List<RestaurantDto> restaurantDtoList = restaurantList.stream()
				.map(restaurant -> RestaurantMapper.INSTANCE.mapRestaurantToRestaurantDto(restaurant))
				.collect(Collectors.toList());
		return restaurantDtoList;
	}

	public RestaurantDto addRestaurant(RestaurantDto restaurantDto) {

		Restaurant restaurantSaved = restaurantRepostiroy
				.save(RestaurantMapper.INSTANCE.mapRestaurantDtoToRestaurant(restaurantDto));
		return RestaurantMapper.INSTANCE.mapRestaurantToRestaurantDto(restaurantSaved);
	}

	public ResponseEntity<RestaurantDto> fetchRestaurant(Integer id) {
		Optional<Restaurant> res = restaurantRepostiroy.findById(id);
		if (res.isPresent()) {
			return new ResponseEntity<>(RestaurantMapper.INSTANCE.mapRestaurantToRestaurantDto(res.get()),
					HttpStatus.OK);
		}
		return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	}

}
