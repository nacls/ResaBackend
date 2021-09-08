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
    private BoardMembershipRepository membershipRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private UserService userService;

    EMembership findMembershipStatus(String username, String boardId) {
        User user = userService.loadUserByUsername(username);
        Set<BoardMembership> userBoards = user.getBoardMemberships();
        for (BoardMembership next : userBoards) {
            if (next.getBoard().getBoardId().equals(boardId)) {
                return next.getStatus();
            }
        }
        return EMembership.NOT_JOINED;
    }

    boolean changeMembershipStatus(User user, Board board, EMembership membership) {
        if (membership == EMembership.NOT_JOINED) {
            return removeUserFromBoard(user, board.getBoardId());
        }
        if (membership == EMembership.CREATOR) {
            return false;
        }
        Set<BoardMembership> userBoards = user.getBoardMemberships();
        for (BoardMembership next : userBoards) {
            if (next.getBoard().getBoardId().equals(board.getBoardId())) {
                if (next.getStatus() == EMembership.CREATOR) {
                    return false;
                } else {
                    next.setStatus(membership);
                    membershipRepository.save(next);
                    return true;
                }
            }
        }
        return createBoardMembership(user, board, membership);
    }

    public boolean createBoardMembership(User user, Board board, EMembership membership) {
        if (findMembershipStatus(user.getUsername(), board.getBoardId())
                != EMembership.NOT_JOINED) {
            return true;
        }
        createMembership(user, board, membership);
        return true;
    }

    private void createMembership(User user, Board board, EMembership membership) {
        BoardMembership newBoardMembership = new BoardMembership();
        newBoardMembership.setUser(user);
        newBoardMembership.setBoard(board);
        newBoardMembership.setStatus(membership);
        membershipRepository.save(newBoardMembership);
    }

    public boolean removeUserFromBoard(User user, String boardId) {
        Board board = boardService.loadBoardByBoardId(boardId);
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

    public boolean doesMembershipExist(User user, Board board) {
        BoardMembershipId membershipId = new BoardMembershipId();
        membershipId.setUser(user);
        membershipId.setBoard(board);
        Optional<BoardMembership> boardMembership = membershipRepository.findById(membershipId);
        return boardMembership.isPresent();
    }
}

