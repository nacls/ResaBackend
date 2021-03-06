package ir.ceit.resa.controllers;


import ir.ceit.resa.model.Board;
import ir.ceit.resa.model.User;
import ir.ceit.resa.payload.request.CreateAnnouncementRequest;
import ir.ceit.resa.payload.response.MessageResponse;
import ir.ceit.resa.services.AnnouncementService;
import ir.ceit.resa.services.BoardService;
import ir.ceit.resa.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private UserService userService;

    @Autowired
    private AnnouncementService announcementService;

    @PostMapping("/add/{boardId}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addAnnouncementToBoard(@PathVariable String boardId,
                                                    @Valid @RequestBody CreateAnnouncementRequest announcementRequest) {
        Board board = boardService.loadBoardByBoardId(boardId);
        User user = userService.getLoggedInUser();
        if (user == null || !boardService.canUserWriterOnBoard(user, boardId)) {
            return ResponseEntity
                    .ok(new MessageResponse("User doesn't have access right!"));
        }
        if (board != null) {
            announcementService.postAnnouncementToBoard(board, user.getUsername(), announcementRequest);
            return ResponseEntity
                    .ok(new MessageResponse("Announcement added successfully!"));

        }
        return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Error: board doesn't exist!"));
    }

    @GetMapping("/get/{boardId}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getBoardAnnouncements(@PathVariable String boardId) {
        if (boardService.loadBoardByBoardId(boardId) != null) {
            return ResponseEntity
                    .ok(announcementService.getBoardAnnouncements(boardId));
        }
        return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Error: board doesn't exist!"));
    }
}
