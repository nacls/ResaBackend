package ir.ceit.resa.controllers;

import ir.ceit.resa.model.*;
import ir.ceit.resa.payload.request.AddUserRequest;
import ir.ceit.resa.payload.request.CreateBoardRequest;
import ir.ceit.resa.payload.response.BoardInfoResponse;
import ir.ceit.resa.payload.response.MessageResponse;
import ir.ceit.resa.repository.BoardMembershipRepository;
import ir.ceit.resa.repository.BoardRepository;
import ir.ceit.resa.repository.RoleRepository;
import ir.ceit.resa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/board")
public class BoardController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    BoardMembershipRepository membershipRepository;


    @GetMapping("/search/{boardString}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> searchBoards(@PathVariable("boardString") String boardString, @Valid @RequestBody AddUserRequest signUpRequest) {
        return null;
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


    @GetMapping("/leave/{boardId}/{username}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> userLeaveBoard(@PathVariable String boardId) {
        return null;
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


    @GetMapping("/edit/{boardId}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> editBoard(@PathVariable String boardId) {
        return null;
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
