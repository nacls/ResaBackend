package ir.ceit.resa.services;

import ir.ceit.resa.model.Announcement;
import ir.ceit.resa.model.Board;
import ir.ceit.resa.payload.request.CreateAnnouncementRequest;
import ir.ceit.resa.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AnnouncementService {

    @Autowired
    AnnouncementRepository announcementRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private UserService userService;

    public Announcement getBoardLatestAnnouncement(Board board) {
        List<Announcement> announcementList = new ArrayList<>(board.getAnnouncements());
        if (announcementList.size() == 0) {
            return null;
        }
        Collections.sort(announcementList);
        return announcementList.get(0);
    }

    public List<Announcement> getBoardAnnouncements(String boardId) {
        Board board = boardService.loadBoardByBoardId(boardId);
        List<Announcement> announcements = announcementRepository.findByBoard(board);
        Collections.sort(announcements);
        return announcements;
    }

    public boolean postAnnouncementToBoard(Board board, CreateAnnouncementRequest announcementRequest) {
        // check if logged in user can write in this board
        String loggedInUser = userService.getLoggedInUser().getUsername();
        if (loggedInUser == null)
            return false;
        Announcement announcement = new Announcement(announcementRequest.getCreationDate(),
                announcementRequest.getMessage(),
                loggedInUser,
                board);

        announcementRepository.save(announcement);
        return true;
    }

}



