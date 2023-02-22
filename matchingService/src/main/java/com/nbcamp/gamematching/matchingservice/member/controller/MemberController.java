package com.nbcamp.gamematching.matchingservice.member.controller;

import com.nbcamp.gamematching.matchingservice.member.domain.FileStore;
import com.nbcamp.gamematching.matchingservice.member.dto.AnswerBuddyRequestDto;
import com.nbcamp.gamematching.matchingservice.member.dto.BoardPageDto.BoardContent;
import com.nbcamp.gamematching.matchingservice.member.dto.BuddyDto;
import com.nbcamp.gamematching.matchingservice.member.dto.BuddyRequestDto;
import com.nbcamp.gamematching.matchingservice.member.dto.ProfileDto;
import com.nbcamp.gamematching.matchingservice.member.dto.UpdateProfileRequest;
import com.nbcamp.gamematching.matchingservice.member.entity.Member;
import com.nbcamp.gamematching.matchingservice.member.service.MemberService;
import com.nbcamp.gamematching.matchingservice.security.UserDetailsImpl;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @Value("${file.dir}")
    private String fileDir;

    private final FileStore fileStore;

    @GetMapping("/")
    @ResponseBody
    public ProfileDto getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        return memberService.getMyProfile(member);
//        model.addAttribute("profile", memberService.getMyProfile(member));
//        return "member/profile";/
    }

    @GetMapping("/boards")
    @ResponseBody
    public List<BoardContent> getMyBoardList(Pageable pageable,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        Pageable newPageable = toPageable(pageable.getPageNumber(),
                pageable.getPageSize());
//        model.addAttribute("boardList", memberService.getMyBoards(member.getId(), newPageable));
//        return "member/boardList";
        return memberService.getMyBoards(member.getId(), newPageable).getContents();
    }

    @GetMapping("/buddy")
    @ResponseBody
    public List<BuddyDto> getMyBuddyList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        List<BuddyDto> myBuddies = memberService.getMyBuddies(member.getId());
//        model.addAttribute("buddyList", myBuddies);
//        return "member/buddyList";
        return myBuddies;
    }

    @GetMapping("/notYetBuddy")
    @ResponseBody
    public List<BuddyRequestDto> getBuddyRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        List<BuddyRequestDto> myBuddies = memberService.getBuddyRequests(member.getId());
//        model.addAttribute("buddyRequestList", myBuddies);
//        return "member/buddyRequestList";
        return myBuddies;
    }

    // 친구 신청
    @PatchMapping("/notYetBuddy/{userId}")
    public ResponseEntity<String> requestBuddy(@PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        return memberService.requestBuddy(member.getId(), userId);
    }

    // 친구 수락/거절
    @PostMapping("/notYetBuddy")
    public ResponseEntity<String> answerBuddyRequest(@RequestBody AnswerBuddyRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        return memberService.answerBuddyRequest(member.getId(), request.getRequestMemberId(),
                request.getAnswer());
    }

    @PostMapping("/")
    public ResponseEntity<String> changeMyProfile(@RequestPart UpdateProfileRequest request,
            @RequestPart MultipartFile image,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        Member member = userDetails.getMember();
        log.info("multipartFile={}", image);
        return memberService.changeMyProfile(member, request, image);
    }

    @GetMapping("/{memberId}")
    @ResponseBody
    public ProfileDto getOtherProfile(@PathVariable Long memberId) {
        return memberService.getOtherProfile(memberId);
//        model.addAttribute("profile", memberService.getOtherProfile(memberId));
//        return "member/profile";
    }

    public static Pageable toPageable(Integer currentPage, Integer size) {
        return PageRequest.of((currentPage - 1), size);
    }
}
