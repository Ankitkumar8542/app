package com.musicPlayer.app.song.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import com.musicPlayer.app.song.dto.SongDtos;
import com.musicPlayer.app.song.entity.Song;

import java.util.ArrayList;
import java.util.List;

public class SongSpecification {

    public static Specification<Song> build(SongDtos.SongSearchRequest req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), Song.SongStatus.ACTIVE));

            if (req.getQuery() != null && !req.getQuery().isBlank()) {
                String like = "%" + req.getQuery().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("artist").get("name")), like)
                ));
            }

            if (req.getArtistId() != null) {
                predicates.add(cb.equal(root.get("artist").get("id"), req.getArtistId()));
            }

            if (req.getAlbumId() != null) {
                predicates.add(cb.equal(root.get("album").get("id"), req.getAlbumId()));
            }

            if (req.getLanguage() != null && !req.getLanguage().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("language")), req.getLanguage().toLowerCase()));
            }

            if (req.getPremium() != null) {
                predicates.add(cb.equal(root.get("premium"), req.getPremium()));
            }

            if (req.getMinDuration() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("durationSeconds"), req.getMinDuration()));
            }

            if (req.getMaxDuration() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("durationSeconds"), req.getMaxDuration()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}