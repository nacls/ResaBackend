package ir.ceit.resa.services;

import ir.ceit.resa.model.Board;
import ir.ceit.resa.payload.request.EditBoardRequest;
import ir.ceit.resa.payload.response.BoardInfoResponse;
import ir.ceit.resa.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MembershipService membershipService;

    public Board loadBoardByBoardId(String boardId) {
        if (boardRepository.existsByBoardId(boardId)) {
            Optional<Board> board = boardRepository.findByBoardId(boardId);
            if (board.isPresent()) {
                return board.get();
            }
        }
        return null;
    }

    public String getBoardCreatorByBoardId(String boardId) {
        Board board = loadBoardByBoardId(boardId);
        if (board != null) {
            return board.getCreatorUsername();
        } else
            return "";
    }

    public BoardInfoResponse editBoard(String username, String boardId, EditBoardRequest editBoardRequest) {
        Board board = loadBoardByBoardId(boardId);
        board.setDescription(editBoardRequest.getDescription());
        board.setFaculty(editBoardRequest.getFaculty());
        board.setCategory(editBoardRequest.getCategory());
        boardRepository.save(board);
        return getBoardInfoResponse(username, board);
    }

    public BoardInfoResponse getBoardInfoResponse(String username, Board board) {
        return new BoardInfoResponse(board.getId(),
                board.getBoardId(),
                board.getDescription(),
                board.getCategory(),
                board.getCreatorUsername(),
                board.getFaculty(),
                membershipService.findMembershipStatus(username, board.getBoardId()));
    }
}
