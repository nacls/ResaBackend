package ir.ceit.resa.controllers;

import ir.ceit.resa.model.Board;
import ir.ceit.resa.model.EMembership;
import ir.ceit.resa.model.User;
import ir.ceit.resa.payload.request.ChangeMembershipRequest;
import ir.ceit.resa.payload.request.CreateBoardRequest;
import ir.ceit.resa.payload.request.EditBoardRequest;
import ir.ceit.resa.payload.request.SearchBoardRequest;
import ir.ceit.resa.payload.response.MessageResponse;
import ir.ceit.resa.services.BoardService;
import ir.ceit.resa.services.MembershipService;
import ir.ceit.resa.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/board")
public class BoardController {

    @Autowired
    MembershipService membershipService;

    @Autowired
    BoardService boardService;

    @Autowired
    UserService userService;

    @PostMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> searchBoards(@Valid @RequestBody SearchBoardRequest searchBoardRequest) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("user doesn't exist"));
        }
        return ResponseEntity.ok(boardService.searchInBoards(searchBoardRequest, user.getUsername()));
    }

    @GetMapping("/joined/{username}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserJoinedBoards(@PathVariable("username") String username) {
        User user = userService.loadUserByUsername(username);
        if (user != null) {
            return ResponseEntity.ok().body(boardService.getUserJoinedBoards(user));
        }
        return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Error: user doesn't exist!"));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createBoard(@Valid @RequestBody CreateBoardRequest createBoardRequest) {
        Board board = boardService.loadBoardByBoardId(createBoardRequest.getBoardId());
        if (board != null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: board id is already taken!"));
        }
        User user = userService.getLoggedInUser();
        if (user != null) {
            return ResponseEntity.ok(boardService.createBoard(createBoardRequest, user));
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
            if (membershipService.createBoardMembership(user, board, EMembership.REGULAR_MEMBER)) {
                return ResponseEntity.ok().body(new MessageResponse("Membership added"));
            } else {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Not allowed!"));
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
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("user doesn't exist"));
        }
    }

    @PutMapping("/edit/{boardId}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> editBoard(@PathVariable String boardId, @Valid @RequestBody EditBoardRequest editBoardRequest) {
        User user = userService.getLoggedInUser();
        if (user != null) {
            if (boardService.canUserEditBoard(user, boardId)) {
                if (boardService.loadBoardByBoardId(boardId) != null) {
                    return ResponseEntity.ok()
                            .body(boardService.editBoard(user.getUsername(), boardId, editBoardRequest));
                } else {
                    return ResponseEntity.badRequest().body(new MessageResponse("Board not found"));
                }
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Not creator, access denied"));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
    }

    @DeleteMapping("/delete/{boardId}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteBoard(@PathVariable String boardId) {
        Board board = boardService.loadBoardByBoardId(boardId);
        if (board == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: board doesn't exist!"));
        }
        User user = userService.getLoggedInUser();
        if (user != null) {
            if (boardService.isLoggedInUserBoardCreator(boardId)) {
                boardService.deleteBoardById(board.getId());
                return ResponseEntity
                        .ok()
                        .body(new MessageResponse("Board deleted successfully!"));
            } else {
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

    @GetMapping("/members/{boardId}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getBoardMembers(@PathVariable String boardId) {
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
        return ResponseEntity.ok(boardService.getBoardMembers(board));
    }

    @PutMapping("/access-control")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> configureUserAccessRight(@Valid @RequestBody ChangeMembershipRequest membershipRequest) {
        if (boardService.changeBoardMembershipStatus(membershipRequest)) {
            return ResponseEntity.ok(new MessageResponse("Membership changed"));
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
