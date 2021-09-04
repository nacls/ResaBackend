package ir.ceit.resa.services;

import ir.ceit.resa.model.Board;
import ir.ceit.resa.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    public Board loadBoardByBoardId(String boardId) {
        if (boardRepository.existsByBoardId(boardId)) {
            Optional<Board> board = boardRepository.findByBoardId(boardId);
            if (board.isPresent()) {
                return board.get();
            }
        }
        return null;
    }
}
