package ir.ceit.resa.controllers;

import ir.ceit.resa.model.*;
import ir.ceit.resa.payload.request.ChangeMembershipRequest;
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
import ir.ceit.resa.services.UserService;
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

    @Autowired
    UserService userService;

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> searchBoards(@Valid @RequestBody SearchBoardRequest searchBoardRequest) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("user doesn't exist"));
        }

        List<Board> searchResultBoards = boardRepository.findByBoardIdContaining(searchBoardRequest.getBoardId());
        List<BoardInfoResponse> infoBoards = new ArrayList<>();
        for (Board searchResultBoard : searchResultBoards) {
            BoardInfoResponse temp = boardService.getBoardInfoResponse(user.getUsername(), searchResultBoard);
            infoBoards.add(temp);
        }
        Collections.sort(infoBoards);
        return ResponseEntity.ok(infoBoards);

    }

    @GetMapping("/joined/{username}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserJoinedBoards(@PathVariable("username") String username) {
        User user = userService.loadUserByUsername(username);
        if (user != null) {
            List<BoardInfoResponse> boardInfoResponses = new ArrayList<>();
            Set<BoardMembership> boardMemberships = user.getBoardMemberships();
            for (BoardMembership next : boardMemberships) {
                BoardInfoResponse temp = boardService
                        .getBoardInfoResponse(user.getUsername(), next.getBoard());
                boardInfoResponses.add(temp);
            }
            Collections.sort(boardInfoResponses);
            return ResponseEntity.ok().body(boardInfoResponses);
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

    @GetMapping("/join/{boardId}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> userJoinBoard(@PathVariable String boardId) {

        User user = userService.getLoggedInUser();
        Board board = boardService.loadBoardByBoardId(boardId);
        if (user != null && board != null) {
            BoardMembershipId membershipId = new BoardMembershipId();
            membershipId.setUser(user);
            membershipId.setBoard(board);
            Optional<BoardMembership> boardMembership = membershipRepository.findById(membershipId);
            if (boardMembership.isPresent()) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: membership exists: " + boardMembership.get().getStatus()));
            } else {
                BoardMembership newBoardMembership = new BoardMembership();
                newBoardMembership.setUser(user);
                newBoardMembership.setBoard(board);
                newBoardMembership.setStatus(EMembership.REGULAR_MEMBER);
                membershipRepository.save(newBoardMembership);
                return ResponseEntity.ok().body("membership added");
            }
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: user or board doesn't exist!"));
        }
    }


    @PutMapping("/leave/{boardId}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> userLeaveBoard(@PathVariable String boardId) {
        User user = userService.getLoggedInUser();
        if (user != null) {
            if (membershipService.removeUserFromBoard(user, boardId))
                return ResponseEntity.ok(new MessageResponse("user left board!"));
            else
                return ResponseEntity.badRequest().body(new MessageResponse("something went wrong"));
        }else {
            return ResponseEntity.badRequest().body(new MessageResponse("user doesn't exist"));
        }
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


    @PutMapping("/access-control")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> configureUserAccessRight(@Valid @RequestBody ChangeMembershipRequest membershipRequest) {
        boolean status = boardService.changeBoardMembershipStatus(membershipRequest);
        if (status) {
            return ResponseEntity.ok(new MessageResponse("membership changed"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Something went wrong"));
        }
    }

    @GetMapping("/get/{boardId}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getBoardInfo(@PathVariable String boardId) {

        Board board = boardService.loadBoardByBoardId(boardId);
        User user = userService.getLoggedInUser();
        if (board == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: board doesn't exist!"));

        }

        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: user doesn't exist!"));
        }

        return ResponseEntity.ok(boardService.getBoardInfoResponse(user.getUsername(), board));
    }
}
