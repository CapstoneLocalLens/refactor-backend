package com.example.localens.Member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.localens.member.domain.Member;
import com.example.localens.member.dto.MemberRequestDto;
import com.example.localens.member.dto.MemberResponseDto;
import com.example.localens.member.dto.TokenDto;
import com.example.localens.member.exception.AlreadyRegisteredException;
import com.example.localens.member.jwt.TokenProvider;
import com.example.localens.member.repository.MemberRepository;
import com.example.localens.member.service.MemberService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;
    private MemberRequestDto memberRequestDto;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .memberUuid(UUID.randomUUID())
                .name("testUser")
                .email("test@example.com")
                .password("passwordHash")
                .build();

        memberRequestDto = new MemberRequestDto("testUser", "test@example.com", "password", "password");
    }

    @Test
    void signup_ShouldSaveMember() {
        when(memberRepository.existsByEmail(memberRequestDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(memberRequestDto.getPassword())).thenReturn("encodePassword");
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        MemberResponseDto result = memberService.signup(memberRequestDto);

        assertEquals("test@example.com", result.getEmail());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void signup_ShouldThrowException_WhenEmailAlreadyExists() {
        when(memberRepository.existsByEmail(memberRequestDto.getEmail())).thenReturn(true);

        assertThrows(AlreadyRegisteredException.class, () -> memberService.signup(memberRequestDto));
    }

    @Test
    void validateMember_ShouldReturnMember_WhenValid() {
        when(memberRepository.findByNameAndEmail("testUser", "test@example.com"))
                .thenReturn(Optional.of(testMember));

        Optional<Member> result = memberService.validateMember("testUser", "test@example.com");

        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getName());
    }

    @Test
    void validateMember_ShouldReturnEmpty_WhenInvalid() {
        when(memberRepository.findByNameAndEmail("invalidUser", "invalid@example.com"))
                .thenReturn(Optional.empty());

        Optional<Member> result = memberService.validateMember("invalidUser", "invalid@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void generateResetToken_ShouldReturnToken() {
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        String resetToken = memberService.generateResetToken(testMember);

        assertNotNull(resetToken);
        assertEquals(testMember.getResetToken(), resetToken);
        verify(memberRepository, times(1)).save(testMember);
    }

    @Test
    void resetPassword_ShouldUpdatePassword_WhenTokenIsValid() {
        String resetToken = "valid-token";
        testMember.setResetToken(resetToken);

        when(memberRepository.findByResetToken(resetToken)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        memberService.resetPassword(resetToken, "newPassword", "newPassword");

        assertEquals("encodedNewPassword", testMember.getPassword());
        assertNull(testMember.getResetToken());
        verify(memberRepository, times(1)).save(testMember);
    }

    @Test
    void resetPassword_ShouldThrowException_WhenTokenIsInvalid() {
        when(memberRepository.findByResetToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> memberService.resetPassword("invalid-token", "newPassword", "newPassword"));
    }

    @Test
    void resetPassword_ShouldThrowException_WhenPasswordsDoNotMatch() {
        String resetToken = "valid-token";
        testMember.setResetToken(resetToken);

        assertThrows(IllegalArgumentException.class,
                () -> memberService.resetPassword(resetToken, "newPassword", "mismatchedPassword"));
    }
}
