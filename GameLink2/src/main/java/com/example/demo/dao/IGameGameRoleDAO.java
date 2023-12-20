package com.example.demo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.dto.GameGameRole;

public interface IGameGameRoleDAO extends JpaRepository<GameGameRole, Integer>{

	Optional<GameGameRole> findByIdGameRoleId(int idGameRole);
	Optional<GameGameRole> findByIdGameId(int idGame);
	Optional<GameGameRole> findByGameIdAndGameRoleId(int idGame, int idGameRole);
}
