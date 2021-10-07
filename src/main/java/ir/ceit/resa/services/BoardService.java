package ir.ceit.resa.services;

import ir.ceit.resa.model.Board;
import ir.ceit.resa.model.BoardMembership;
import ir.ceit.resa.model.EMembership;
import ir.ceit.resa.model.User;
import ir.ceit.resa.payload.request.*;
import ir.ceit.resa.payload.response.BoardInfoResponse;
import ir.ceit.resa.payload.response.BoardMemberResponse;
import ir.ceit.resa.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private UserService userService;

    @Autowired
    private AnnouncementService announcementService;

    public Board loadBoardByBoardId(String boardId) {
        if (boardRepository.existsByBoardId(boardId)) {
            Optional<Board> board = boardRepository.findByBoardId(boardId);
            if (board.isPresent()) {
                return board.get();
            }
        }
        return null;
    }

    private String getBoardCreatorByBoardId(String boardId) {
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
                getBoardCreatorFullName(board),
                board.getFaculty(),
                membershipService.findMembershipStatus(username, board.getBoardId()),
                announcementService.getBoardLatestAnnouncement(board));
    }

    public List<BoardMemberResponse> getBoardMembers(Board board) {
        List<BoardMemberResponse> boardMembers = new ArrayList<>();
        Set<BoardMembership> memberships = board.getBoardMemberships();

        for (BoardMembership next : memberships) {
            BoardMemberResponse temp = new BoardMemberResponse(
                    next.getUser().getUsername(),
                    next.getUser().getFullName(),
                    next.getStatus());
            boardMembers.add(temp);
        }
        boardMembers.sort(Collections.reverseOrder());
        return boardMembers;

    }

    private BoardInfoResponse getBoardInfoResponse(String username, Board board, EMembership membership) {
        return new BoardInfoResponse(board.getId(),
                board.getBoardId(),
                board.getDescription(),
                board.getCategory(),
                getBoardCreatorFullName(board),
                board.getFaculty(),
                membership,
                announcementService.getBoardLatestAnnouncement(board));
    }

    public boolean changeBoardMembershipStatus(ChangeMembershipRequest changeMembershipRequest) {
        if (isLoggedInUserBoardCreator(changeMembershipRequest.getBoardId())) {
            User user = userService.loadUserByUsername(changeMembershipRequest.getUsername());
            Board board = loadBoardByBoardId(changeMembershipRequest.getBoardId());
            EMembership membership = changeMembershipRequest.getMembership();
            if (user == null || board == null)
                return false;
            return membershipService.changeMembershipStatus(user, board, membership);
        }
        return false;
    }

    public boolean isLoggedInUserBoardCreator(String boardId) {
        String boardCreatorUsername = getBoardCreatorByBoardId(boardId);
        String loggedInUser = userService.getLoggedInUser().getUsername();
        return boardCreatorUsername.equalsIgnoreCase(loggedInUser);
    }

    public List<BoardInfoResponse> searchInBoards(SearchBoardRequest searchBoardRequest, String username) {
        String searchKeyword = searchBoardRequest.getBoardId();
        List<Board> searchResultBoards = boardRepository.findByBoardIdOrBoardTitleContaining(searchKeyword);
        List<BoardInfoResponse> infoBoards = new ArrayList<>();
        for (Board searchResultBoard : searchResultBoards) {
            BoardInfoResponse temp = getBoardInfoResponse(username, searchResultBoard);
            infoBoards.add(temp);
        }
        Collections.sort(infoBoards);
        return infoBoards;
    }

    public List<BoardInfoResponse> getUserJoinedBoards(User user) {
        List<BoardInfoResponse> boardInfoResponses = new ArrayList<>();
        Set<BoardMembership> boardMemberships = user.getBoardMemberships();
        for (BoardMembership next : boardMemberships) {
            BoardInfoResponse temp = getBoardInfoResponse(user.getUsername(), next.getBoard());
            boardInfoResponses.add(temp);
        }
        Collections.sort(boardInfoResponses);
        return boardInfoResponses;
    }

    public BoardInfoResponse createBoard(CreateBoardRequest createBoardRequest, User user) {
        Board board = new Board(createBoardRequest.getBoardId(),
                createBoardRequest.getDescription(),
                createBoardRequest.getCategory(),
                user.getUsername(),
                createBoardRequest.getFaculty());
        boardRepository.save(board);
        Board existingBoard = loadBoardByBoardId(board.getBoardId());
        membershipService.createBoardMembership(user, existingBoard, EMembership.CREATOR);
        announcementService.postAnnouncementToBoard(existingBoard,
                user.getUsername(),
                getBoardFirstAnnouncement(createBoardRequest.getCreationDate(),
                        user,
                        existingBoard.getBoardId()));
        return getBoardInfoResponse(user.getUsername(), existingBoard, EMembership.CREATOR);
    }

    private CreateAnnouncementRequest getBoardFirstAnnouncement(Date creationDate, User user, String boardId) {
        CreateAnnouncementRequest announcementRequest = new CreateAnnouncementRequest();
        announcementRequest.setCreationDate(creationDate);
        String boardCreation = "برد " + boardId + " توسط " + user.getFullName() + " ایجاد شد.";
        announcementRequest.setMessage(boardCreation);
        return announcementRequest;
    }

    public boolean canUserEditBoard(User user, String boardId) {
        return user.getUsername().equals(getBoardCreatorByBoardId(boardId));
    }

    public boolean canUserWriterOnBoard(User user, String boardId) {
        EMembership membership = membershipService.findMembershipStatus(user.getUsername(), boardId);
        return membership == EMembership.WRITER || membership == EMembership.CREATOR;
    }

    public void deleteBoardById(Long id) {
        boardRepository.deleteById(id);
    }

    private String getBoardCreatorFullName(Board board) {
        User creator = userService.loadUserByUsername(board.getCreatorUsername());
        return creator.getFullName();
    }
}
