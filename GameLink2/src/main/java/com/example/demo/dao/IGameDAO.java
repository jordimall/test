/**
 * 
 */
package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.dto.Game;

public interface IGameDAO extends JpaRepository<Game, Integer> {

}
