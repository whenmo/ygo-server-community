package com.whenmo.repository.database;

import org.springframework.data.jpa.repository.JpaRepository;

import com.whenmo.model.database.domain.Board;

public interface BoardRepository extends JpaRepository<Board, Integer> {

}