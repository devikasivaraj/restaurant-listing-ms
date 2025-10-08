package com.devika.restaurantlisting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devika.restaurantlisting.entity.Restaurant;

@Repository
public interface RestaurantRepo extends JpaRepository<Restaurant, Integer>{

}
