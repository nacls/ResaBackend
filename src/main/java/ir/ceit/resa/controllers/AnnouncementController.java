package ir.ceit.resa.controllers;


import ir.ceit.resa.model.Announcement;
import ir.ceit.resa.model.Board;
import ir.ceit.resa.payload.request.CreateAnnouncementRequest;
import ir.ceit.resa.payload.response.MessageResponse;
import ir.ceit.resa.repository.AnnouncementRepository;
import ir.ceit.resa.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    AnnouncementRepository announcementRepository;

    @PostMapping("/add/{boardId}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addAnnouncementToBoard(@PathVariable String boardId,
                                                    @Valid @RequestBody CreateAnnouncementRequest announcementRequest) {
        if (boardRepository.existsByBoardId(boardId)) {
            Optional<Board> board = boardRepository.findByBoardId(boardId);
            if (board.isPresent()) {
                Board existingBoard = board.get();
                Announcement announcement = new Announcement(announcementRequest.getCreationDate(),
                        announcementRequest.getMessage(),
                        announcementRequest.getWriter(),
                        existingBoard);

                announcementRepository.save(announcement);

                return ResponseEntity.ok(new MessageResponse("Announcement added successfully!"));

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

    @GetMapping("/get/{boardId}")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getBoardAnnouncements(@PathVariable String boardId) {
        if (boardRepository.existsByBoardId(boardId)) {
            Optional<Board> board = boardRepository.findByBoardId(boardId);
            if (board.isPresent()) {
                Board existingBoard = board.get();
                List<Announcement> announcements = announcementRepository.findByBoard(existingBoard);

                return ResponseEntity.ok(announcements);

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
