package ir.ceit.resa.controllers;

import ir.ceit.resa.model.*;
import ir.ceit.resa.payload.request.CreateBoardRequest;
import ir.ceit.resa.payload.request.EditBoardRequest;
import ir.ceit.resa.payload.request.SearchBoardRequest;
import ir.ceit.resa.payload.response.BoardInfoResponse;
import ir.ceit.resa.payload.response.MessageResponse;
import ir.ceit.resa.repository.BoardMembershipRepository;
import ir.ceit.resa.repository.BoardRepository;
import ir.ceit.resa.repository.UserRepository;
import ir.ceit.resa.services.BoardService;
import ir.ceit.resa.services.MembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/board")
public class BoardController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    BoardMembershipRepository membershipRepository;

    @Autowired
    MembershipService membershipService;

    @Autowired
    BoardService boardService;

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> searchBoards(@Valid @RequestBody SearchBoardRequest searchBoardRequest) {
        List<Board> searchResultBoards = boardRepository.findByBoardIdContaining(searchBoardRequest.getBoardId());
        String loggedInUser = "";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            loggedInUser = ((UserDetails) principal).getUsername();
        }

        if (searchResultBoards.size() == 0) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("no boards found"));
        } else {
            List<BoardInfoResponse> infoBoards = new ArrayList<>();
            for (Board searchResultBoard : searchResultBoards) {
                String boardId = searchResultBoard.getBoardId();
                BoardInfoResponse temp = new BoardInfoResponse(
                        searchResultBoard.getId(),
                        boardId,
                        searchResultBoard.getDescription(),
                        searchResultBoard.getCategory(),
                        searchResultBoard.getCreatorUsername(),
                        searchResultBoard.getFaculty(),
                        membershipService.findMembershipStatus(loggedInUser, boardId)
                );
                infoBoards.add(temp);
            }
            return ResponseEntity.ok(infoBoards);
        }
    }

    @GetMapping("/joined/{username}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserJoinedBoards(@PathVariable("username") String username) {

        if (userRepository.existsByUsername(username)) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {

                Set<BoardInfoResponse> boardInfoResponses = new HashSet<>();

                Set<BoardMembership> boardMemberships = user.get().getBoardMemberships();

                for (BoardMembership next : boardMemberships) {
                    BoardInfoResponse temp = new BoardInfoResponse(next.getBoard().getId(),
                            next.getBoard().getBoardId(),
                            next.getBoard().getDescription(),
                            next.getBoard().getCategory(),
                            next.getBoard().getCreatorUsername(),
                            next.getBoard().getFaculty(),
                            next.getStatus());
                    boardInfoResponses.add(temp);
                }
                return ResponseEntity.ok().body(boardInfoResponses);

            } else {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: user doesn't exist!"));
            }
        }
        return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Error: user doesn't exist!"));
    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createBoard(@Valid @RequestBody CreateBoardRequest createBoardRequest) {

        if (boardRepository.existsByBoardId(createBoardRequest.getBoardId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: board id is already taken!"));
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String loggedInUser = ((UserDetails) principal).getUsername();
            Board board = new Board(createBoardRequest.getBoardId(),
                    createBoardRequest.getDescription(),
                    createBoardRequest.getCategory(),
                    loggedInUser,
                    createBoardRequest.getFaculty());
            // TODO: add board membership
            boardRepository.save(board);

            return ResponseEntity.ok(new MessageResponse("Board added successfully!"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Couldn't find the user.");
        }
    }

    @GetMapping("/join/{username}/{boardId}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addUserToBoard(@PathVariable("username") String username,
                                            @PathVariable String boardId) {

        Optional<User> user = userRepository.findByUsername(username);
        Optional<Board> board = boardRepository.findByBoardId(boardId);
        if (user.isPresent() && board.isPresent()) {
            BoardMembershipId membershipId = new BoardMembershipId();
            membershipId.setUser(user.get());
            membershipId.setBoard(board.get());
            Optional<BoardMembership> boardMembership = membershipRepository.findById(membershipId);
            if (boardMembership.isPresent()) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: membership exists: " + boardMembership.get().getStatus()));
            } else {
                BoardMembership newBoardMembership = new BoardMembership();
                newBoardMembership.setUser(user.get());
                newBoardMembership.setBoard(board.get());
                newBoardMembership.setStatus(EMembership.REGULAR_MEMBER);
                membershipRepository.save(newBoardMembership);
                return ResponseEntity.ok().body("membership added");
            }
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: user doesn't exist!"));
        }
    }


    @PutMapping("/leave/{boardId}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> userLeaveBoard(@PathVariable String boardId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String loggedInUser = ((UserDetails) principal).getUsername();
            boolean ok = membershipService.removeUserFromBoard(loggedInUser, boardId);
            if (ok)
                return ResponseEntity.ok(new MessageResponse("user left board!"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("something went wrong"));
    }


    @DeleteMapping("/delete/{boardId}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteBoard(@PathVariable String boardId) {
        Optional<Board> board = boardRepository.findByBoardId(boardId);
        if (!board.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: board doesn't exist!"));

        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String loggedInUser = ((UserDetails) principal).getUsername();
            Optional<User> user = userRepository.findByUsername(loggedInUser);
            if (user.isPresent()) {
                String creator = board.get().getCreatorUsername();
                if (!loggedInUser.equals(creator) && !user.get().getRoles().contains(ERole.ROLE_ADMIN)) {
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Error: you don't have the access rights to delete this board!"));
                }
            } else {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: user doesn't exist!"));
            }

        }


        boardRepository.deleteById(board.get().getId());
        return ResponseEntity
                .ok()
                .body(new MessageResponse("Board deleted successfully!"));
    }


    @PutMapping("/edit/{boardId}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> editBoard(@PathVariable String boardId, @Valid @RequestBody EditBoardRequest editBoardRequest) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String loggedInUser = ((UserDetails) principal).getUsername();
            if (loggedInUser.equals(boardService.getBoardCreatorByBoardId(boardId))) {
                if (boardService.loadBoardByBoardId(boardId) != null) {
                    return ResponseEntity.ok().body(boardService.editBoard(loggedInUser, boardId, editBoardRequest));
                }
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Not creator, access denied"));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
    }


    @GetMapping("/writer/{boardId}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> configureWriterAccessRight(@PathVariable String boardId) {
        return null;
    }

    @GetMapping("/get/{boardId}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getBoardInfo(@PathVariable String boardId) {

        EMembership membership = null;
        Optional<Board> board = boardRepository.findByBoardId(boardId);

        if (!board.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: board doesn't exist!"));

        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String loggedInUser = ((UserDetails) principal).getUsername();
            Optional<User> user = userRepository.findByUsername(loggedInUser);
            if (user.isPresent()) {
                BoardMembershipId membershipId = new BoardMembershipId();
                membershipId.setUser(user.get());
                membershipId.setBoard(board.get());
                Optional<BoardMembership> boardMembership = membershipRepository.findById(membershipId);
                if (boardMembership.isPresent()) {
                    membership = boardMembership.get().getStatus();

                } else {
                    membership = EMembership.NOT_JOINED;
                }
            }
        }


        Board existingBoard = board.get();
        BoardInfoResponse boardInfoResponse = new BoardInfoResponse(
                existingBoard.getId(),
                existingBoard.getBoardId(),
                existingBoard.getDescription(),
                existingBoard.getCategory(),
                existingBoard.getCreatorUsername(),
                existingBoard.getFaculty(),
                membership
        );
        return ResponseEntity.ok(boardInfoResponse);
    }
}
