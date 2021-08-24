package ir.ceit.resa.repository;


import ir.ceit.resa.model.Announcement;
import ir.ceit.resa.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByBoard(Board board);
}
