package ir.ceit.resa.repository;


import ir.ceit.resa.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Optional<Board> findByBoardId(String boardId);

    Boolean existsByBoardId(String boardId);

    List<Board> findByBoardIdContaining(String boardId);
}
