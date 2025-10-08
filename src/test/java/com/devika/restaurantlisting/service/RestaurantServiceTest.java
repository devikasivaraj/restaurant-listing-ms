package com.devika.restaurantlisting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.devika.restaurantlisting.dto.RestaurantDto;
import com.devika.restaurantlisting.entity.Restaurant;
import com.devika.restaurantlisting.repository.RestaurantRepo;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

    @Mock
    private RestaurantRepo restaurantRepostiroy; // Note the spelling matches the service class

    @InjectMocks
    private RestaurantService restaurantService;

    // Sample data for consistent testing
    private Restaurant restaurantEntity;
    private RestaurantDto restaurantDto;

    @BeforeEach
    public void setup() {
        // Initialize objects before each test
        restaurantEntity = new Restaurant(1, "The Great Burger Co.", "123 Main St", "Cityville", "Gourmet burgers");
        restaurantDto = new RestaurantDto(1, "The Great Burger Co.", "123 Main St", "Cityville", "Gourmet burgers");
    }

    @Test
    void testFindAllRestaurants() {
        // Arrange
        Restaurant restaurant2 = new Restaurant(2, "Pizza Palace", "456 Oak Ave", "Cityville", "Italian pizza");
        List<Restaurant> mockList = Arrays.asList(restaurantEntity, restaurant2);

        when(restaurantRepostiroy.findAll()).thenReturn(mockList);

        // Act
        List<RestaurantDto> result = restaurantService.getAllRestaurants();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("The Great Burger Co.", result.get(0).getName());
        assertEquals("Pizza Palace", result.get(1).getName());

        // Verify that the repository method was called exactly once
        verify(restaurantRepostiroy, times(1)).findAll();
    }

    @Test
    void testAddRestaurant() {
        // Arrange
        // The service logic converts DTO -> Entity, saves, then converts back to DTO.
        when(restaurantRepostiroy.save(any(Restaurant.class))).thenReturn(restaurantEntity);

        // Act
        RestaurantDto resultDto = restaurantService.addRestaurant(restaurantDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals(restaurantDto.getName(), resultDto.getName());
        assertEquals(restaurantDto.getId(), resultDto.getId()); // ID should be preserved/returned
        
        // Verify that the save method was called exactly once
        verify(restaurantRepostiroy, times(1)).save(any(Restaurant.class));
    }

    @Test
    void testFetchRestaurantById_ExistingId() {
        // Arrange
        when(restaurantRepostiroy.findById(1)).thenReturn(Optional.of(restaurantEntity));

        // Act
        ResponseEntity<RestaurantDto> response = restaurantService.fetchRestaurant(1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(restaurantDto.getName(), response.getBody().getName());
        
        verify(restaurantRepostiroy, times(1)).findById(1);
    }

    @Test
    void testFetchRestaurantById_NonExistingId() {
        // Arrange
        when(restaurantRepostiroy.findById(99)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<RestaurantDto> response = restaurantService.fetchRestaurant(99);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(restaurantRepostiroy, times(1)).findById(99);
    }
}
