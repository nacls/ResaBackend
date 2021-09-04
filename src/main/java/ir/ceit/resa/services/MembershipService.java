package ir.ceit.resa.services;


import ir.ceit.resa.model.*;
import ir.ceit.resa.repository.BoardMembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class MembershipService {

    @Autowired
    BoardService boardService;

    @Autowired
    UserService userService;

    @Autowired
    BoardMembershipRepository membershipRepository;

    public EMembership findMembershipStatus(String username, String boardId) {
        User user = userService.loadUserByUsername(username);
        Set<BoardMembership> userBoards = user.getBoardMemberships();
        for (BoardMembership next : userBoards) {
            if (next.getBoard().getBoardId().equals(boardId)) {
                return next.getStatus();
            }
        }
        return EMembership.NOT_JOINED;
    }

    public boolean removeUserFromBoard(String username, String boardId) {
        Board board = boardService.loadBoardByBoardId(boardId);
        User user = userService.loadUserByUsername(username);
        Set<BoardMembership> userBoards = user.getBoardMemberships();
        for (BoardMembership next : userBoards) {
            if (next.getBoard().getBoardId().equals(boardId) && next.getStatus() != EMembership.CREATOR) {
                Optional<BoardMembership> boardMembership = membershipRepository.findById(next.getPrimaryKey());
                if (boardMembership.isPresent()) {
                    user.getBoardMemberships().remove(boardMembership.get());
                    board.getBoardMemberships().remove(boardMembership.get());
                    membershipRepository.delete(boardMembership.get());
                    return true;
                }
            }
        }
        return false;
    }
}

