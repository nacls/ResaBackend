package ir.ceit.resa.services;


import ir.ceit.resa.model.*;
import ir.ceit.resa.payload.request.ChangeMembershipRequest;
import ir.ceit.resa.repository.BoardMembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class MembershipService {

    @Autowired
    BoardMembershipRepository membershipRepository;

    @Autowired
    BoardService boardService;

    @Autowired
    UserService userService;

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

    public boolean changeMembershipStatus(ChangeMembershipRequest membershipRequest) {
        User user = userService.loadUserByUsername(membershipRequest.getUsername());
        if (membershipRequest.getMembership() == EMembership.NOT_JOINED) {
            return removeUserFromBoard(user, membershipRequest.getBoardId());
        }

        if (membershipRequest.getMembership() == EMembership.CREATOR) {
            return false;
        }

        Set<BoardMembership> userBoards = user.getBoardMemberships();
        for (BoardMembership next : userBoards) {
            if (next.getBoard().getBoardId().equals(membershipRequest.getBoardId())) {
                if (next.getStatus() == EMembership.CREATOR) {
                    return false;
                } else {
                    next.setStatus(membershipRequest.getMembership());
                    membershipRepository.save(next);
                    return true;
                }
            }
        }
        return createBoardMembership(membershipRequest);
    }

    public boolean createBoardMembership(ChangeMembershipRequest membershipRequest) {
        if (findMembershipStatus(membershipRequest.getUsername(), membershipRequest.getBoardId()) != EMembership.NOT_JOINED) {
            return true;
        }
        User user = userService.loadUserByUsername(membershipRequest.getUsername());
        Board board = boardService.loadBoardByBoardId(membershipRequest.getBoardId());
        if (user == null || board == null) {
            return false;
        }
        BoardMembership newBoardMembership = new BoardMembership();
        newBoardMembership.setUser(user);
        newBoardMembership.setBoard(board);
        newBoardMembership.setStatus(membershipRequest.getMembership());
        membershipRepository.save(newBoardMembership);
        return true;
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
}

