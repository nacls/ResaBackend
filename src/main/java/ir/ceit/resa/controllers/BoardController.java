package ir.ceit.resa.controllers;

import ir.ceit.resa.model.*;
import ir.ceit.resa.payload.request.AddUserRequest;
import ir.ceit.resa.payload.request.CreateBoardRequest;
import ir.ceit.resa.payload.response.BoardInfoResponse;
import ir.ceit.resa.payload.response.MessageResponse;
import ir.ceit.resa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

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


    @GetMapping("/search/{boardString}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> searchBoards(@PathVariable("boardString") String boardString, @Valid @RequestBody AddUserRequest signUpRequest) {
        return null;
    }

    @GetMapping("/joined/{username}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserJoinedBoards(@PathVariable("username") String username) {
        return null;
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
        return null;
    }


    @GetMapping("/delete/{boardId}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteBoard(@PathVariable String boardId) {
        return null;
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
        if (boardRepository.existsByBoardId(boardId)) {
            Optional<Board> board = boardRepository.findByBoardId(boardId);
            if (board.isPresent()) {
                Board existingBoard = board.get();
                BoardInfoResponse boardInfoResponse = new BoardInfoResponse(
                        existingBoard.getId(),
                        existingBoard.getBoardId(),
                        existingBoard.getDescription(),
                        existingBoard.getCategory(),
                        existingBoard.getCreatorUsername(),
                        existingBoard.getFaculty()
                );
                return ResponseEntity.ok(boardInfoResponse);

            } else {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: board doesn't exist!"));
            }
        }
        return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Error: board doesn't exist!"));
    }
}
