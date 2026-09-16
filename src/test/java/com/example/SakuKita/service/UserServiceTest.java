package com.example.SakuKita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SaldoService saldoService;

    private UserService userService;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, saldoService);
    }

    @Test
    void testTambahUser_PasswordDiBcrypt() {
        User inputUser = new User(null, "Budi", "budi@email.com", "rahasia123");

        when(userRepository.findByEmail("budi@email.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = userService.tambahUser(inputUser);

        assertNotNull(result);
        assertEquals("budi@email.com", result.getEmail());
        assertTrue(result.getPassword().startsWith("$2a$") || result.getPassword().startsWith("$2b$"));
        assertTrue(encoder.matches("rahasia123", result.getPassword()));
        verify(saldoService).saldoAwal(any(User.class));
    }

    @Test
    void testLogin_SuksesDenganBcrypt() {
        String hashedPassword = encoder.encode("passwordBenar");
        User user = new User(1L, "Budi", "budi@email.com", hashedPassword);

        when(userRepository.findByEmail("budi@email.com")).thenReturn(Optional.of(user));

        User loggedInUser = userService.login("budi@email.com", "passwordBenar");
        assertNotNull(loggedInUser);
        assertEquals("Budi", loggedInUser.getName());
    }

    @Test
    void testLogin_PasswordSalah_ThrowsException() {
        String hashedPassword = encoder.encode("passwordBenar");
        User user = new User(1L, "Budi", "budi@email.com", hashedPassword);

        when(userRepository.findByEmail("budi@email.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.login("budi@email.com", "passwordSalah"));
    }

    @Test
    void testLogin_UserLamaPlainText_BerhasilDanDiupgradeKeBcrypt() {
        User user = new User(2L, "UserLama", "lama@email.com", "plain123");

        when(userRepository.findByEmail("lama@email.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User loggedInUser = userService.login("lama@email.com", "plain123");
        assertNotNull(loggedInUser);
        assertTrue(loggedInUser.getPassword().startsWith("$2a$") || loggedInUser.getPassword().startsWith("$2b$"));
        assertTrue(encoder.matches("plain123", loggedInUser.getPassword()));
    }
}

