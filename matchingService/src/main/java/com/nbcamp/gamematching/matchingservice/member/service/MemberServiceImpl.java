package com.nbcamp.gamematching.matchingservice.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nbcamp.gamematching.matchingservice.board.entity.Board;
import com.nbcamp.gamematching.matchingservice.board.service.BoardService;
import com.nbcamp.gamematching.matchingservice.common.domain.CreatePageable;
import com.nbcamp.gamematching.matchingservice.exception.NotFoundException.NotFoundMatchingException;
import com.nbcamp.gamematching.matchingservice.exception.NotFoundException.NotFoundMemberException;
import com.nbcamp.gamematching.matchingservice.matching.domain.MemberLog;
import com.nbcamp.gamematching.matchingservice.matching.dto.NicknameDto;
import com.nbcamp.gamematching.matchingservice.matching.entity.MatchingLog;
import com.nbcamp.gamematching.matchingservice.matching.entity.ResultMatching;
import com.nbcamp.gamematching.matchingservice.matching.repository.MatchingLogRepository;
import com.nbcamp.gamematching.matchingservice.matching.repository.ResultMatchingRepository;
import com.nbcamp.gamematching.matchingservice.member.domain.FileDetail;
import com.nbcamp.gamematching.matchingservice.member.domain.MemberRoleEnum;
import com.nbcamp.gamematching.matchingservice.member.dto.*;
import com.nbcamp.gamematching.matchingservice.member.dto.BoardPageDto.BoardContent;
import com.nbcamp.gamematching.matchingservice.member.entity.Member;
import com.nbcamp.gamematching.matchingservice.member.entity.Profile;
import com.nbcamp.gamematching.matchingservice.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MatchingLogRepository matchingLogRepository;
    private final BoardService boardService;
    private final FileUploadService fileUploadService;
    private final ResultMatchingRepository resultMatchingRepository;

    @Value("${admin.password}")
    private String admin;

    @Override
    public ProfileDto getMyProfile(Member member) {
        // 혹시 요청한 멤버가 삭제된 멤버일 수 있으니 Repository 에 찾아서 DTO 를 만든다
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(NotFoundMemberException::new);

        return new ProfileDto(findMember);
    }

    @Override
    public BoardPageDto getMyBoards(Long memberId, Pageable pageable) {

        Page<Board> boardList = boardService.findAllByMemberId(memberId, pageable);

        List<BoardContent> boardContents = boardList.getContent().stream().map(BoardContent::new)
                .collect(Collectors.toList());

        return BoardPageDto.builder()
                .contents(boardContents)
                .numberOfElements(boardList.getNumberOfElements())
                .totalElements(boardList.getNumberOfElements())
                .totalPages(boardList.getTotalPages())
                .currentPage(pageable.getPageNumber())
                .build();
    }


    @Override
    public List<ProfileDto> getMyBuddies(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);

        List<Member> buddies = findMember.getMyBuddies();
        return ProfileDto.of(buddies);
    }

    @Override
    public List<BuddyRequestDto> getBuddyRequests(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);

        List<Member> notYetBuddyList = findMember.getNotYetBuddies();
        return BuddyRequestDto.of(notYetBuddyList);
    }

    @Override
    public List<MatchingLog2Dto> getMyMatching2List(Long memberId) {
        List<MatchingLog2Dto> matchingLog2DtoList = new ArrayList<>();

        List<ResultMatching> resultMatchingList = getResultMatchingList(
                memberId);
        for (ResultMatching resultMatching : resultMatchingList) {
            if (resultMatching.getPlayMode().contains("2")) {
                List<MatchingLog> matching = matchingLogRepository.findAllByResultMatching(
                        resultMatching);

                List<Member> members = matching.stream().map(MatchingLog::getMember)
                        .filter(member -> (member.getId() != memberId))
                        .collect(Collectors.toList());

                MatchingLog2Dto matchingLog2Dto = new MatchingLog2Dto(members.get(0),
                        resultMatching.getId());
                matchingLog2DtoList.add(matchingLog2Dto);
            }
        }
        return matchingLog2DtoList;
    }

    @Override
    public List<MatchingLog5Dto> getMyMatching5List(Long memberId) {
        List<MatchingLog5Dto> matchingLog5DtoList = new ArrayList<>();

        List<ResultMatching> resultMatchingList = getResultMatchingList(memberId);

        for (ResultMatching resultMatching : resultMatchingList) {
            if (resultMatching.getPlayMode().contains("5")) {
                List<MatchingLog> matching = matchingLogRepository.findAllByResultMatching(
                        resultMatching);
                List<MemberLog> memberAndLogs = matching.stream()
                        .map(MatchingLog::getMemberAndLog)
                        .filter(memberAndLog -> (memberAndLog.getMemberId() != memberId))
                        .collect(Collectors.toList());

                MatchingLog5Dto matchingLog5Dto = new MatchingLog5Dto(memberAndLogs,
                        resultMatching.getId());
                matchingLog5DtoList.add(matchingLog5Dto);
            }
        }
        return matchingLog5DtoList;
    }

    private List<ResultMatching> getResultMatchingList(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        List<MatchingLog> matchingLogList = matchingLogRepository.findAllByMember(findMember);

        return matchingLogList.stream()
                .map(MatchingLog::getResultMatching).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<String> changeMyProfile(Member member, UpdateProfileRequest request,
            MultipartFile image) {
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(NotFoundMemberException::new);
        Profile findMemberProfile = findMember.getProfile();

        if (image != null) {
            FileDetail fileDetail = fileUploadService.save(image);
            findMemberProfile.changeProfile(request, fileDetail.getPath());
        } else {
            findMemberProfile.changeProfile(request, "");
        }



        return new ResponseEntity<>("프로필이 변경되었습니다.", HttpStatus.OK);
    }

    @Override
    public ProfileDto getOtherProfile(Long userId) {
        Member findMember = memberRepository.findById(userId)
                .orElseThrow(NotFoundMemberException::new);

        return new ProfileDto(findMember);
    }

    @Override
    public ResponseEntity<String> requestBuddy(Long memberId, Long targetUserId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        Member targetMember = memberRepository.findById(targetUserId)
                .orElseThrow(NotFoundMemberException::new);

        targetMember.addNotYetBuddies(findMember);
        return new ResponseEntity<>("친구 요청되었습니다.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> answerBuddyRequest(Long memberId, Long requestMemberId,
            Boolean answer) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        findMember.changeNotYetBuddies(requestMemberId, answer);
        String message = answer ? "친구 등록되었습니다." : "요청이 거부되었습니다.";
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @Override
    public Member responseMemberByMemberEmail(String memberEmail) {
        return memberRepository.findByEmail(memberEmail).orElseThrow(NotFoundMemberException::new);
    }

    @Override
    public ResponseEntity<String> deleteMyBuddy(Long memberId, Long buddyId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);

        findMember.deleteBuddy(buddyId);
        return new ResponseEntity<>("친구가 삭제되었습니다.", HttpStatus.OK);
    }

    @Override
    public List<MemberAdminDto> findAllByAdmin(Integer page) {
        Page<Member> memberPage = memberRepository.findAll(CreatePageable.createPageable(page));
        return MemberAdminDto.of(memberPage.getContent());
    }

    @Override
    public void deleteByAdmin(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    @Override
    public ResponseEntity<String> changeRole(Long id, String adminId) {
        Member findMember = memberRepository.findById(id).orElseThrow(NotFoundMemberException::new);

        if (adminId.equals(admin)) {
            findMember.changeRole(MemberRoleEnum.ADMIN);
            return new ResponseEntity<>("관리자로 변경되었습니다.", HttpStatus.OK);
        } else {
            findMember.changeRole(MemberRoleEnum.USER);
            return new ResponseEntity<>("유저로 변경되었습니다.", HttpStatus.OK);
        }

    }

    @Override
    public List<NicknameDto> findNicknamesInMatching(List<MatchingLog> matchingLogs, Long memberId) {
        List<Member> members = matchingLogs.stream().map(MatchingLog::getMember).filter(member -> (member.getId() != memberId))
                .collect(Collectors.toList());
        return NicknameDto.of(members);

    }

}
