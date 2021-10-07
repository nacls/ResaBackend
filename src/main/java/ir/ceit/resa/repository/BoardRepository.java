package ir.ceit.resa.repository;


import ir.ceit.resa.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Optional<Board> findByBoardId(String boardId);

    Boolean existsByBoardId(String boardId);

    List<Board> findByBoardIdContaining(String boardId);

    @Query(value="select * from boards u where u.board_name like %:keyword% or u.description like %:keyword%", nativeQuery=true)
    List<Board> findByBoardIdOrBoardTitleContaining(@Param("keyword") String keyword);
}
