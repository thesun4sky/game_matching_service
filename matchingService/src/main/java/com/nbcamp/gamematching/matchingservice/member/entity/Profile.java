package com.nbcamp.gamematching.matchingservice.member.entity;

import com.nbcamp.gamematching.matchingservice.exception.SignException.InvalidNickname;
import com.nbcamp.gamematching.matchingservice.member.domain.GameType;
import com.nbcamp.gamematching.matchingservice.member.domain.Tier;
import com.nbcamp.gamematching.matchingservice.member.dto.UpdateProfileRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @Column
    private String nickname;

    @Column
    private String profileImage;

    @Enumerated(EnumType.STRING)
    private Tier tier;

    @Enumerated(EnumType.STRING)
    private GameType game;

    private Integer mannerPoints = 0;

    @Builder
    public Profile(String nickname, String profileImage, Tier tier, GameType game) {
        if (Pattern.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣|a-z|A-Z|]{3,20}.*", nickname)) {
            this.nickname = nickname;
        } else {
            throw new InvalidNickname();
        }

        this.profileImage = profileImage;

        if (Tier.isContains(tier)) {
            this.tier = tier;
        }
        if (GameType.isContains(game)) {
            this.game = game;
        }
    }

    public void changeProfile(UpdateProfileRequest profileRequest, String imageDir) {
        if (!imageDir.equals("")) {
            this.profileImage = imageDir;
        }
        if (!profileRequest.getNickname().isEmpty()) {
            this.nickname = profileRequest.getNickname();
        }
        if (profileRequest.getGame() != null) {
            this.game = profileRequest.getGame();
        }
        if (profileRequest.getTier() != null) {
            this.tier = profileRequest.getTier();
        }
    }

    public void changeMannerPoints(String UpDown) {
        if (UpDown.equals("UP")) {
            this.mannerPoints += 10;
        } else if (UpDown.equals("DOWN")) {
            this.mannerPoints -= 10;
        }

    }
}
