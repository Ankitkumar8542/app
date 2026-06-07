//package com.musicPlayer.app.user.entity;
//
//import jakarta.persistence.Entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import com.musicPlayer.app.common.util.BaseEntity;
//
//import java.time.LocalDateTime;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//@Entity
//@Table(name = "users")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class User extends BaseEntity implements UserDetails {
//    
//	@Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Long id;
//    @Column(nullable = false)
//    private String name;
//
//    @Column(nullable = false, unique = true)
//    private String email;
//
//    @Column(nullable = false)
//    private String password;
//
//    @Column(name = "profile_image")
//    private String profileImage;
//
//    @Column(name = "profile_image_public_id")
//    private String profileImagePublicId;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    @Builder.Default
//    private Role role = Role.USER;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    @Builder.Default
//    private AccountStatus status = AccountStatus.ACTIVE;
//
//    @Column(name = "email_verified")
//    @Builder.Default
//    private boolean emailVerified = false;
//
//    @Column(name = "date_of_birth")
//    private java.time.LocalDate dateOfBirth;
//
//    private String country;
//
//    @Column(name = "premium_expires_at")
//    private LocalDateTime premiumExpiresAt;
//
//    @Column(name = "monthly_play_count")
//    @Builder.Default
//    private Long monthlyPlayCount = 0L;
//
//    @Column(name = "play_count_reset_date")
//    private LocalDateTime playCountResetDate;
//
//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(
//            name = "user_followed_artists",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "artist_id")
//    )
//    @Builder.Default
//    private Set<com.musicPlayer.app.artist.entity.Artist> followedArtists = new HashSet<>();
//
//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(
//            name = "user_liked_songs",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "song_id")
//    )
//    @Builder.Default
//    private Set<com.musicPlayer.app.song.entity.Song> likedSongs = new HashSet<>();
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return List.of(new SimpleGrantedAuthority(role.name()));
//    }
//
//    @Override
//    public String getUsername() { return email; }
//
//    @Override
//    public boolean isAccountNonExpired() { return true; }
//
//    @Override
//    public boolean isAccountNonLocked() { return status != AccountStatus.BANNED; }
//
//    @Override
//    public boolean isCredentialsNonExpired() { return true; }
//
//    @Override
//    public boolean isEnabled() { return status == AccountStatus.ACTIVE; }
//
//    public boolean isPremium() {
//        return role == Role.PREMIUM || role == Role.ADMIN ||
//               (premiumExpiresAt != null && premiumExpiresAt.isAfter(LocalDateTime.now()));
//    }
//
//    public enum Role {
//        USER, PREMIUM, ADMIN
//    }
//
//    public enum AccountStatus {
//        ACTIVE, SUSPENDED, BANNED
//    }
//    
//    
//}


package com.musicPlayer.app.user.entity;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.musicPlayer.app.common.util.BaseEntity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "profile_image_public_id")
    private String profileImagePublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    private String country;

    @Column(name = "premium_expires_at")
    private LocalDateTime premiumExpiresAt;

    @Column(name = "monthly_play_count")
    @Builder.Default
    private Long monthlyPlayCount = 0L;

    @Column(name = "play_count_reset_date")
    private LocalDateTime playCountResetDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_followed_artists",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    @Builder.Default
    private Set<com.musicPlayer.app.artist.entity.Artist> followedArtists = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_liked_songs",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    @Builder.Default
    private Set<com.musicPlayer.app.song.entity.Song> likedSongs = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return status != AccountStatus.BANNED; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return status == AccountStatus.ACTIVE; }

    public boolean isPremium() {
        return role == Role.PREMIUM || role == Role.ADMIN ||
               (premiumExpiresAt != null && premiumExpiresAt.isAfter(LocalDateTime.now()));
    }

    public enum Role {
        USER, PREMIUM, ADMIN
    }

    public enum AccountStatus {
        ACTIVE, SUSPENDED, BANNED
    }
    
    
}